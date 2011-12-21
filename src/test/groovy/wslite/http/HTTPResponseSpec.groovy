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

    void 'header keys are case insensitive'() {
        when:
        def resp = new HTTPResponse(headers:['Content-Type': 'text/xml',
                                             Accept: 'text/javascript'])

        then:
        'text/xml' == resp.headers.'content-type'
        'text/javascript' == resp.headers['accEPT']
    }

    void 'headers are not modifiable once assigned'() {
        when:
        def resp = new HTTPResponse(headers:['Content-Type': 'text/xml',
                                             Accept: 'text/javascript'])
        def h = resp.headers
        h.newKey = 'newVal'

        then:
        thrown(UnsupportedOperationException)
        2 == resp.headers.size()
        null == resp.headers.newKey
    }

}
