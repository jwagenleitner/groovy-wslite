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

class RequestBuilderSpec extends Specification {

    def "requires a method"() {
        when:
        new RequestBuilder().build(null, "http://ws.org", null, null)

        then: thrown(IllegalArgumentException)
    }

    def "requires a url"() {
        when:
        new RequestBuilder().build(HTTPMethod.GET, null, null, null)

        then: thrown(IllegalArgumentException)
    }

    def "adding path to url"() {
        given:
        def params = [path: "/users/123"]

        when:
        HTTPRequest request = new RequestBuilder().build(HTTPMethod.GET, "http://ws.org/services", params, null)

        then:
        "http://ws.org/services/users/123" ==  request.url.toString()
    }

    def "path with slash and url with trailing slash"() {
        given:
        def url =  "http://ws.org/services/"
        def params = [path: "/users/123"]

        when:
        HTTPRequest request = new RequestBuilder().build(HTTPMethod.GET, url, params, null)

        then:
        "http://ws.org/services/users/123" ==  request.url.toString()
    }

    def "path with no beginning slash and url with no trailing slash"() {
        given:
        def url =  "http://ws.org/services"
        def params = [path: "users/123"]

        when:
        HTTPRequest request = new RequestBuilder().build(HTTPMethod.GET, url, params, null)

        then:
        "http://ws.org/services/users/123" ==  request.url.toString()
    }

    def "map to querystring"() {
        given:
        def url =  "http://ws.org/services"
        def params = [path: "/users", query:[deptid:"6900", managerid:"123"]]

        when:
        HTTPRequest request = new RequestBuilder().build(HTTPMethod.GET, url, params, null)

        then:
        "http://ws.org/services/users?deptid=6900&managerid=123" == request.url.toString()
    }

    def "map to querystring with encoded strings"() {
        given:
        def url =  "http://ws.org/services"
        def params = [path: "/users", query:["hire_date":"06/19/2009", homepage:"http://geocities.com/users/jansmith"]]

        when:
        HTTPRequest request = new RequestBuilder().build(HTTPMethod.GET, url, params, null)

        then:
        "http://ws.org/services/users?hire_date=06%2F19%2F2009&homepage=http%3A%2F%2Fgeocities.com%2Fusers%2Fjansmith" == request.url.toString()
    }

    def "headers added to request"() {
        given:
        def url =  "http://ws.org/services"
        def params = [headers:["Accept":"text/plain", "X-Foo": "123"]]

        when:
        HTTPRequest request = new RequestBuilder().build(HTTPMethod.GET, url, params, null)

        then:
        "text/plain" == request.headers.Accept
        "123" == request.headers."X-Foo"
    }

    def "sets http connection parameters"() {
        given:
        def url =  "http://ws.org/services"
        def params = [path: "/foo", readTimeout:9876, sslTrustAllCerts:false]

        when:
        HTTPRequest request = new RequestBuilder().build(HTTPMethod.GET, url, params, null)

        then:
        9876 == request.readTimeout
        !request.sslTrustAllCerts
    }

    def "original params not modified"() {
        given:
        def url =  "http://ws.org/services"
        def params = [path: "/users/123", readTimeout:9876, sslTrustAllCerts:false]

        when:
        HTTPRequest request = new RequestBuilder().build(HTTPMethod.GET, url, params, null)
        params.remove("path")

        then:
        null == params.path
        "http://ws.org/services/users/123" == request.url.toString()
    }

    def "accept parameter can be set using enum or string"() {
        expect:
        def response = new RequestBuilder().build(HTTPMethod.GET, "http://ws.org/services", [accept:accept], null)
        response.headers.Accept == contentType

        where:
        accept                | contentType
        ContentType.XML       | ContentType.XML.getAcceptHeader()
        "text/plain"          | "text/plain"
        "${ContentType.JSON}" | ContentType.JSON.toString()
    }
}
