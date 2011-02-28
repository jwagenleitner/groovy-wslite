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

class HTTPHeaderMapSpec extends Specification {

    def "retain original case"() {
        when:
        def h = new HTTPHeaderMap("Content-Type":"text/xml")
        h.Accept = "text/xml"
        h["cOnTeNt-EnCodInG"] = "dummified"

        then:
        h.keySet().find { it == "Content-Type" }
        h.keySet().find { it == "Accept" }
        h.keySet().find { it == "cOnTeNt-EnCodInG" }
    }

    def "check existence of keys case insensitively"(){
        when:
        def h = new HTTPHeaderMap("Content-Type":"text/xml")

        then:
        "text/xml" == h["content-type"]
        "text/xml" == h."content-type"
        "text/xml" == h.get("content-type")
        1 == h.size()
    }

    def "overwrite case insensitively and assume the new key's case"() {
        when:
        def h = new HTTPHeaderMap("Content-Type":"text/xml")
        h."content-TYPE" = "text/javascript"

        then:
        1 == h.size()
        "text/javascript" == h."CONTENT-TYPE"
        h.keySet().find { it == "content-TYPE" }
    }

    def "be an instance of a Map"() {
        when:
        def h = new HTTPHeaderMap("Content-Type":"text/xml")

        then:
        h instanceof Map
    }

    def "be able to delete the given key case-sensitively"() {
        when:
        def h = new HTTPHeaderMap("Content-Type":"text/xml")
        h.remove("content-type")

        then:
        0 == h.size()
        null == h."Content-Type"
        null == h."content-type"
    }

    def "returns the removed value"() {
        when:
        def h = new HTTPHeaderMap("Content-Type":"text/xml")

        then:
        "text/xml" == h.remove("content-type")
    }

    def "return null when remove is called on a non-existent key"() {
        when:
        def h = new HTTPHeaderMap("Content-Type":"text/xml")

        then:
        null == h.remove("content-disposition")
        1 == h.size()
    }

    def "not create headers out of thin air"() {
        when:
        def h = new HTTPHeaderMap()
        h."Content-Type"

        then:
        null == h."Content-Type"
        0 == h.size()
    }

    def "each closure should contain original case"() {
        when:
        def h = new HTTPHeaderMap("Content-Type":"text/xml", "Foo":"bar")

        then:
        h.each { k, v ->
            assert k in ["Content-Type", "Foo"]
        }
    }

    def "able to see if contains key case-insensitively"() {
        when:
        def h = new HTTPHeaderMap("Content-Type":"text/xml", "Foo":"bar")

        then:
        h.containsKey("fOO")
        h.containsKey("CONTENT-type")
    }

    def "able to see if contains value"() {
        when:
        def h = new HTTPHeaderMap("Content-Type":"text/xml", "Foo":"bar")

        then:
        h.containsValue("bar")
    }

    def "putting map with different case keys will overwrite existing keys"() {
        when:
        def h = new HTTPHeaderMap("Content-Type":"text/xml", "Foo":"bar")
        h.putAll(["fOO":"baz", "Accept":"text/json"])

        then:
        3 == h.size()
        "baz" == h.foo
        !h.keySet().find { it == "Foo" }
    }

    def "able to make a clone of existing Map"() {
        when:
        def m = [foo:"bar", seven:"eight"]
        def h = new HTTPHeaderMap(m)
        h.remove("FOO")
        h.homer = "simposon"
        h.barney = "gumble"

        then:
        2 == m.size()
        3 == h.size()
        null == h.foo
        "bar" == m.foo
    }

    def "able to put a new value in empty Map"() {
        when:
        def h = new HTTPHeaderMap()
        h.put("foo", "bar")

        then:
        1 == h.size()
    }
}
