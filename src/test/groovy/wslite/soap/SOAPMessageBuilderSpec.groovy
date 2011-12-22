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

    void 'default SOAP version is 1.1'() {
        when: 'a message with no version is built'
        def message = new SOAPMessageBuilder().build {
            body {}
        }

        then: 'version should default to 1.1'
        assert SOAPVersion.V1_1 == message.version
    }

    void 'overriding SOAP version'() {
        when: 'a message specifies v1.2 is built'
        def message = new SOAPMessageBuilder().build {
            version SOAPVersion.V1_2
        }

        then: 'version should be set to 1.2'
        assert SOAPVersion.V1_2 == message.version
    }

    void 'default SOAP namespace prefix'() {
        when: 'a message does not specify a SOAP namespace prefix'
        def message = new SOAPMessageBuilder().build {
            body {}
        }

        then: 'SOAP namespace prefix should default to SOAP'
        assert message.toString().contains("<${SOAP.SOAP_NS_PREFIX}:Envelope")
    }

    void 'overriding SOAP namespace prefix'() {
        when: 'a message specifies an alternative SOAP namespace prefix'
        def message = new SOAPMessageBuilder().build {
            soapNamespacePrefix 'FOOBAR'
            body {}
        }

        then:
        assert message.toString().contains('<FOOBAR:Envelope')
    }

    void 'custom envelope attributes'() {
        when: 'custom envelope attributes are speicified'
        def message = new SOAPMessageBuilder().build {
            envelopeAttributes foo: 'bar'
            body {}
        }

        then:
        def env = slurp(message.toString())
        assert env.@foo.text() == 'bar'
    }

    @Issue('https://github.com/jwagenleitner/groovy-wslite/issues/30')
    void 'can handle nested header and body tags'() {
        when: 'message body has nested header or body elements'
        def message = new SOAPMessageBuilder().build {
            body {
                ejecutar( 'xmlns:mns': 'urn:servicioFrontera') {
                    xmlOrder() {
                        order {
                            header { }
                            body { }
                            foo {
                              orderName('bar')
                            }
                        }
                    }
                }
            }
        }

        and: 'message body with nested header or body tags'
        def messageBody = new SOAPMessageBuilder().build {
            body {
                body {
                    header { }
                }

            }
        }

        and: 'mesage header with nested header or body tags'
        def messageHeader = new SOAPMessageBuilder().build {
            header {
                header {
                    body { }
                }
            }
        }

        then:
        def env = slurp(message.toString())
        assert !env.Body.ejecutar.xmlOrder.order.header.isEmpty()
        assert !env.Body.ejecutar.xmlOrder.order.body.isEmpty()
        assert !env.Body.ejecutar.xmlOrder.order.foo.isEmpty()

        and:
        def envBody = slurp(messageBody.toString())
        assert !envBody.Body.body.isEmpty()
        assert !envBody.Body.body.header.isEmpty()

        and:
        def envHeader = slurp(messageHeader.toString())
        assert !envHeader.Header.header.isEmpty()
        assert !envHeader.Header.header.body.isEmpty()
    }

    private slurp(message) {
        return new XmlSlurper().parseText(message)
    }

}
