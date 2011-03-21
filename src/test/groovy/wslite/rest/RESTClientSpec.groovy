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
package wslite.rest

import spock.lang.*
import wslite.http.*
import wslite.json.*

class RESTClientSpec extends Specification {

    String testURL = "http://ws.org"
    RESTClient client = new RESTClient(testURL)

    def "minimal get"() {
        when:
        def response = client.get()

        then:
        200 == response.statusCode
    }

    def "no args constructor and setting base url as a property"() {
        when:
        def restClient = new RESTClient()
        restClient.url = "http://test.org"
        def httpClient = new MockHTTPClient()
        httpClient.response = getMockResponse()
        restClient.httpClient = httpClient
        def response = restClient.get()

        then:
        200 == response.statusCode
        "http://test.org" == response.url.toString()
    }

    def "xml response parsing"() {
        expect:
        client.httpClient.response.contentType = contentType
        client.httpClient.response.data = """<?xml version="1.0"?><test><foo>bar</foo></test>""".bytes
        def response = client.get()
        response.XML.foo.text() == foo
        assert 0 < response.TEXT.size()
        assert response instanceof XmlResponse
        assert response instanceof TextResponse

        where:
        contentType                 | foo
        "application/xml"           | "bar"
        "text/xml"                  | "bar"
        "application/xhtml+xml"     | "bar"
        "application/atom+xml"      | "bar"
    }

    def "response parsing of text responses"() {
        expect:
        client.httpClient.response.contentType = contentType
        client.httpClient.response.data = """bar""".bytes
        def response = client.get()
        response.TEXT == foo
        assert response instanceof TextResponse

        where:
        contentType                 | foo
        "text/plain"                | "bar"
        "text/html"                 | "bar"
        "text/csv"                  | "bar"
    }

    def "response parsing of JSONObject responses"() {
        expect:
        client.httpClient.response.contentType = contentType
        client.httpClient.response.data = """{"foo":"bar"}""".bytes
        def response = client.get()
        response.JSON.foo == foo
        assert response.JSON instanceof JSONObject
        assert response instanceof JsonResponse

        where:
        contentType                 | foo
        "text/javascript"           | "bar"
        "text/json"                 | "bar"
        "application/json"          | "bar"
    }

    def "response parsing of JSONArray responses"() {
        expect:
        client.httpClient.response.contentType = contentType
        client.httpClient.response.data = """[{"foo":"bar"},{"foo":"baz"}]""".bytes
        def response = client.get()
        response.JSON[0].foo == foo0
        response.JSON[1].foo == foo1
        assert response.JSON.size() == 2
        assert response.JSON instanceof JSONArray
        assert response instanceof JsonResponse

        where:
        contentType                 | foo0      | foo1
        "text/javascript"           | "bar"     | "baz"
        "text/json"                 | "bar"     | "baz"
        "application/json"          | "bar"     | "baz"
    }

    def "custom response handler"() {
        when:
        client.addResponseHandler(MockCustomResponse)
        client.httpClient.response.contentType = "application/foo"
        client.httpClient.response.data = """bar""".bytes
        def response = client.get()

        then:
        response instanceof MockCustomResponse
        "bar" == response.CUSTOM
        4 == client.responseHandlers.size()
        MockCustomResponse == client.responseHandlers.first()
    }

    def "custom response handler takes precedence over default handlers"() {
        when:
        client.addResponseHandler(MockXmlResponse)
        client.httpClient.response.contentType = "application/xml"
        client.httpClient.response.data = """bar""".bytes
        def response = client.get()

        then:
        response instanceof MockXmlResponse
        "bar" == response.MOCK_XML
        4 == client.responseHandlers.size()
        MockXmlResponse == client.responseHandlers.first()
    }

    def "no accept header if no accept param specified"() {
        when:
        def response = client.get()

        then:
        null == client.httpClient.request.headers.Accept
    }

    def "default accept header"() {
        when:
        client.setDefaultAcceptHeader("text/xml")
        def response = client.get()

        then:
        "text/xml" == client.httpClient.request.headers.Accept
    }

    def "accept params overrides default accept header"() {
        when:
        client.setDefaultAcceptHeader("text/xml")
        def response = client.get(accept: "application/json")

        then:
        "application/json" == client.httpClient.request.headers.Accept
    }

