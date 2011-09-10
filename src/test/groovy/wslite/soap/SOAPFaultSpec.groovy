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
import wslite.http.HTTPClient
import wslite.http.HTTPClientException
import wslite.http.HTTPResponse

class SOAPFaultSpec extends Specification {

    def soapClient = new SOAPClient(serviceURL: "http://test.com")
    def testSoapMessage = { body { test(true) } }

    def sampleSOAP11Fault = """
<?xml version='1.0' encoding='UTF-8'?>
<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>
    <soap:Body>
        <soap:Fault xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>
            <faultcode>soap:Client</faultcode>
            <faultstring>Invalid message format</faultstring>
            <faultactor>http://example.org/someactor</faultactor>
            <detail>
                <m:msg xmlns:m='http://example.org/faults/exceptions'>
                    Test message
                </m:msg>
            </detail>
        </soap:Fault>
    </soap:Body>
</soap:Envelope>
""".trim()

    def sampleSOAP12Fault = """
<?xml version='1.0' encoding='UTF-8'?>
<env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope"
    xmlns:m="http://www.example.org/timeouts"
    xmlns:xml="http://www.w3.org/XML/1998/namespace">
    <env:Body>
        <env:Fault>
            <env:Code>
                <env:Value>env:Sender* </env:Value>
                <env:Subcode>
                     <env:Value>m:MessageTimeout* </env:Value>
                </env:Subcode>
            </env:Code>
            <env:Reason>
                <env:Text xml:lang="en">Sender Timeout* </env:Text>
            </env:Reason>
            <env:Detail>
                <m:MaxTime>P5M* </m:MaxTime>
            </env:Detail>
        </env:Fault>
    </env:Body>
</env:Envelope>
""".trim()

    def "throws exception if SOAP 1.1 Fault response is returned from server"() {
        given:
        def httpc = [execute:{req ->
            throw new HTTPClientException("fault", null, null, new HTTPResponse(data:sampleSOAP11Fault.bytes))
        }] as HTTPClient
        soapClient.httpClient = httpc

        when:
        def response = soapClient.send(testSoapMessage)

        then:
        def sfe = thrown(SOAPFaultException)
        sfe.message.contains("soap:Client")
        sfe.message.contains("Invalid message format")
        "soap:Client" == sfe.fault.faultcode.text()
    }

    def "throws exception if SOAP 1.2 Fault response is returned from server"() {
        given:
        def httpc = [execute:{req ->
            throw new HTTPClientException("fault", null, null, new HTTPResponse(data:sampleSOAP12Fault.bytes))
        }] as HTTPClient
        soapClient.httpClient = httpc

        when:
        def response = soapClient.send(testSoapMessage)

        then:
        def sfe = thrown(SOAPFaultException)
        sfe.message.contains("env:Sender*")
        sfe.message.contains("Sender Timeout")
        "env:Sender* " == sfe.fault.Code.Value.text()
    }

    def "throws exception if SOAP 1.1 Fault response is returned from server with http status code success"() {
        given:
        def httpc = [execute:{req ->
            return new HTTPResponse(statusCode:200, data:sampleSOAP11Fault.bytes)
        }] as HTTPClient
        soapClient.httpClient = httpc

        when:
        def response = soapClient.send(testSoapMessage)

        then:
        def sfe = thrown(SOAPFaultException)
        sfe.message.contains("soap:Client")
        sfe.message.contains("Invalid message format")
        "soap:Client" == sfe.fault.faultcode.text()
    }

    def "throws exception if SOAP 1.2 Fault response is returned from server with http status code success"() {
        given:
        def httpc = [execute:{req ->
            return new HTTPResponse(statusCode:200, data:sampleSOAP12Fault.bytes)
        }] as HTTPClient
        soapClient.httpClient = httpc

        when:
        def response = soapClient.send(testSoapMessage)

        then:
        def sfe = thrown(SOAPFaultException)
        sfe.message.contains("env:Sender*")
        sfe.message.contains("Sender Timeout")
        "env:Sender* " == sfe.fault.Code.Value.text()
    }

}
