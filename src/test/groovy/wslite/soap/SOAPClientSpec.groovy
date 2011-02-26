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
import wslite.http.*

class SOAPClientSpec extends Specification {

    def soapClient = new SOAPClient(serviceURL: "http://test.com")
    def testSoapMessage = { body { test(true) } }

    def "parse valid SOAP response message"() {
        given: "a SOAP 1.1 response that is valid"
        def soapResponse = """
            <?xml version='1.0' encoding='UTF-8'?>
            <SOAP:Envelope xmlns:SOAP='http://schemas.xmlsoap.org/soap/envelope/'>
              <SOAP:Header>
                <token>foo</token>
              </SOAP:Header>
              <SOAP:Body>
                <GetFoo>
                  <result>bar</result>
                </GetFoo>
              </SOAP:Body>
            </SOAP:Envelope>""".trim()

        and: "a soap client configured to receive this response"
        def httpc = [execute:{req -> [data:soapResponse.bytes]}] as HTTPClient
        soapClient.httpClient = httpc

        when: "a message is sent"
        def response = soapClient.send(testSoapMessage)

        then: "the parsed response is accessible"
        "foo" == response.Envelope.Header.token.text()
        "bar" == response.Envelope.Body.GetFoo.result.text()
    }

    def "throws parse exception if no response"() {
        given: "a soap client configured to receive no response"
        def httpc = [execute:{req -> [data:null]}] as HTTPClient
        soapClient.httpClient = httpc

        when: "a message is sent"
        def response = soapClient.send(testSoapMessage)

        then: thrown(SOAPMessageParseException)
    }

    def "throws parse exception if XML response is invalid"() {
        given: "a soap client configured to receive invalid XML"
        def httpc = [execute:{req -> [data:"foo".bytes]}] as HTTPClient
        soapClient.httpClient = httpc

        when: "a message is sent"
        def response = soapClient.send(testSoapMessage)

        then: thrown(SOAPMessageParseException)
    }

    def "throws exception if SOAP response message has invalid root element"() {
        given: "a SOAP message without the Envelope root node"
        def soapResponse = """
            <?xml version='1.0' encoding='UTF-8'?>
            <SOAP:Package xmlns:SOAP='http://schemas.xmlsoap.org/soap/envelope/'>
              <SOAP:Header>
                <token>foo</token>
              </SOAP:Header>
              <SOAP:Body>
                <GetFoo>
                  <result>bar</result>
                </GetFoo>
              </SOAP:Body>
            </SOAP:Package>""".trim()

        and: "a soap client configured to receive this response"
        def httpc = [execute:{req -> [data:soapResponse.bytes]}] as HTTPClient
        soapClient.httpClient = httpc

        when: "a message is sent"
        def response = soapClient.send(testSoapMessage)

        then: thrown(SOAPMessageParseException)
    }

    def "throws exception if SOAP response message has invalid Body element"() {
        given: "a SOAP response with no Body element"
        def soapResponse = """
            <?xml version='1.0' encoding='UTF-8'?>
            <SOAP:Envelope xmlns:SOAP='http://schemas.xmlsoap.org/soap/envelope/'>
              <SOAP:Header>
                <token>foo</token>
              </SOAP:Header>
              <SOAP:Torso>
                <GetFoo>
                  <result>bar</result>
                </GetFoo>
              </SOAP:Torso>
            </SOAP:Envelope>""".trim()

        and: "a soap client configured to receive this response"
        def httpc = [execute:{req -> [data:soapResponse.bytes]}] as HTTPClient
        soapClient.httpClient = httpc

        when: "a message is sent"
        def response = soapClient.send(testSoapMessage)

        then: thrown(SOAPMessageParseException)
    }

    def "throws exception if SOAP Fault response is returned from server"() {
        given: "a SOAP Fault response"
        def soapResponse = """
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

        and: "a soap client configured to receive this response"
        def httpc = [execute:{req -> [data:soapResponse.bytes]}] as HTTPClient
        soapClient.httpClient = httpc

        when: "a message is sent"
        def response = soapClient.send(testSoapMessage)

        then:
        def sfe = thrown(SOAPFaultException)
        "742" == sfe.faultcode
        "742" == sfe.response.Envelope.Body.Fault.faultcode.text()
    }

}
