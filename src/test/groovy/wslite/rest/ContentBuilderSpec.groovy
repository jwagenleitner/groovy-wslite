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

class ContentBuilderSpec extends Specification {

    void 'default content type and charset'() {
        when:
        def builder = new ContentBuilder('text/plain', 'UTF-8').build {}

        then:
        'text/plain' == builder.contentType
        'UTF-8' == builder.charset
        'text/plain; charset=UTF-8' == builder.getContentTypeHeader()
    }

    void 'type overrides content type default'() {
        when:
        def builder = new ContentBuilder('text/plain', 'UTF-8').build {
            type 'application/xml'
        }

        then:
        'application/xml' == builder.contentType
    }

    void 'charset overrides charset default'() {
        when:
        def builder = new ContentBuilder('text/plain', 'UTF-8').build {
            charset 'ISO-8859-1'
        }

        then:
        'text/plain' == builder.contentType
        'ISO-8859-1' == builder.charset
        'text/plain; charset=ISO-8859-1' == builder.getContentTypeHeader()
    }

    void 'bytes'() {
        when:
        def builder = new ContentBuilder('text/plain', 'UTF-8').build {
            bytes 'foo'.getBytes('UTF-8')
        }

        then:
        'foo'.getBytes('UTF-8') == builder.getData()
    }

    void 'text'() {
        when:
        def builder = new ContentBuilder('text/plain', 'UTF-8').build {
            text 'foo'
        }

        then:
        'foo' == new String(builder.getData(), builder.charset)
    }

    void 'urlenc'() {
        when:
        def builder = new ContentBuilder('text/plain', 'UTF-8').build {
            urlenc foo: 'bar', q: ['one', 'two']
        }

        then:
        'foo=bar&q=one&q=two' == new String(builder.getData(), builder.charset)
    }

    void 'xml'() {
        when:
        def builder = new ContentBuilder('text/plain', 'UTF-8').build {
            xml {
                root()
            }
        }

        then:
        '<root/>' == new String(builder.getData(), builder.charset)
    }

    void 'json'() {
        when:
        def builder = new ContentBuilder('text/plain', 'UTF-8').build {
            json employee: [job: [title: 'Nuclear Technician', department: 'Sector 7g']]
        }

        then:
        '''{"employee":{"job":{"title":"Nuclear Technician","department":"Sector 7g"}}}''' == new String(builder.getData(), builder.charset)
    }

    void 'no default charset'() {
        when:
        def builder = new ContentBuilder('text/plain', null).build {
            xml {
                root()
            }
        }

        then:
        "text/plain; charset=${HTTP.DEFAULT_CHARSET}" == builder.getContentTypeHeader()
    }

    void 'guesses content type for bytes'() {
        when:
        def builder = new ContentBuilder(null, null).build {
            bytes 'foo'.bytes
        }
        ContentTypeHeader contentTypeHeader = new ContentTypeHeader(builder.getContentTypeHeader())

        then:
        ContentType.BINARY.toString() == contentTypeHeader.mediaType
    }

    void 'guesses content type for text'() {
        when:
        def builder = new ContentBuilder(null, null).build {
            text 'foo'
        }
        ContentTypeHeader contentTypeHeader = new ContentTypeHeader(builder.getContentTypeHeader())

        then:
        ContentType.TEXT.toString() == contentTypeHeader.mediaType
    }

    void 'guesses content type for urlenc'() {
        when:
        def builder = new ContentBuilder(null, null).build {
            urlenc 'foo': 'bar'
        }
        ContentTypeHeader contentTypeHeader = new ContentTypeHeader(builder.getContentTypeHeader())

        then:
        ContentType.URLENC.toString() == contentTypeHeader.mediaType
    }

    void 'guesses content type for xml'() {
        when:
        def builder = new ContentBuilder(null, null).build {
            xml {
                foo()
            }
        }
        ContentTypeHeader contentTypeHeader = new ContentTypeHeader(builder.getContentTypeHeader())

        then:
        ContentType.XML.toString() == contentTypeHeader.mediaType
    }

    void 'guesses content type for json'() {
        when:
        def builder = new ContentBuilder(null, null).build {
            json id: '12345', department: 'Finance'
        }
        ContentTypeHeader contentTypeHeader = new ContentTypeHeader(builder.getContentTypeHeader())

        then:
        ContentType.JSON.toString() == contentTypeHeader.mediaType
    }

}
