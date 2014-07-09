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

import spock.lang.*
import org.spockframework.runtime.*
import wslite.http.*

class SOAPClientSpec extends Specification {

    SOAPClient soapClient = new SOAPClient(serviceURL: 'http://test.com')

    void 'parse valid SOAP response message'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap11Response.bytes)

        when:
        def response = soapClient.send(testSoapMessage)

        then:
        'foo' == response.envelope.Header.token.text()
        'bar' == response.envelope.Body.GetFoo.result.text()
    }

    @Issue("#46")
    void 'handles one way message exchanges'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: null)

        when:
        def response = soapClient.send(testSoapMessage)

        then:
        null == response.body
        null == response.envelope
        !response.hasFault()
        !response.hasHeader()
        null == response.soapVersion
    }

    void 'throws parse exception if XML response is invalid'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: 'foo'.bytes)

        when:
        def response = soapClient.send(testSoapMessage)

        then:
        def ex = thrown(SOAPMessageParseException)
        'foo' == ex.response.contentAsString
    }

    def 'throws exception if SOAP response message has invalid root element'() {
        given:
        def soapResponse = '''
            <?xml version='1.0' encoding='UTF-8'?>
            <SOAP:Package xmlns:SOAP='http://schemas.xmlsoap.org/soap/envelope/'>
              <SOAP:Body>
                <GetFoo>
                  <result>bar</result>
                </GetFoo>
              </SOAP:Body>
            </SOAP:Package>'''.trim()

        and:
        soapClient.httpClient = mockHTTPClient(data: soapResponse.bytes)

        when:
        def response = soapClient.send(testSoapMessage)

        then: thrown(SOAPMessageParseException)
    }

    void 'throws exception if SOAP response message has invalid Body element'() {
        given:
        def soapResponse = '''
            <?xml version='1.0' encoding='UTF-8'?>
            <SOAP:Envelope xmlns:SOAP='http://schemas.xmlsoap.org/soap/envelope/'>
              <SOAP:Torso>
                <GetFoo>
                  <result>bar</result>
                </GetFoo>
              </SOAP:Torso>
            </SOAP:Envelope>'''.trim()

        and:
        soapClient.httpClient = mockHTTPClient(data: soapResponse.bytes)

        when:
        def response = soapClient.send(testSoapMessage)

        then: thrown(SOAPMessageParseException)
    }

    void 'parse exception contains soap response text'() {
        given: 'a SOAP response with no Body element'
        def soapResponse = '''
            <?xml version='1.0' encoding='UTF-8'?>
            <SOAP:Envelope xmlns:SOAP='http://schemas.xmlsoap.org/soap/envelope/'>
              <SOAP:Torso>
                <GetFoo>
                  <result>bar</result>
                </GetFoo>
              </SOAP:Torso>
            </SOAP:Envelope>'''.trim()

        and:
        soapClient.httpClient = mockHTTPClient(data: soapResponse.bytes)

        when:
        def response = soapClient.send(testSoapMessage)

        then:
        def smpe = thrown(SOAPMessageParseException)
        smpe.soapMessageText.contains('<result>bar</result>')
    }

    void 'should add SOAPAction to request headers'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap11Response.bytes)

        when:
        def response = soapClient.send(SOAPAction: 'http://foo/bar', testSoapMessage)

        then:
        'http://foo/bar' == response.httpRequest.headers.SOAPAction
    }

    void 'should add empty string SOAPAction to request headers if set by client'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap11Response.bytes)

        when:
        def response = soapClient.send(SOAPAction: '', testSoapMessage)

        then:
        '' == response.httpRequest.headers.SOAPAction
    }

    void 'should not add SOAPAction to request headers if SOAPAction header already present'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap11Response.bytes)

        when:
        def response = soapClient.send(SOAPAction: 'http://foo/bar', headers: [soapaction: ''], testSoapMessage)

        then:
        '' == response.httpRequest.headers.SOAPAction
    }

    void 'should add action parameter to Content-Type header for SOAP 1.2 messages'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap12Response.bytes)

        when:
        def response = soapClient.send(SOAPAction: 'http://foo/bar') {
            version SOAPVersion.V1_2
            body { test(true) }
        }

        then:
        response.httpRequest.headers.'Content-Type'.endsWith('; action="http://foo/bar"')
    }

    void 'send should pass arguments in the http request'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap11Response.bytes)

        when:
        def response = soapClient.send(readTimeout: 15000, connectTimeout: 30000,
                                            followRedirects: false, testSoapMessage)

        then:
        15000 == response.httpRequest.readTimeout
        30000 == response.httpRequest.connectTimeout
        !response.httpRequest.followRedirects
    }

    void 'content-type for http request should default based on SOAP 1.1 message version'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap11Response.bytes)

        when:
        def response = soapClient.send {
            version SOAPVersion.V1_1
            body { test() }
        }

        then:
        response.httpRequest.headers.'content-type'.startsWith(SOAP.SOAP_V11_MEDIA_TYPE)
    }

    void 'content-type for http request should default based on SOAP 1.2 message version'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap12Response.bytes)

        when:
        def response = soapClient.send {
            version SOAPVersion.V1_2
            body { test() }
        }

        then:
        response.httpRequest.headers.'content-type'.startsWith(SOAP.SOAP_V12_MEDIA_TYPE)
    }

    void 'content-type specified in header for http request should override setting based on message version'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap12Response.bytes)

        when:
        def response = soapClient.send(headers: ['content-type': 'vendor/soap']) {
            version SOAPVersion.V1_2
            body { test() }
        }

        then:
        'vendor/soap' == response.httpRequest.headers.'content-type'
    }

    void 'send raw string with soap 1.1 message and soap version will be detected'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap11Response.bytes)

        when:
        def response = soapClient.send('''
            <?xml version='1.0' encoding='UTF-8'?>
            <SOAP:Envelope xmlns:SOAP='http://schemas.xmlsoap.org/soap/envelope/'>
                <SOAP:Body>
                    <GetFoo>bar</GetFoo>
                </SOAP:Body>
            </SOAP:Envelope>''')

        then:
        response.httpRequest.headers.'content-type'.startsWith(SOAP.SOAP_V11_MEDIA_TYPE)
    }

    void 'send raw string with soap 1.2 message and soap version will be detected'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap12Response)

        when:
        def response = soapClient.send('''<?xml version='1.0' encoding='UTF-8'?>
                                <SOAP:Envelope xmlns:SOAP='http://www.w3.org/2003/05/soap-envelope'>
                                  <SOAP:Body>
                                    <GetFoo>bar</GetFoo>
                                  </SOAP:Body>
                                </SOAP:Envelope>''')

        then:
        response.httpRequest.headers.'content-type'.startsWith(SOAP.SOAP_V12_MEDIA_TYPE)
    }

    void 'send raw string with soap message and override the soap version used'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap12Response.bytes)

        when:
        def response = soapClient.send(SOAPVersion.V1_2,
                                '''<?xml version='1.0' encoding='UTF-8'?>
                                <SOAP:Envelope xmlns:SOAP='http://www.w3.org/2003/05/soap-envelope'>
                                  <SOAP:Body>
                                    <GetFoo>bar</GetFoo>
                                  </SOAP:Body>
                                </SOAP:Envelope>''')

        then:
        response.httpRequest.headers.'content-type'.startsWith(SOAP.SOAP_V12_MEDIA_TYPE)
    }

    void 'can send raw soap message with http params'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap11Response)

        when: 'a raw text string is sent'
        def response = soapClient.send(connectTimeout: 7000, readTimeout: 9000,
                                '''<?xml version='1.0' encoding='UTF-8'?>
                                <SOAP:Envelope xmlns:SOAP='http://www.w3.org/2003/05/soap-envelope'>
                                  <SOAP:Body>
                                    <GetFoo>bar</GetFoo>
                                  </SOAP:Body>
                                </SOAP:Envelope>''')

        then:
        7000 == response.httpRequest.connectTimeout
        9000 == response.httpRequest.readTimeout
    }

    void 'can send raw soap message with http params and overriding soap version'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap12Response.bytes)

        when:
        def response = soapClient.send(SOAPVersion.V1_2,
                                connectTimeout: 7000,
                                readTimeout: 9000,
                                '''<?xml version='1.0' encoding='UTF-8'?>
                                <SOAP:Envelope xmlns:SOAP='http://www.w3.org/2003/05/soap-envelope'>
                                  <SOAP:Body>
                                    <GetFoo>bar</GetFoo>
                                  </SOAP:Body>
                                </SOAP:Envelope>''')

        then:
        response.httpRequest.headers.'content-type'.startsWith(SOAP.SOAP_V12_MEDIA_TYPE)
        7000 == response.httpRequest.connectTimeout
        9000 == response.httpRequest.readTimeout
    }

    void 'throws exception if an HTTP exception is thrown'() {
        given:
        soapClient.httpClient = [execute: { httpRequest ->
            throw new HTTPClientException('fail', null, httpRequest, new HTTPResponse(statusCode: 500))
        }] as HTTPClient

        when:
        def response = soapClient.send(testSoapMessage)

        then:
        def ex = thrown(SOAPClientException)
        ex.response.statusCode == 500
    }

    void 'original parameters are not modified'() {
        given:
        def origParams = [SOAPAction: 'urn:foo', connectTimeout: 5000, readTimeout: 10000]

        and:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap11Response.bytes)

        when:
        def response = soapClient.send(origParams, testSoapMessage)

        then:
        3 == origParams.size()
        'urn:foo' == origParams.SOAPAction
        5000 == origParams.connectTimeout
        10000 == origParams.readTimeout
    }

    void 'uses default character encoding if none is specified'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap11Response.bytes)

        when:
        def response = soapClient.send {
            body {
                foo()
            }
        }

        then:
        SOAP.DEFAULT_CHAR_ENCODING == new ContentTypeHeader(response.httpRequest.headers['Content-Type']).charset
    }

    void 'uses character encoding specified in the message builder'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap11Response.bytes)

        when:
        def response = soapClient.send {
            encoding 'ISO-8859-1'
            body {
                foo()
            }
        }

        then:
        'ISO-8859-1' == new ContentTypeHeader(response.httpRequest.headers['Content-Type']).charset
    }

    void 'uses character encoding specified in the Content-Type header'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap11Response.bytes)

        when:
        def response = soapClient.send(headers: ['Content-Type': 'text/xml; charset=UTF-16']) {
            encoding 'ISO-8859-1'
            body {
                foo()
            }
        }

        then:
        'UTF-16' == new ContentTypeHeader(response.httpRequest.headers['Content-Type']).charset
    }

    void 'uses character encoding specified in the xml declaration'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap11Response.bytes)

        when:
        def response = soapClient.send('''<?xml version='1.0' encoding='ISO-8859-2'?>
                                <SOAP:Envelope xmlns:SOAP='http://www.w3.org/2003/05/soap-envelope'>
                                  <SOAP:Body>
                                    <GetFoo/>
                                  </SOAP:Body>
                                </SOAP:Envelope>'''.trim())

        then:
        'ISO-8859-2' == new ContentTypeHeader(response.httpRequest.headers['Content-Type']).charset
    }

    void 'uses default character encoding if no Content-Type charset or xml declaration encoding'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap11Response.bytes)

        when:
        def response = soapClient.send('''<?xml version='1.0'?>
                                <SOAP:Envelope xmlns:SOAP='http://www.w3.org/2003/05/soap-envelope'>
                                  <SOAP:Body>
                                    <GetFoo/>
                                  </SOAP:Body>
                                </SOAP:Envelope>'''.trim())

        then:
        SOAP.DEFAULT_CHAR_ENCODING == new ContentTypeHeader(response.httpRequest.headers['Content-Type']).charset
    }

    void 'Content-Type header specified in the request be used as-is and not modified'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap11Response.bytes)

        when:
        def response = soapClient.send(headers: ['Content-Type': 'application/xml+soap+foo']) {
            body {
                foo()
            }
        }

        then:
        'application/xml+soap+foo' == response.httpRequest.headers['Content-Type']
    }

    void 'allows doctype in xml markup'() {
        given:
        soapClient.httpClient = mockHTTPClient(data: simpleSoap11ResponseWithDocType.bytes)

        when:
        def response = soapClient.send {
            body {
                foo()
            }
        }

        then:
        'bar with DOCTYPE' == response.GetFoo.result.text()
    }

    private static final Closure testSoapMessage = { body { test(true) } }

    private static final String simpleSoap11Response = '''
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
            </SOAP:Envelope>'''.trim()

    private static final String simpleSoap12Response = '''<?xml version='1.0' encoding='UTF-8'?>
                                <SOAP:Envelope xmlns:SOAP='http://www.w3.org/2003/05/soap-envelope'>
                                  <SOAP:Body>
                                    <GetFoo/>
                                  </SOAP:Body>
                                </SOAP:Envelope>'''.trim()

    private static final String simpleSoap11ResponseWithDocType = '''<!DOCTYPE SOAP:Envelope >
            <SOAP:Envelope xmlns:SOAP='http://schemas.xmlsoap.org/soap/envelope/'>
              <SOAP:Body>
                <GetFoo>
                  <result>bar with DOCTYPE</result>
                </GetFoo>
              </SOAP:Body>
            </SOAP:Envelope>'''.trim()

    private mockHTTPClient(Map responseParams) {
        return [execute: { httpRequest ->
            return new HTTPResponse(responseParams)
        }] as HTTPClient
    }

}
