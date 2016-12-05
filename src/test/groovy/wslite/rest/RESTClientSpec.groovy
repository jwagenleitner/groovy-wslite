/* Copyright 2011-2014 the original author or authors.
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
package wslite.rest

import spock.lang.*
import wslite.http.*

class RESTClientSpec extends Specification {

    RESTClient client

    def setup() {
        client = new RESTClient('http://ws.org')
        def httpClient = new MockHTTPClient()
        httpClient.response = getMockResponse()
        client.httpClient = httpClient
    }

    void 'minimal get'() {
        when:
        def response = client.get()

        then:
        200 == response.statusCode
    }

    void 'no args constructor and setting base url as a property'() {
        when:
        def restClient = new RESTClient()
        restClient.url = 'http://test.org'
        def httpClient = new MockHTTPClient()
        httpClient.response = getMockResponse()
        restClient.httpClient = httpClient
        def response = restClient.get()

        then:
        200 == response.statusCode
        'http://test.org' == response.url.toString()
    }

    void 'xml response parsing'() {
        given:
        client.httpClient.response.contentType = contentType
        client.httpClient.response.data = '''<?xml version="1.0"?><test><foo>bar</foo></test>'''.bytes
        def response = client.get()

        expect:
        response.xml.foo.text() == foo

        where:
        contentType                 | foo
        'application/xml'           | 'bar'
        'text/xml'                  | 'bar'
        'application/xhtml+xml'     | 'bar'
        'application/atom+xml'      | 'bar'
    }

    void 'response parsing of text responses'() {
        given:
        client.httpClient.response.contentType = contentType
        client.httpClient.response.data = 'bar'.bytes
        def response = client.get()

        expect:
        response.text == foo

        where:
        contentType                 | foo
        'text/plain'                | 'bar'
        'text/html'                 | 'bar'
        'text/csv'                  | 'bar'
    }

    void 'response parsing of JSONObject responses'() {
        given:
        client.httpClient.response.contentType = contentType
        client.httpClient.response.data = '''{"foo": "bar"}'''.bytes
        def response = client.get()

        expect:
        response.json.foo == foo

        where:
        contentType                 | foo
        'text/javascript'           | 'bar'
        'text/json'                 | 'bar'
        'application/json'          | 'bar'
    }

    void 'response parsing of JSONArray responses'() {
        given:
        client.httpClient.response.contentType = contentType
        client.httpClient.response.data = '''[{"foo": "bar"}, {"foo": "baz"}]'''.bytes
        def response = client.get()

        expect:
        response.json[0].foo == foo0
        response.json[1].foo == foo1
        assert response.json.size() == 2

        where:
        contentType                 | foo0      | foo1
        'text/javascript'           | 'bar'     | 'baz'
        'text/json'                 | 'bar'     | 'baz'
        'application/json'          | 'bar'     | 'baz'
    }

    void 'no accept header if no accept param specified'() {
        when:
        def response = client.get()

        then:
        null == client.httpClient.request.headers.Accept
    }

    void 'default accept header'() {
        when:
        client.setDefaultAcceptHeader('text/xml')
        def response = client.get()

        then:
        'text/xml' == client.httpClient.request.headers.Accept
    }

    void 'accept params overrides default accept header'() {
        when:
        client.setDefaultAcceptHeader('text/xml')
        def response = client.get(accept: 'application/json')

        then:
        'application/json' == client.httpClient.request.headers.Accept
    }

    void 'accept header params overrides default accept header'() {
        when:
        client.setDefaultAcceptHeader('text/xml')
        def response = client.get(accept: 'application/json', headers: [Accept: 'text/csv'])

        then:
        'text/csv' == client.httpClient.request.headers.Accept
    }

    void 'content type header set explicitly sets header even if no data'() {
        when:
        def response = client.get(headers: ['Content-Type': 'application/json'])

        then:
        'application/json' == client.httpClient.request.headers.'Content-Type'
    }

    void 'content type header not set automatically if no real data'() {
        when:
        def response = client.get([:]) {
            // no real content
        }

        then:
        !client.httpClient.request.headers.'Content-Type'
    }

    void 'default content type'() {
        when:
        client.setDefaultContentTypeHeader('application/xml')
        def response = client.post() {
            text 'foo'
        }

        then:
        "application/xml; charset=${client.defaultCharset}" == client.httpClient.request.headers."Content-Type"
    }

    void 'content type param overrides default content type'() {
        when:
        client.setDefaultContentTypeHeader('application/xml')
        def response = client.post() {
            type 'text/plain'
            text 'foo'
        }

        then:
        "text/plain; charset=${client.defaultCharset}" == client.httpClient.request.headers."Content-Type"
    }

    void 'content type header overrides param and default content type'() {
        when:
        client.setDefaultContentTypeHeader('application/xml')
        def response = client.post(headers: ['Content-Type': 'text/csv']) {
            type 'text/plain'
            text 'foo'
        }

        then:
        'text/csv' == client.httpClient.request.headers.'Content-Type'
    }

    void 'default charset is applied when content-type param is set'() {
        when:
        def response = client.post() {
            type 'text/plain'
            text 'foo'
        }

        then:
        "text/plain; charset=UTF-8" == client.httpClient.request.headers.'Content-Type'
    }

    void 'charset param overrides default charset when content-type param is set'() {
        when:
        client.defaultCharset = 'UTF-8'
        def response = client.post() {
            type 'text/plain'
            charset 'ISO-8859-1'
            text 'foo'
        }

        then:
        "text/plain; charset=ISO-8859-1" == client.httpClient.request.headers.'Content-Type'
    }

    void 'charset in content-type header overrides all'() {
        when:
        client.defaultCharset = 'UTF-8'
        def response = client.post(headers: ['Content-Type': 'text/csv; charset=US-ASCII']) {
            type 'text/plain'
            charset 'ISO-8859-1'
            text 'foo'
        }

        then:
        'text/csv; charset=US-ASCII' == client.httpClient.request.headers.'Content-Type'
    }

    void 'charset not set if not specified in content-type header'() {
        when:
        client.defaultCharset = 'UTF-8'
        def response = client.post(headers: ['Content-Type': 'text/csv']) {
            type 'text/plain'
            charset 'ISO-8859-1'
            text 'foo'
        }

        then:
        'text/csv' == client.httpClient.request.headers.'Content-Type'
    }

    void 'default content-type and charset'() {
        when:
        client.defaultContentTypeHeader = 'application/vnd+json'
        client.defaultCharset = 'ISO-8859-1'
        def response = client.post() {
            text 'foo'
        }

        then:
        'application/vnd+json; charset=ISO-8859-1' == client.httpClient.request.headers.'Content-Type'
    }

    void 'original parameters are not modified'() {
        when:
        def params = [path: '/foo']
        client.defaultContentTypeHeader = 'application/vnd+json'
        def response = client.post(params) {
            text 'foo'
        }

        then:
        null == params.contentType
    }

    void 'invalid url throws exception'() {
        when:
        def client = new RESTClient('foo:bar')
        client.get()

        then:
        def ex = thrown(RESTClientException)
        null == ex.request
        null == ex.response
    }

    void 'throws exception if fails to build a request and request and response are null on exception'() {
        given:
        def restClient = new RESTClient()

        when:
        restClient.get()

        then:
        def ex = thrown(RESTClientException)
        ex.request == null
        ex.response == null
    }

    void 'throws exception if HTTP exception is thrown and request and response are not null on exception'() {
        given:
        def restClient = new RESTClient('http://foo.org')
        restClient.httpClient = [execute: { request ->
            throw new HTTPClientException('fail', null, request, null)
        }] as HTTPClient

        when:
        restClient.get()

        then:
        def ex = thrown(RESTClientException)
        ex.request != null
        ex.response == null
        ex.request.url.toString() == 'http://foo.org'
    }

    void 'throws exception if fails to handle response content and request and response are not null on exception'() {
        given:
        client.httpClient.response.contentType = 'text/xml'
        client.httpClient.response.data = '<foo><name></foo>'.bytes

        when:
        client.get()

        then:
        def ex = thrown(RESTContentParseException)
        ex.request != null
        ex.response != null
        ex.response.contentType == 'text/xml'
        ex.response.contentAsString == '<foo><name></foo>'
    }

    void 'http patch method using method override'() {
        when:
        def response = client.patch()

        then:
        HTTPMethod.PATCH.toString() == client.httpClient.request.headers[HTTP.X_HTTP_METHOD_OVERRIDE_HEADER]
    }

    void 'http head method'() {
        when:
        def response = client.head()

        then:
        HTTPMethod.HEAD == client.httpClient.request.method
    }

    private getMockResponse(headers=[:], data=null) {
        def response = new HTTPResponse()
        response.statusCode = 200
        response.statusMessage = 'OK'
        response.headers = headers
        response.data = data
        response.contentEncoding = headers['Content-Encoding']
        response.contentType = headers['Content-Type']
        response.contentLength = data ? data.size() : 0
        return response
    }

}

class MockHTTPClient extends HTTPClient {

    HTTPResponse response
    HTTPRequest request

    @Override
    HTTPResponse execute(HTTPRequest request) {
        this.request = request
        response.url = request.url
        response.date = new Date()
        return response
    }
}
