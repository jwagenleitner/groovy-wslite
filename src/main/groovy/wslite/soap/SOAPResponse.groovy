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
        envelope = soapEnvelope
        envelope.declareNamespace(soap11:SOAP.SOAP11_NS, soap12:SOAP.SOAP12_NS)
        def namespace = [:]
        if (!envelope.'soap11:Body'.isEmpty()) {
            namespace.'soap-env' = SOAP.SOAP11_NS
            soapVersion = SOAPVersion.V1_1
        }
        if (!envelope.'soap12:Body'.isEmpty()) {
            namespace.'soap-env' = SOAP.SOAP12_NS
            soapVersion = SOAPVersion.V1_2
        }
        if (!namespace) {
            throw new IllegalStateException("No SOAP 1.1 or 1.2 Body element found.")
        }
        envelope.declareNamespace(namespace)
    }

    def getBody() {
        return envelope.'soap-env:Body'
    }

    def getHeader() {
        return envelope.'soap-env:Header'
    }

    boolean hasHeader() {
        return !getHeader().isEmpty()
    }

    def getFault() {
        return getBody().'soap-env:Fault'
    }

    boolean hasFault() {
        return !getFault().isEmpty()
    }

    def propertyMissing(String name) {
        return getBody()."${name}"
    }

    @Override
    String toString() {
        def includes = ['soapVersion', 'httpRequest', 'httpResponse']
        ObjectHelper.dump(this, include:includes)
    }

}
