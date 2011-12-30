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
                root() {
                    foo('bar')
                }
            }
        }

        then:
        def xml = new XmlSlurper().parseText(new String(builder.getData(), builder.charset))
        'bar' == xml.foo.text()
    }

    void 'json'() {
        when:
        def builder = new ContentBuilder('text/plain', 'UTF-8').build {
            json employee: [job: [title: 'Nuclear Technician', department: 'Sector 7g']]
        }

        then:
        def json = new JSONObject(new String(builder.getData(), builder.charset))
        'Nuclear Technician' == json.employee.job.title
        'Sector 7g' == json.employee.job.department
    }

    void 'will use the http 1.1 default charset if none is specified'() {
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

    @Issue('https://github.com/jwagenleitner/groovy-wslite/issues/33')
    void 'json content using GStrings should work with JSON object'() {
        given:
        def value = "a"

        when:
        def builder = new ContentBuilder(ContentType.JSON.toString(), 'UTF-8').build {
            json param: "${value}"
        }

        then:
        def json = new JSONObject(new String(builder.getData(), 'UTF-8'))
        'a' == json.param
    }

    @Issue('https://github.com/jwagenleitner/groovy-wslite/issues/33')
    void 'json content using GStrings should work with JSON array'() {
        given:
        def value1 = "a"
        def value2 = "b"

        when:
        def builder = new ContentBuilder(ContentType.JSON.toString(), 'UTF-8').build {
            json(["${value1}", "${value2}"])
        }

        then:
        def json = new JSONArray(new String(builder.getData(), 'UTF-8'))
        'a' == json[0]
        'b' == json[1]
    }

    void 'xml nodes with same name as builder methods should work'() {
        given:
        def xmlns = 'http://hr.org'
        def data = 'b'

        when:
        def builder = new ContentBuilder(ContentType.XML.toString(), 'UTF-8').build {
            xml {
                PersonRequest(xmlns: xmlns, foo: 'bar') {
                    text('foo')
                    text2(data)
                }
            }
        }

        then:
        def xml = new XmlSlurper().parseText(new String(builder.getData(), 'UTF-8'))
        'foo' == xml.text.text()
        'b' == xml.text2.text()
        'PersonRequest' == xml.name()
        'bar' == xml.@foo.text()
    }

    void 'xml can be created from a Closure'() {
        given:
        def someXml = {
            HolidayRequest {
                type('Vacation')
            }
        }

        when:
        def builder = new ContentBuilder(null, 'UTF-8').build {
            xml someXml
        }

        then:
        def xml = new XmlSlurper().parseText(new String(builder.getData(), 'UTF-8'))
        'Vacation' == xml.type.text()
    }

    void 'json using property with same name as builder methods should work'() {
        given:
        def someVar = 'a'
        def data = 'b'

        when:
        def builder = new ContentBuilder(ContentType.JSON.toString(), 'UTF-8').build {
            json text: someVar, title: data
        }

        then:
        def json = new JSONObject(new String(builder.getData(), 'UTF-8'))
        'a' == json.text
        'b' == json.title
    }

    void 'text using property with same name as builder methods should work'() {
        given:
        def data = 'a'

        when:
        def builder = new ContentBuilder(null, 'UTF-8').build {
            text data
        }

        then:
        '''a''' == new String(builder.getData(), 'UTF-8')
    }

    void 'urlenc using property with same name as builder methods should work'() {
        given:
        def data = 'a'

        when:
        def builder = new ContentBuilder(null, 'UTF-8').build {
            urlenc data: data
        }

        then:
        '''data=a''' == new String(builder.getData(), 'UTF-8')
    }

    void 'empty content'() {
        when:
        def builderBytes = new ContentBuilder(null, 'UTF-8').build { bytes() }
        def builderText = new ContentBuilder(null, 'UTF-8').build { text() }
        def builderUrlenc = new ContentBuilder(null, 'UTF-8').build { urlenc() }
        def builderXml = new ContentBuilder(null, 'UTF-8').build { xml() }
        def builderJson = new ContentBuilder(null, 'UTF-8').build { json() }

        then:
        null == builderBytes.getData()
        null == builderText.getData()
        null == builderUrlenc.getData()
        null == builderXml.getData()
        null == builderJson.getData()
    }

}
