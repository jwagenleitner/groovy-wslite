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

import wslite.http.HTTPClient

class SOAPClient {

    String serviceURL
    def http = new HTTPClient()

    def send(headers=[:], builder=new SOAPMessageBuilder(), content) {
        def message = buildSOAPMessage(content, builder)
        if (!headers.'Content-Type') {
            headers.'Content-Type' = (message.version == SOAPVersion.V1_1) ? 'text/xml; charset=UTF-8' : 'application/soap+xml; charset=UTF-8'
        }
        def response = http.executeMethod("POST", new URL(serviceURL), message.toString().bytes, headers)
        response['Envelope'] = parseEnvelope(response.data)
        if (!response.Envelope.Body.Fault.isEmpty()) {
            def soapFault = buildSOAPFaultException(response.Envelope.Body.Fault)
            soapFault.response = response
            throw soapFault
        }
        return response
    }

    private def buildSOAPMessage(content, builder) {
        content.delegate = builder
        content.call()
        return builder
    }

    private def parseEnvelope(data) {
        return new XmlSlurper().parse(new ByteArrayInputStream(data))
    }

    private def buildSOAPFaultException(fault) {
        return new SOAPFaultException(fault.faultcode.text(), fault.faultstring.text(), fault.faultactor.text(), fault.detail.text())
    }
}
