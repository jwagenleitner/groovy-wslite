/* Copyright 2011-2012 the original author or authors.
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

    String serviceURL
    HTTPClient httpClient

    boolean validating = false
    boolean namespaceAware = true
    boolean allowDocTypeDeclaration = true

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
        def message = new SOAPMessageBuilder().build(content)
        return send(requestParams, message.version, message.toString())
    }

    SOAPResponse send(Map requestParams=[:], String content) {
        return send(requestParams, detectSOAPVersion(content), content)
    }

    SOAPResponse send(Map requestParams=[:], SOAPVersion soapVersion, String content) {
        HTTPRequest httpRequest
        HTTPResponse httpResponse
        def httpRequestParams = new LinkedHashMap(requestParams ?: [:])
        try {
            httpRequest = buildHTTPRequest(httpRequestParams, soapVersion, content)
            httpResponse = httpClient.execute(httpRequest)
        } catch (HTTPClientException httpEx) {
            generateSOAPFaultException(httpEx)
        } catch (Exception ex) {
            throw new SOAPClientException(ex.message, ex, httpRequest, httpResponse)
        }
        SOAPResponse soapResponse = buildSOAPResponse(httpRequest, httpResponse)
        if (soapResponse.hasFault()) {
            throw new SOAPFaultException(soapResponse)
        }
        return soapResponse
    }

    private HTTPRequest buildHTTPRequest(requestParams, soapVersion, message) {
        def soapAction = requestParams.remove(SOAP.SOAP_ACTION_HEADER)
        def httpRequest = new HTTPRequest(requestParams)
        httpRequest.url = new URL(serviceURL)
        httpRequest.method = HTTPMethod.POST
        String charEncoding = getCharacterEncoding(httpRequest, message)
        setContentTypeHeaderIfNotPresent(httpRequest, soapVersion, charEncoding)
        setSoapActionHeaderIfNotPresent(httpRequest, soapVersion, soapAction)
        httpRequest.data = message.getBytes(charEncoding)
        return httpRequest
    }

    private SOAPResponse buildSOAPResponse(httpRequest, httpResponse) {
        SOAPResponse response
        try {
            String soapMessageText = httpResponse.contentAsString
            def soapEnvelope = soapMessageText ? parseEnvelope(soapMessageText) : null
            response = new SOAPResponse(httpRequest:httpRequest,
                                        httpResponse:httpResponse,
                                        envelope:soapEnvelope,
                                        text:soapMessageText)
        } catch (Exception ex) {
            throw new SOAPMessageParseException(ex.message, ex, httpRequest, httpResponse)
        }
        return response
    }

    private parseEnvelope(String soapMessageText) {
        def envelopeNode = new XmlSlurper(validating, namespaceAware, allowDocTypeDeclaration).parseText(soapMessageText)
        if (envelopeNode.name() != SOAP.ENVELOPE_ELEMENT_NAME) {
            throw new IllegalStateException('Root element is ' + envelopeNode.name() +
                    ', expected ' + SOAP.ENVELOPE_ELEMENT_NAME)
        }
        if (envelopeNode."${SOAP.BODY_ELEMENT_NAME}".isEmpty()) {
            throw new IllegalStateException(SOAP.BODY_ELEMENT_NAME + ' element is missing')
        }
        return envelopeNode
    }

    private void generateSOAPFaultException(HTTPClientException httpEx) {
        SOAPFaultException soapFaultException
        try {
            SOAPResponse soapResponse = buildSOAPResponse(httpEx.request, httpEx.response)
            if (!soapResponse.hasFault()) {
                throw httpEx
            }
            soapFaultException = new SOAPFaultException(soapResponse)
        } catch (Exception) {
            throw new SOAPClientException(httpEx.message, httpEx, httpEx.request, httpEx.response)
        }
        throw soapFaultException
    }

    private String getCharacterEncoding(HTTPRequest httpRequest, String message) {
        String encoding = getCharacterEncodingFromContentTypeHeader(httpRequest.headers[HTTP.CONTENT_TYPE_HEADER])
        if (encoding) {
            return encoding
        }
        return getCharacterEncodingFromXmlDeclaration(message) ?: SOAP.DEFAULT_CHAR_ENCODING
    }

    private String getCharacterEncodingFromContentTypeHeader(String contentType) {
        return new ContentTypeHeader(contentType).charset
    }

    private String getCharacterEncodingFromXmlDeclaration(String xml) {
        def m = xml =~ /^<\?xml.*?encoding\s*=\s*["|'](.*?)["|'].*\?>/
        return (m && m[0]?.size() == 2) ? m[0][1] : null
    }

    private void setContentTypeHeaderIfNotPresent(HTTPRequest httpRequest, SOAPVersion soapVersion, String charset) {
        if (httpRequest.headers[HTTP.CONTENT_TYPE_HEADER]) {
            return
        }
        httpRequest.headers[HTTP.CONTENT_TYPE_HEADER] = (soapVersion == SOAPVersion.V1_1) ?
                                               "${SOAP.SOAP_V11_MEDIA_TYPE}; charset=${charset}" :
                                               "${SOAP.SOAP_V12_MEDIA_TYPE}; charset=${charset}"
    }

    private void setSoapActionHeaderIfNotPresent(HTTPRequest httpRequest, SOAPVersion soapVersion, String soapAction) {
        if (soapAction == null) {
            return
        }
        if (soapVersion == SOAPVersion.V1_1) {
            if (!httpRequest.headers.containsKey(SOAP.SOAP_ACTION_HEADER)) {
                httpRequest.headers[SOAP.SOAP_ACTION_HEADER] = soapAction
            }
        } else if (soapVersion == SOAPVersion.V1_2) {
            httpRequest.headers[HTTP.CONTENT_TYPE_HEADER] += '; ' + SOAP.SOAP_ACTION_V12_HEADER + '="' + soapAction + '"'
        }
    }

    private SOAPVersion detectSOAPVersion(String content) {
        SOAPVersion sv = SOAPVersion.V1_1
        try {
            if (SOAP.SOAP12_NS == new XmlSlurper(validating, namespaceAware, allowDocTypeDeclaration).parseText(content).namespaceURI()) {
                sv = SOAPVersion.V1_2
            }
        } catch (Exception ex) { }
        return sv
    }

}
