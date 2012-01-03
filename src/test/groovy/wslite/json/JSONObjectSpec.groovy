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
package wslite.json

import spock.lang.*

class JSONObjectSpec extends Specification {

    void 'should be a Map'() {
        expect:
        new JSONObject() instanceof Map
    }

    void 'should answer to the Groovy truth'() {
        given:
        JSONObject json1 = new JSONObject()
        JSONObject json2 = new JSONObject('''{"foo":"bar"}''')
        JSONObject json3 = new JSONObject('{}')

        expect:
        !json1
        json2
        !json3
    }

    void 'can be constructed from a GString'() {
        given:
        def someVar = 'GStrings are cool'

        when:
        JSONObject result = new JSONObject("""{"foo":"Bar says ${someVar}"}""")

        then:
        'Bar says GStrings are cool' == result.foo
        '''{"foo":"Bar says GStrings are cool"}''' == result.toString()
    }

    void 'can be constructed from a Map with GString as a value'() {
        given:
        def someVar = 'bar'
        def someMap = [foo: "foo is ${someVar}"]

        when:
        JSONObject result = new JSONObject(someMap)

        then:
        'foo is bar' == result.foo
        '''{"foo":"foo is bar"}''' == result.toString()
    }

    void 'can be constructed from a Map with GString as a key'() {
        given:
        def someVar = "foo"
        def someMap = ["${someVar}": 'bar']

        when:
        JSONObject result = new JSONObject(someMap)

        then:
        '''{"foo":"bar"}''' == result.toString()
    }

    void 'map key containing null value will not exist'() {
        given:
        def someMap = [foo: 'bar', baz: null]

        when:
        def result = new JSONObject(someMap)

        then:
        'bar' == result.foo
        null == result.baz
        !result.containsKey('baz')
        1 == result.size()
        '''{"foo":"bar"}''' == result.toString()
    }

    void 'json NULL falseness'() {
        given:
        def answer = JSONObject.NULL ? 'true' : 'false'

        expect:
        !JSONObject.NULL
        'false' == answer
    }

    void 'json NULL missing properties return null'() {
        given:
        JSONObject result = new JSONObject('''{"foo":"bar","baz":null}''')

        expect:
        result.baz instanceof JSONObject.Null
        null == result.baz?.zap
    }

    void 'json NULL equals null'() {
        expect:
        JSONObject.NULL.equals(null)
        // Groovy equals comparison in DefaultTypeTransformation#compareEqual(Object left, Object right)
        // returns false if either argument is a Java null, so unfortunately JSONObject.NULL == null
        // can not work.
        !(JSONObject.NULL == null)
    }

}
