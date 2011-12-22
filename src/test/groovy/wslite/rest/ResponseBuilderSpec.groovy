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

class ResponseBuilderSpec extends Specification {

    HTTPResponse httpResponse = new HTTPResponse()
    
    void 'text response sets text property'() {
        given:
        httpResponse.contentType = 'text/plain'
        httpResponse.data = 'foo'.bytes

        when:
        def response = new ResponseBuilder().build(null, httpResponse)

        then:
        'foo' == response.text
        null == response.xml
        null == response.json
    }

    void 'xml response sets text property and xml property'() {
        given:
        httpResponse.contentType = 'application/xml'
        httpResponse.data = '<foo><name>bar</name></foo>'.bytes

        when:
        def response = new ResponseBuilder().build(null, httpResponse)

        then:
        '<foo><name>bar</name></foo>' == response.text
        'bar' == response.xml.name.text()
        null == response.json
    }

    void 'json response sets text property and json property'() {
        given:
        httpResponse.contentType = 'application/json'
        httpResponse.data = '{foo:{name: "bar"}}'.bytes

        when:
        def response = new ResponseBuilder().build(null, httpResponse)

        then:
        '{foo:{name: "bar"}}' == response.text
        'bar' == response.json.foo.name
        null == response.xml
    }

    void 'handles text responses with no content'() {
        given:
        httpResponse.contentType = 'text/plain'
        httpResponse.data = null

        when:
        def response = new ResponseBuilder().build(null, httpResponse)

        then:
        '' == response.text
    }

    void 'throws exception when content-type is xml and there is no content'() {
        given:
        httpResponse.contentType = 'text/xml'
        httpResponse.data = null

        when:
        def response = new ResponseBuilder().build(null, httpResponse)

        then:
        thrown(Exception)
    }

    void 'throws exception when content-type is json and there is no content'() {
        given:
        httpResponse.contentType = 'application/json'
        httpResponse.data = null

        when:
        def response = new ResponseBuilder().build(null, httpResponse)

        then:
        def ex = thrown(Exception)
    }

    void 'throws exception when content-type is xml and content contains invalid markup'() {
        given:
        httpResponse.contentType = 'text/xml'
        httpResponse.data = '<html><body>Error<br></body></html>'.bytes

        when:
        def response = new ResponseBuilder().build(null, httpResponse)

        then:
        thrown(Exception)
    }

    void 'throws exception when content-type is json and content contains invalid json'() {
        given:
        httpResponse.contentType = 'application/json'
        httpResponse.data = '<html><body>Error<br></body></html>'.bytes

        when:
        def response = new ResponseBuilder().build(null, httpResponse)

        then:
        thrown(Exception)
    }
}
