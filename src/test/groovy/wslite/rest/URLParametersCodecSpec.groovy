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

class URLParametersCodecSpec extends Specification {

    void 'encodes a map to a url encoded string'() {
        expect:
        urlencoded == new URLParametersCodec().encode(map)

        where:
        map                                 | urlencoded
        [q:"test"]                          | "q=test"
        [q:["test1", "test2"], foo:"bar"]   | "q=test1&q=test2&foo=bar"
        [path:"/hr/departments"]            | "path=%2Fhr%2Fdepartments"
        [title:"Homer & Sally"]             | "title=Homer+%26+Sally"
    }

    void 'decodes a url encoded string to a map'() {
        expect:
        map == new URLParametersCodec().decode(urlencoded)

        where:
        map                                 | urlencoded
        [q:"test"]                          | "q=test"
        [q:["test1", "test2"], foo:"bar"]   | "q=test1&q=test2&foo=bar"
        [path:"/hr/departments"]            | "path=%2Fhr%2Fdepartments"
        [title:"Homer & Sally"]             | "title=Homer+%26+Sally"
    }

}
