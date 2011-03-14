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
        def builder = new RequestBuilder(null, "http://ws.org", null, null)
        builder.build()

        then: thrown(IllegalStateException)
    }

    def "requires a url"() {
        when:
        def builder = new RequestBuilder(HTTPMethod.GET, null, null, null)
        builder.build()

        then: thrown(IllegalStateException)
    }

    def "adding path to url"() {
        given:
        def params = [path: "/users/123"]

        when:
        def builder = new RequestBuilder(HTTPMethod.GET, "http://ws.org/services", params, null)
        HTTPRequest request = builder.build()

        then:
        "http://ws.org/services/users/123" ==  request.url.toString()
    }

    def "path with slash and url with trailing slash"() {
        given:
        def url =  "http://ws.org/services/"
        def params = [path: "/users/123"]

        when:
        def builder = new RequestBuilder(HTTPMethod.GET, url, params, null)
        HTTPRequest request = builder.build()

        then:
        "http://ws.org/services/users/123" ==  request.url.toString()
    }

    def "path with no beginning slash and url with no trailing slash"() {
        given:
        def url =  "http://ws.org/services"
        def params = [path: "users/123"]

        when:
        def builder = new RequestBuilder(HTTPMethod.GET, url, params, null)
        HTTPRequest request = builder.build()

        then:
        "http://ws.org/services/users/123" ==  request.url.toString()
    }

    def "map to querystring"() {
        given:
        def url =  "http://ws.org/services"
        def params = [path: "/users", query:[deptid:"6900", managerid:"123"]]

        when:
        def builder = new RequestBuilder(HTTPMethod.GET, url, params, null)
        HTTPRequest request = builder.build()

        then:
        "http://ws.org/services/users?deptid=6900&managerid=123" == request.url.toString()
    }

    def "map to querystring with encoded strings"() {
        given:
        def url =  "http://ws.org/services"
        def params = [path: "/users", query:["hire_date":"06/19/2009", homepage:"http://geocities.com/users/jansmith"]]

        when:
        def builder = new RequestBuilder(HTTPMethod.GET, url, params, null)
        HTTPRequest request = builder.build()

        then:
        "http://ws.org/services/users?hire_date=06%2F19%2F2009&homepage=http%3A%2F%2Fgeocities.com%2Fusers%2Fjansmith" == request.url.toString()
    }

    def "headers added to request"() {
        given:
        def url =  "http://ws.org/services"
        def params = [headers:["Accept":"text/plain", "X-Foo": "123"]]

        when:
        def builder = new RequestBuilder(HTTPMethod.GET, url, params, null)
        HTTPRequest request = builder.build()

        then:
        "text/plain" == request.headers.Accept
        "123" == request.headers."X-Foo"
    }

    def "sets http connection parameters"() {
        given:
        def url =  "http://ws.org/services"
        def params = [connectionParams:[readTimeout:9876, trustAllSSLCerts:false]]

        when:
        def builder = new RequestBuilder(HTTPMethod.GET, url, params, null)
        HTTPRequest request = builder.build()

        then:
        9876 == request.readTimeout
        !request.trustAllSSLCerts
    }

    def "original params not modified"() {
        given:
        def url =  "http://ws.org/services"
        def params = [path: "/users/123", connectionParams:[readTimeout:9876, trustAllSSLCerts:false]]

        when:
        def builder = new RequestBuilder(HTTPMethod.GET, url, params, null)
        params.remove("path")
        HTTPRequest request = builder.build()

        then:
        null == params.path
        "http://ws.org/services/users/123" == request.url.toString()
    }

    def "accept parameter can be set using enum or string"() {
        expect:
        def builder = new RequestBuilder(HTTPMethod.GET, "http://ws.org/services", [accept:accept], null)
        builder.build()
        builder.headers.Accept == contentType

        where:
        accept                | contentType
        ContentType.XML       | ContentType.XML.getAcceptHeader()
        "text/plain"          | "text/plain"
        "${ContentType.JSON}" | ContentType.JSON.toString()
    }
}
