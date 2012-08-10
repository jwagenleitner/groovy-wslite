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

import wslite.http.HTTPRequest
import wslite.http.HTTPResponse
import groovy.util.slurpersupport.GPathResult
import wslite.util.ObjectHelper

class SOAPResponse {

    SOAPVersion soapVersion
    HTTPRequest httpRequest
    HTTPResponse httpResponse
    GPathResult envelope
    String text

    void setEnvelope(GPathResult soapEnvelope) {
        if (!soapEnvelope) {
            return
        }
        envelope = soapEnvelope
        def namespace = [:]
        switch (envelope.namespaceURI()) {
            case SOAP.SOAP11_NS:
                namespace[SOAP.SOAP_NS_PREFIX] = SOAP.SOAP11_NS
                soapVersion = SOAPVersion.V1_1
                break
            case SOAP.SOAP12_NS:
                namespace[SOAP.SOAP_NS_PREFIX] = SOAP.SOAP12_NS
                soapVersion = SOAPVersion.V1_2
                break
        }
        if (!namespace) {
            throw new IllegalStateException('No SOAP 1.1 or 1.2 Envelope found')
        }
        envelope.declareNamespace(namespace)
    }

    def getBody() {
        return envelope?."${SOAP.SOAP_NS_PREFIX}:${SOAP.BODY_ELEMENT_NAME}"
    }

    def getHeader() {
        return envelope?."${SOAP.SOAP_NS_PREFIX}:${SOAP.HEADER_ELEMENT_NAME}"
    }

    boolean hasHeader() {
        return envelope != null && !getHeader()?.isEmpty()
    }

    def getFault() {
        return getBody()?."${SOAP.SOAP_NS_PREFIX}:${SOAP.FAULT_ELEMENT_NAME}"
    }

    boolean hasFault() {
        return envelope != null && !getFault()?.isEmpty()
    }

    def propertyMissing(String name) {
        return getBody()?."${name}"
    }

    @Override
    String toString() {
        def includes = ['soapVersion', 'httpRequest', 'httpResponse']
        ObjectHelper.dump(this, include:includes)
    }

}
