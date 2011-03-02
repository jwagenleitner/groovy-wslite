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

class SOAPClient {

    static final String SOAP_V11_CONTENT_TYPE = "text/xml; charset=UTF-8"
    static final String SOAP_V12_CONTENT_TYPE = "application/soap+xml; charset=UTF-8"

    String serviceURL
    HTTPClient httpClient

    SOAPClient(HTTPClient httpClient=new HTTPClient()) {
        this.httpClient = httpClient
    }

    SOAPResponse send(Map requestParams=[:], Closure content) {
        def message = buildSOAPMessage(content)
        return send(requestParams, message.version, message.toString())
    }

    SOAPResponse send(Map requestParams=[:], String content) {
        return send(requestParams, SOAPVersion.V1_1, content)
    }

    SOAPResponse send(Map requestParams=[:], SOAPVersion soapVersion, String content) {
        def httpRequest = buildHTTPRequest(requestParams, soapVersion, content)
        def response = httpClient.execute(httpRequest)
        def soapResponse = new SOAPResponse(httpResponse:response, Envelope:parseEnvelope(response.data))
        if (!soapResponse.Envelope.Body.Fault.isEmpty()) {
            def soapFault = buildSOAPFaultException(soapResponse.Envelope.Body.Fault)
            soapFault.response = soapResponse
            throw soapFault
        }
        return soapResponse
    }

    private def buildSOAPMessage(content) {
        def builder = new SOAPMessageBuilder()
        content.resolveStrategy = Closure.DELEGATE_FIRST
        content.delegate = builder
        content.call()
        return builder
    }

    private def buildHTTPRequest(requestParams, soapVersion, message) {
        def soapAction = requestParams.remove("SOAPAction")
        def httpRequest = new HTTPRequest(requestParams)
        httpRequest.url = new URL(serviceURL)
        httpRequest.method = HTTPMethod.POST
        httpRequest.data = message.bytes
        if (!httpRequest.headers."Content-Type") {
            httpRequest.headers["Content-Type"] = (soapVersion == SOAPVersion.V1_1) ? SOAP_V11_CONTENT_TYPE : SOAP_V12_CONTENT_TYPE
        }
        if (!httpRequest.headers."SOAPAction" && soapAction && soapVersion == SOAPVersion.V1_1) {
            httpRequest.headers.SOAPAction = soapAction
        }
        return httpRequest
    }

    private def parseEnvelope(data) {
        def envelopeNode
        try {
            envelopeNode = new XmlSlurper().parse(new ByteArrayInputStream(data))
        } catch (org.xml.sax.SAXParseException sax) {
            throw new SOAPMessageParseException("Unable to parse XML response", sax)
        } catch (Exception ex) {
            throw new SOAPMessageParseException("Invalid Argument", ex)
        }
        if (envelopeNode.name() != "Envelope") {
            throw new SOAPMessageParseException("Root element is " + envelopeNode.name() + ", expected 'Envelope'")
        }
        if (!envelopeNode.childNodes().find {it.name() == "Body"}) {
            throw new SOAPMessageParseException("Body element is missing")
        }
        return envelopeNode
    }

    private def buildSOAPFaultException(fault) {
        return new SOAPFaultException(  fault.faultcode.text(),
                                        fault.faultstring.text(),
                                        fault.faultactor.text(),
                                        fault.detail.text() )
    }
}
