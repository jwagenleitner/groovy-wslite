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

    def buildMessage(message) {
        def messageBuilder = new SOAPMessageBuilder()
        message.delegate = messageBuilder
        message.call()
        return messageBuilder
    }

    def slurp(message) {
        return new XmlSlurper().parseText(message)
    }

    def "default SOAP version is 1.1"() {
        when:"a message with no version is built"
        def message = buildMessage {
            body {}
        }

        then:"version should default to 1.1"
        assert SOAPVersion.V1_1 == message.version
    }

    def "overriding SOAP version"() {
        when:"a message specifies v1.2 is built"
        def message = buildMessage {
            version SOAPVersion.V1_2
        }

        then:"version should be set to 1.2"
        assert SOAPVersion.V1_2 == message.version
    }

    def "default SOAP namespace prefix"() {
        when:"a message doesn't specify a SOAP namespace prefix"
        def message = buildMessage {
            body {}
        }

        then:"SOAP namespace prefix should default to SOAP"
        assert message.toString().contains("<SOAP:Envelope")
    }

    def "overriding SOAP namespace prefix"() {
        when:"a message specifies an alternative SOAP namespace prefix"
        def message = buildMessage {
            soapNamespacePrefix "FOOBAR"
            body {}
        }

        then:
        assert message.toString().contains("<FOOBAR:Envelope")
    }

    def "custom envelope attributes"() {
        when:"custom envelope attributes are speicified"
        def message = buildMessage {
            envelopeAttributes foo:'bar'
            body {}
        }

        then:
        def env = slurp(message.toString())
        assert env.@foo.text() == "bar"
    }

}
