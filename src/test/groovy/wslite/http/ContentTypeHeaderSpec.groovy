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

class ContentTypeHeaderSpec extends Specification {

    void 'can get media type from Content-Type header'() {
        expect:
        mediaType == new ContentTypeHeader(contentType).mediaType

        where:
        mediaType                       | contentType
        'application/xml'               | 'application/xml'
        'application/xml'               | 'application/xml;charset=UTF-8'
        'application/soap+xml'          | 'application/soap+xml; charset=UTF-8'
        'application/vnd.json+xml'      | 'application/vnd.json+xml'
        'application/soap+xml'          | 'application/soap+xml; action="urn:echoResponse";charset=UTF-16'
        null                            | null
    }

    void 'can get charset from Content-Type header string'() {
        expect:
        charset == new ContentTypeHeader(contentType).charset

        where:
        charset             | contentType
        'utf-8'             | '''text/xml; charset="utf-8"; action="some action"'''
        'ISO-8859-1'        | '''application/xml; action="some action"; charset=ISO-8859-1'''
        'ISO-8859-1'        | '''application/xml; action="some action"; charset=ISO-8859-1; boundary="x-fence"'''
        'utf-8'             | '''text/xml; CHARSET=utf-8;'''
        'UTF-8'             | 'application/xml;charset=UTF-8'
        'UTF-8'             | 'application/soap+xml; charset=UTF-8'
        'UTF-8'             | 'application/xml;   charset=UTF-8'
        'UTF-8'             | 'application/xml;   charset=UTF-8  '
        'UTF-8'             | 'application/xml;   charset=UTF-8 mode=RW'
        'UTF-16'            | 'application/soap+xml; charset=UTF-16;action="urn:echoResponse"'
        'UTF-8'             | '''text/xml;
    charset=UTF-8; boundary="mime"'''
        null                | null
        null                | 'application/xml'
        null                | 'application/vnd.json+xml'
    }

}
