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
import org.spockframework.runtime.*
import wslite.http.*

class SOAPClientSpec extends Specification {

    def soapClient = new SOAPClient(serviceURL: "http://test.com")
    def testSoapMessage = { body { test(true) } }
    def simpleSoapResponse = """<?xml version='1.0' encoding='UTF-8'?>
                                <SOAP:Envelope xmlns:SOAP='http://schemas.xmlsoap.org/soap/envelope/'>
                                  <SOAP:Body>
                                    <GetFoo/>
                                  </SOAP:Body>
                                </SOAP:Envelope>""".trim()

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

    def "should add SOAPAction to request headers"() {
        setup:
        def httpc = [execute:{req -> return new HTTPResponse(headers:req.headers, data:simpleSoapResponse.bytes)}] as HTTPClient
        soapClient.httpClient = httpc

        when: "a message is sent that includes a SOAPAction"
        def response = soapClient.send(SOAPAction:"http://foo/bar", testSoapMessage)

        then:
        "http://foo/bar" == response.headers.SOAPAction
    }

    def "send should pass arguments in the http request"() {
        setup:
        def httpc = [
            execute: {req ->
                        assert 15000 == req.readTimeout
                        assert 30000 == req.connectTimeout
                        assert false == req.followRedirects
                        [data:simpleSoapResponse.bytes]
        }] as HTTPClient
        soapClient.httpClient = httpc

        when: "send args contain http request params"
        def response = soapClient.send(readTimeout:15000, connectTimeout:30000, followRedirects:false, testSoapMessage)

        then: notThrown(ConditionNotSatisfiedError)
    }

    def "content-type for http request should default based on SOAP 1.1 message version"() {
        setup: "an http client expecting a SOAP 1.1 content-type for the request"
        def httpc = [
            execute: {req ->
                        assert SOAPClient.SOAP_V11_CONTENT_TYPE == req.headers.find { it.key.toLowerCase() == "content-type" }?.value
                        [data:simpleSoapResponse.bytes]
        }] as HTTPClient
        soapClient.httpClient = httpc

        when: "a v1.1 message is sent"
        def response = soapClient.send {
            version SOAPVersion.V1_1
            body { test() }
        }

        then: notThrown(ConditionNotSatisfiedError)
    }

    def "content-type for http request should default based on SOAP 1.2 message version"() {
        setup: "an http client expecting a SOAP 1.2 content-type for the request"
        def httpc = [
            execute: {req ->
                        assert SOAPClient.SOAP_V12_CONTENT_TYPE == req.headers.find { it.key.toLowerCase() == "content-type" }?.value
                        [data:simpleSoapResponse.bytes]
        }] as HTTPClient
        soapClient.httpClient = httpc

        when: "a v1.2 message is sent"
        def response = soapClient.send {
            version SOAPVersion.V1_2
            body { test() }
        }

        then: notThrown(ConditionNotSatisfiedError)
    }

    def "content-type specified in header for http request should override setting based on message version"() {
        setup: "an http client expecting a custom content-type for the request"
        def httpc = [
            execute: {req ->
                        assert "vendor/soap" == req.headers.find { it.key.toLowerCase() == "content-type" }?.value
                        [data:simpleSoapResponse.bytes]
        }] as HTTPClient
        soapClient.httpClient = httpc

        when: "a message is sent that includes a custom content-type header"
        def response = soapClient.send(headers:["content-type":"vendor/soap"]) {
            version SOAPVersion.V1_2
            body { test() }
        }

        then: notThrown(ConditionNotSatisfiedError)
    }

    def "can send raw soap message"() {
        given: "a soap client configured to echo the soap request to soap response"
        def httpc = [execute:{req -> [data:req.data]}] as HTTPClient
        soapClient.httpClient = httpc

        when: "a raw text string is sent"
        def response = soapClient.send(SOAPVersion.V1_1,
                                """<?xml version='1.0' encoding='UTF-8'?>
                                <SOAP:Envelope xmlns:SOAP='http://schemas.xmlsoap.org/soap/envelope/'>
                                  <SOAP:Body>
                                    <GetFoo>bar</GetFoo>
                                  </SOAP:Body>
                                </SOAP:Envelope>""".trim())

        then:
        "bar" == response.Envelope.Body.GetFoo.text()
    }

    def "can send raw soap message with http params"() {
        given: "a soap client configured to echo the soap request to soap response and verify http params"
        def httpc = [execute:{req ->
                        assert 7000 == req.connectTimeout
                        assert 9000 == req.readTimeout
                    [data:req.data]}] as HTTPClient
        soapClient.httpClient = httpc

        when: "a raw text string is sent"
        def response = soapClient.send(SOAPVersion.V1_1,
                                connectTimeout:7000,
                                readTimeout:9000,
                                """<?xml version='1.0' encoding='UTF-8'?>
                                <SOAP:Envelope xmlns:SOAP='http://schemas.xmlsoap.org/soap/envelope/'>
                                  <SOAP:Body>
                                    <GetFoo>bar</GetFoo>
                                  </SOAP:Body>
                                </SOAP:Envelope>""".trim())

        then:
        notThrown(ConditionNotSatisfiedError)
        "bar" == response.Envelope.Body.GetFoo.text()
    }

}
