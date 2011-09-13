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

class HTTPSpec extends Specification {

    def "parses mime type from content-type header"() {
        expect:
        mimeType == HTTP.parseMimeTypeFromContentType(contentType)

        where:
        contentType                             | mimeType
        "application/xml"                       | "application/xml"
        "application/xml;charset=UTF-8"         | "application/xml"
        "application/soap+xml; charset=UTF-8"   | "application/soap+xml"
        "application/vnd.json+xml"              | "application/vnd.json+xml"
//        'application/soap+xml; action="urn:echoResponse";charset=UTF-16' | "application/soap+xml"
    }

    def "parses charset from content-type header"() {
        expect:
        charset == HTTP.parseCharsetParamFromContentType(contentType)

        where:
        contentType                                     | charset
        "application/xml"                               | null
        "application/xml;charset=UTF-8"                 | "UTF-8"
        "application/soap+xml; charset=UTF-8"           | "UTF-8"
        "application/vnd.json+xml"                      | null
        "application/xml;   charset=UTF-8"              | "UTF-8"
        "application/xml;   charset=UTF-8  "            | "UTF-8"
        "application/xml;   charset=UTF-8 mode=RW"      | "UTF-8"
//        'application/soap+xml; charset=UTF-16;action="urn:echoResponse"' | "UTF-16"
    }

    def "parses map to url encoded string"() {
        expect:
        urlencoded == HTTP.mapToURLEncodedString(map)

        where:
        map                                 | urlencoded
        [q:"test"]                          | "q=test"
        [q:["test1", "test2"], foo:"bar"]   | "q=test1&q=test2&foo=bar"
        [path:"/hr/departments"]            | "path=%2Fhr%2Fdepartments"
        [title:"Homer & Sally"]             | "title=Homer+%26+Sally"
    }

    def "parses urlencoded string to map"() {
        expect:
        map == HTTP.urlEncodedStringToMap(urlencoded)

        where:
        map                                 | urlencoded
        [q:"test"]                          | "q=test"
        [q:["test1", "test2"], foo:"bar"]   | "q=test1&q=test2&foo=bar"
        [path:"/hr/departments"]            | "path=%2Fhr%2Fdepartments"
        [title:"Homer & Sally"]             | "title=Homer+%26+Sally"
    }

    def "parses charset from content-type header and removes invalid characters"() {
        expect:
        charset == HTTP.parseCharsetParamFromContentType(contentType)

        // See http://download.oracle.com/javase/6/docs/api/java/nio/charset/Charset.html
        // for list of valid characters in a Charset name
        where:
        contentType                                     | charset
        'text/xml;charset="utf-8"'                      | "utf-8"
        "text/xml;charset='UTF-8'"                      | "UTF-8"
        'text/xml;charset=JIS_X0212-1990'               | "JIS_X0212-1990"
    }
}