    def "accept header params overrides default accept header"() {
        when:
        client.setDefaultAcceptHeader("text/xml")
        def response = client.get(accept: "application/json", headers:[Accept: "text/csv"])

        then:
        "text/csv" == client.httpClient.request.headers.Accept
    }

    def "content type header sets header even if no data"() {
        when:
        def response = client.get(headers:["Content-Type": "application/json"])

        then:
        "application/json" == client.httpClient.request.headers."Content-Type"
    }

    def "default content type"() {
        when:
        client.setDefaultContentTypeHeader("application/xml")
        def response = client.post() {
            text "foo"
        }

        then:
        "application/xml; charset=${client.defaultCharset}" == client.httpClient.request.headers."Content-Type"
    }

    def "content type param overrides default content type"() {
        when:
        client.setDefaultContentTypeHeader("application/xml")
        def response = client.post() {
            type "text/plain"
            text "foo"
        }

        then:
        "text/plain; charset=${client.defaultCharset}" == client.httpClient.request.headers."Content-Type"
    }

    def "content type header overrides param and default content type"() {
        when:
        client.setDefaultContentTypeHeader("application/xml")
        def response = client.post(headers:["Content-Type":"text/csv"]) {
            type "text/plain"
            text "foo"
        }

        then:
        "text/csv" == client.httpClient.request.headers."Content-Type"
    }

    def "default charset is applied when content-type param is set"() {
        when:
        def response = client.post() {
            type "text/plain"
            text "foo"
        }

        then:
        "text/plain; charset=UTF-8" == client.httpClient.request.headers."Content-Type"
    }

    def "charset param overrides default charset when content-type param is set"() {
        when:
        client.defaultCharset = "UTF-8"
        def response = client.post() {
            type "text/plain"
            charset "ISO-8859-1"
            text "foo"
        }

        then:
        "text/plain; charset=ISO-8859-1" == client.httpClient.request.headers."Content-Type"
    }

    def "charset in content-type header overrides all"() {
        when:
        client.defaultCharset = "UTF-8"
        def response = client.post(headers:["Content-Type":"text/csv; charset=US-ASCII"]) {
            type "text/plain"
            charset "ISO-8859-1"
            text "foo"
        }

        then:
        "text/csv; charset=US-ASCII" == client.httpClient.request.headers."Content-Type"
    }

    def "charset not set if not specified in content-type header"() {
        when:
        client.defaultCharset = "UTF-8"
        def response = client.post(headers:["Content-Type":"text/csv"]) {
            type "text/plain"
            charset = "ISO-8859-1"
            text "foo"
        }

        then:
        "text/csv" == client.httpClient.request.headers."Content-Type"
    }

    def "default content-type and charset"() {
        when:
        client.defaultContentTypeHeader = "application/vnd+json"
        client.defaultCharset = "ISO-8859-1"
        def response = client.post() {
            text "foo"
        }

        then:
        "application/vnd+json; charset=ISO-8859-1" == client.httpClient.request.headers."Content-Type"
    }

    def "original parameters are not modified"() {
        when:
        def params = [path: "/foo"]
        client.defaultContentTypeHeader = "application/vnd+json"
        def response = client.post(params) {
            text "foo"
        }

        then:
        null == params.contentType
    }

    def setup() {
        def httpClient = new MockHTTPClient()
        httpClient.response = getMockResponse()
        client.httpClient = httpClient
    }

    HTTPResponse getMockResponse(HTTPHeaderMap headers=[:], data=null) {
        def response = new HTTPResponse()
        response.statusCode = 200
        response.statusMessage = "OK"
        response.headers = headers
        response.data = data
        response.contentEncoding = headers["Content-Encoding"]
        response.contentType = headers["Content-Type"]
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

class MockCustomResponse extends TextResponse {
    def CUSTOM

    MockCustomResponse(HTTPResponse response) {
        super(response)
        CUSTOM = TEXT
    }

    static boolean handles(String contentType) {
        return "application/foo" == contentType
    }
}

class MockXmlResponse extends TextResponse {
    def MOCK_XML

    MockXmlResponse(HTTPResponse response) {
        super(response)
        MOCK_XML = TEXT
    }

    static boolean handles(String contentType) {
        return XmlResponse.handles(contentType)
    }
}
