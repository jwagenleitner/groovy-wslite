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

class JSONArraySpec extends Specification {

    void 'should be a List'() {
        expect:
        new JSONArray() instanceof List
    }

    void 'should answer to the Groovy truth'() {
        given:
        JSONArray json1 = new JSONArray()
        JSONArray json2 = new JSONArray('''["foo","bar"]''')
        JSONArray json3 = new JSONArray('[]')

        expect:
        !json1
        json2
        !json3
    }

    void 'can be constructed from a GString'() {
        given:
        def someVar = 'GStrings are cool'

        when:
        JSONArray result = new JSONArray("""["foo", "Bar says ${someVar}"]""")

        then:
        'Bar says GStrings are cool' == result[1]
        '''["foo","Bar says GStrings are cool"]''' == result.toString()
    }

    void 'can be constructed from a List with GString as a value'() {
        given:
        def someVar = 'bar'
        def someList = ['foo', "foo is ${someVar}"]

        when:
        JSONArray result = new JSONArray(someList)

        then:
        'foo is bar' == result[1]
        '''["foo","foo is bar"]''' == result.toString()
    }

}
