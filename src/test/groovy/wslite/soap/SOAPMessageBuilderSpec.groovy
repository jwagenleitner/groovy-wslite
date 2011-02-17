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
package wslite.soap

import spock.lang.*

class SOAPMessageBuilderSpec extends Specification {

    def "default SOAP version is 1.1"() {
        given:"a closure with no version defined"
        def message = {
            body {}
        }

        when:"message is built"
        def messageBuilder = new SOAPMessageBuilder()
        message.delegate = messageBuilder
        message.call()

        then:"version should default to 1.1"
        assert SOAPVersion.V1_1 == messageBuilder.version
    }

    def "overriding SOAP version"() {
        given:"a closure that sets the SOAP version to 1.2"
        def message = {
            version SOAPVersion.V1_2
        }

        when:"message is built"
        def messageBuilder = new SOAPMessageBuilder()
        message.delegate = messageBuilder
        message.call()

        then:"version should be set to 1.2"
        assert SOAPVersion.V1_2 == messageBuilder.version
    }

}
