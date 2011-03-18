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

class ContentBuilderSpec extends Specification {

    def getBuilder(String contentType, String charset, Closure content) {
        def contentBuilder = new ContentBuilder(contentType, charset)
        content.resolveStrategy = Closure.DELEGATE_FIRST
        content.delegate = contentBuilder
        content.call()
        return contentBuilder
    }

    def getBuilder(Closure content) {
        return getBuilder(null, null, content)
    }

    def "default content type and charset"() {
        when:
        def builder = getBuilder("text/plain", "UTF-8") {}

        then:
        "text/plain" == builder.contentType
        "UTF-8" == builder.charset
        "text/plain; charset=UTF-8" == builder.getContentTypeHeader()
    }

    def "type overrides content type default"() {
        when:
        def builder = getBuilder("text/plain", "UTF-8") {
            type "application/xml"
        }

        then:
        "application/xml" == builder.contentType
    }

    def "charset overrides charset default"() {
        when:
        def builder = getBuilder("text/plain", "UTF-8") {
            charset "ISO-8859-1"
        }

        then:
        "text/plain" == builder.contentType
        "ISO-8859-1" == builder.charset
        "text/plain; charset=ISO-8859-1" == builder.getContentTypeHeader()
    }

    def "bytes"() {
        when:
        def builder = getBuilder("text/plain", "UTF-8") {
            bytes "foo".getBytes("UTF-8")
        }

        then:
        "foo".getBytes("UTF-8") == builder.getData()
    }

    def "text"() {
        when:
        def builder = getBuilder("text/plain", "UTF-8") {
            text "foo"
        }

        then:
        "foo" == new String(builder.getData(), builder.charset)
    }

    def "urlenc"() {
        when:
        def builder = getBuilder("text/plain", "UTF-8") {
            urlenc foo:"bar", q:["one", "two"]
        }

        then:
        "foo=bar&q=one&q=two" == new String(builder.getData(), builder.charset)
    }

    def "xml"() {
        when:
        def builder = getBuilder("text/plain", "UTF-8") {
            xml {
                root()
            }
        }

        then:
        "<root/>" == new String(builder.getData(), builder.charset)
    }

}
