/* Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wslite.soap

import wslite.http.*
import wslite.http.auth.*

class SOAPClient {

    static final String SOAP_V11_CONTENT_TYPE = "text/xml; charset=UTF-8"
    static final String SOAP_V12_CONTENT_TYPE = "application/soap+xml; charset=UTF-8"

    String serviceURL
    HTTPClient httpClient

    SOAPClient() {
        this.httpClient = new HTTPClient()
    }

    SOAPClient(String url, HTTPClient httpClient=new HTTPClient()) {
        this.serviceURL = url
        this.httpClient = httpClient
    }

    SOAPClient(HTTPClient httpClient) {
        this.httpClient = httpClient
    }

    void setAuthorization(HTTPAuthorization authorization) {
        this.httpClient.authorization = authorization
    }

    SOAPResponse send(Map requestParams=[:], Closure content) {
        def message = buildSOAPMessage(content)
        return send(requestParams, message.version, message.toString())
    }

    SOAPResponse send(Map requestParams=[:], String content) {
        return send(requestParams, SOAPVersion.V1_1, content)
    }

    SOAPResponse send(Map requestParams=[:], SOAPVersion soapVersion, String content) {
        HTTPRequest httpRequest
        HTTPResponse httpResponse
        def httpRequestParams = new LinkedHashMap(requestParams ?: [:])
        try {
            httpRequest = buildHTTPRequest(httpRequestParams, soapVersion, content)
            httpResponse = httpClient.execute(httpRequest)
        } catch (HTTPClientException httpEx) {
            throw new SOAPClientException(httpEx.message, httpEx, httpEx.request, httpEx.response)
        } catch (Exception ex) {
            throw new SOAPClientException(ex.message, ex, httpRequest, httpResponse)
        }
        return buildSOAPResponse(httpRequest, httpResponse)
    }

    private SOAPMessageBuilder buildSOAPMessage(content) {
        def builder = new SOAPMessageBuilder()
        content.resolveStrategy = Closure.DELEGATE_FIRST
        content.delegate = builder
        content.call()
        return builder
    }

    private HTTPRequest buildHTTPRequest(requestParams, soapVersion, message) {
        def soapAction = requestParams.remove("SOAPAction")
        def httpRequest = new HTTPRequest(requestParams)
        httpRequest.url = new URL(serviceURL)
        httpRequest.method = HTTPMethod.POST
        httpRequest.data = message.bytes
        if (!httpRequest.headers."Content-Type") {
            httpRequest.headers["Content-Type"] = (soapVersion == SOAPVersion.V1_1) ?
                                                   SOAP_V11_CONTENT_TYPE : SOAP_V12_CONTENT_TYPE
        }
        if (!httpRequest.headers."SOAPAction" && soapAction && soapVersion == SOAPVersion.V1_1) {
            httpRequest.headers.SOAPAction = soapAction
        }
        return httpRequest
    }

    private SOAPResponse buildSOAPResponse(httpRequest, httpResponse) {
        SOAPResponse response
        try {
            String soapMessageText = httpResponse.getContentAsString()
            def soapEnvelope = parseEnvelope(soapMessageText)
            response = new SOAPResponse(httpRequest:httpRequest,
                                        httpResponse:httpResponse,
                                        envelope:soapEnvelope,
                                        text:soapMessageText)
        } catch (Exception ex) {
            throw new SOAPMessageParseException(ex.message, ex, httpRequest, httpResponse)
        }
        if (response.hasFault()) {
            throw buildSOAPFaultException(response)
        }
        return response
    }

    private def parseEnvelope(String soapMessageText) {
        def envelopeNode
        envelopeNode = new XmlSlurper().parseText(soapMessageText)
        if (envelopeNode.name() != "Envelope") {
            throw new IllegalStateException("Root element is " + envelopeNode.name() + ", expected 'Envelope'")
        }
        if (!envelopeNode.childNodes().find {it.name() == "Body"}) {
            throw new IllegalStateException("Body element is missing")
        }
        return envelopeNode
    }

    private SOAPFaultException buildSOAPFaultException(soapResponse) {
        SOAPFaultException sfe = new SOAPFaultException("${soapResponse.fault.faultstring.text()} [$soapResponse.fault.faultcode.text()]".toString())
        sfe.response = soapResponse
        sfe.faultcode = soapResponse.fault.faultcode.text()
        sfe.faultstring = soapResponse.fault.faultstring.text()
        sfe.faultactor = soapResponse.fault.faultactor.text()
        sfe.detail = soapResponse.fault.detail.text()
        return sfe
    }
}
