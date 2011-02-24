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

    String serviceURL
    HTTPClient httpClient

    SOAPClient(HTTPClient httpClient=new HTTPClient()) {
        this.httpClient = httpClient
    }

    def send(requestParams=[:], content) {
        def message = buildSOAPMessage(content)
        def httpRequest = buildHTTPRequest(requestParams, message)
        def response = httpClient.execute(httpRequest)
        def soapResponse = new SOAPResponse(httpResponse:response)
        soapResponse['Envelope'] = parseEnvelope(response.data)
        if (!soapResponse.Envelope.Body.Fault.isEmpty()) {
            def soapFault = buildSOAPFaultException(soapResponse.Envelope.Body.Fault)
            soapFault.response = soapResponse
            throw soapFault
        }
        return soapResponse
    }

    private def buildSOAPMessage(content) {
        def builder = new SOAPMessageBuilder()
        content.delegate = builder
        content.call()
        return builder
    }

    private def buildHTTPRequest(requestParams, message) {
        def soapAction = requestParams.remove("SOAPAction")
        def httpRequest = new HTTPRequest(requestParams)
        httpRequest.url = new URL(serviceURL)
        httpRequest.method = HTTPMethod.POST
        httpRequest.data = message.toString().bytes
        if (!httpRequest.headers.'Content-Type') {
            httpRequest.headers.'Content-Type' = (message.version == SOAPVersion.V1_1) ? 'text/xml; charset=UTF-8' : 'application/soap+xml; charset=UTF-8'
        }
        if (!httpRequest.headers.SOAPAction && soapAction && message.version == SOAPVersion.V1_1) {
            httpRequest.headers.SOAPAction = soapAction
        }
        return httpRequest
    }

    private def parseEnvelope(data) {
        return new XmlSlurper().parse(new ByteArrayInputStream(data))
    }

    private def buildSOAPFaultException(fault) {
        return new SOAPFaultException(fault.faultcode.text(), fault.faultstring.text(), fault.faultactor.text(), fault.detail.text())
    }
}
