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
    def testValidSOAPResponse = """
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
    def testInvalidSOAPResponseEnvelope = """
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
    def testInvalidSOAPResponseBody = """
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

    def "parse valid SOAP response message"() {
        given:
        def httpc = [execute:{req -> [data:testValidSOAPResponse.bytes]}] as HTTPClient
        soapClient.httpClient = httpc

        when:
        def response = soapClient.send(testSoapMessage)

        then:
        "foo" == response.Envelope.Header.token.text()
        "bar" == response.Envelope.Body.GetFoo.result.text()
    }

    def "throws parse exception if no response"() {
        given:
        def httpc = [execute:{req -> [data:null]}] as HTTPClient
        soapClient.httpClient = httpc

        when:
        def response = soapClient.send(testSoapMessage)

        then:
        thrown(SOAPMessageParseException)
    }

    def "throws parse exception if XML response is invalid"() {
        given:
        def httpc = [execute:{req -> [data:"foo".bytes]}] as HTTPClient
        soapClient.httpClient = httpc

        when:
        def response = soapClient.send(testSoapMessage)

        then:
        thrown(SOAPMessageParseException)
    }

    def "throws exception if SOAP response message has invalid root element"() {
        given:
        def httpc = [execute:{req -> [data:testInvalidSOAPResponseEnvelope.bytes]}] as HTTPClient
        soapClient.httpClient = httpc

        when:
        def response = soapClient.send(testSoapMessage)

        then:
        thrown(SOAPMessageParseException)
    }

    def "throws exception if SOAP response message has invalid Body element"() {
        given:
        def httpc = [execute:{req -> [data:testInvalidSOAPResponseBody.bytes]}] as HTTPClient
        soapClient.httpClient = httpc

        when:
        def response = soapClient.send(testSoapMessage)

        then:
        thrown(SOAPMessageParseException)
    }

}
