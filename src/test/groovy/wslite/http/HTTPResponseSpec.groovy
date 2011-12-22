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
package wslite.http

import spock.lang.*

class HTTPResponseSpec extends Specification {
    
    HTTPResponse response = new HTTPResponse()

    void 'header keys are case insensitive'() {
        when:
        response.headers = ['Content-Type': 'text/xml', Accept: 'text/javascript']

        then:
        'text/xml' == response.headers.'content-type'
        'text/javascript' == response.headers['accEPT']
    }

    void 'headers are not modifiable once assigned'() {
        when:
        response.headers = ['Content-Type': 'text/xml',Accept: 'text/javascript']
        response.headers.newKey = 'newVal'

        then:
        thrown(UnsupportedOperationException)
        2 == response.headers.size()
        null == response.headers.newKey
    }

}
