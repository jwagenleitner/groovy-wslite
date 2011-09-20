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

import spock.lang.*

class SOAPResponseSpec extends Specification {

    def soap11Response = """
        <?xml version='1.0' encoding='UTF-8'?>
        <SOAP:Envelope xmlns:SOAP='http://schemas.xmlsoap.org/soap/envelope/'>
          <SOAP:Header>
            <apiToken>742ABC</apiToken>
          </SOAP:Header>
          <SOAP:Body>
            <GetAddressResponse>
                <Address1>742 Evergreen Terrace</Address1>
                <Address2>Apt 101</Address2>
                <City>Springfield</City>
            </GetAddressResponse>
          </SOAP:Body>
        </SOAP:Envelope>""".trim()

    def soapFaultResponse = """
         <?xml version='1.0' encoding='UTF-8'?>
         <SOAP:Envelope xmlns:SOAP='http://schemas.xmlsoap.org/soap/envelope/'>
           <SOAP:Header />
           <SOAP:Body>
             <SOAP:Fault>
               <faultcode>742</faultcode>
               <faultstring>client error</faultstring>
               <details>
                     <error>over quantity limit</error>
                     <error>out of stock</error>
               </details>
             </SOAP:Fault>
           </SOAP:Body>
         </SOAP:Envelope>""".trim()

    def parseText(text) {
        return new XmlSlurper().parseText(text)
    }

    def "access first Body element if it exists"() {
        when:
        def response = new SOAPResponse(envelope: parseText(soap11Response))

        then:
        "Springfield" == response.GetAddressResponse.City.text()
        "Springfield" == response.envelope.Body.GetAddressResponse.City.text()
    }

    def "access first Body element that does not exist returns empty string"() {
        when:
        def response = new SOAPResponse(envelope: parseText(soap11Response))

        then:
        "" == response.GetAddressResponse2.City.text()
    }

    def "should report soap fault is present"() {
        when:
        def response = new SOAPResponse(envelope: parseText(soapFaultResponse))

        then:
        response.hasFault()
    }

    def "should return soap fault if present"() {
        when:
        def response = new SOAPResponse(envelope: parseText(soapFaultResponse))

        then:
        response.hasFault()
        "742" == response.fault.faultcode.text()
    }

    def "should not report soap fault is present for a valid response"() {
        when:
        def response = new SOAPResponse(envelope: parseText(soap11Response))

        then:
        !response.hasFault()
    }

    def "should access the soap body directly"() {
        when:
        def response = new SOAPResponse(envelope: parseText(soap11Response))

        then:
        "742 Evergreen Terrace" == response.body.GetAddressResponse.Address1.text()
    }

    def "should access the soap header directly"() {
        when:
        def response = new SOAPResponse(envelope: parseText(soap11Response))

        then:
        "742ABC" == response.header.apiToken.text()
    }

}
