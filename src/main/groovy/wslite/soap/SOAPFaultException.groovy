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

class SOAPFaultException extends SOAPClientException {

    @Delegate
    SOAPResponse soapResponse

    SOAPFaultException(SOAPResponse response) {
        super(parseFaultCode(response) + ' - ' + parseFaultReason(response), response.httpRequest, response.httpResponse)
        this.soapResponse = response
    }

    private static String parseFaultCode(SOAPResponse faultResponse) {
        String faultCode
        switch (faultResponse.soapVersion) {
            case SOAPVersion.V1_1:
                faultCode = faultResponse.fault.':faultcode'.text()
                break
            case SOAPVersion.V1_2:
                faultCode = faultResponse.fault."${SOAP.SOAP_NS_PREFIX}:Code"."${SOAP.SOAP_NS_PREFIX}:Value".text()
                break
        }
        return faultCode
    }

    private static String parseFaultReason(SOAPResponse faultResponse) {
        String faultReason
        switch (faultResponse.soapVersion) {
            case SOAPVersion.V1_1:
                faultReason = faultResponse.fault.':faultstring'.text()
                break
            case SOAPVersion.V1_2:
                faultReason = faultResponse.fault."${SOAP.SOAP_NS_PREFIX}:Reason"."${SOAP.SOAP_NS_PREFIX}:Text".text()
                break
        }
        return faultReason
    }

}
