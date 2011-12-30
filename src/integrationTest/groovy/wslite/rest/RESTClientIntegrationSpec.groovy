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

class RESTClientIntegrationSpec extends Specification {

    void 'FresnoStateNews.com'() {
        when:
        def client = new RESTClient('http://www.fresnostatenews.com/feed/')
        def response = client.get()

        then:
        200 == response.statusCode
        'text/xml' == response.contentType
        'UTF-8' == response.charset
        'text/xml; charset=UTF-8' == response.headers."Content-Type"
        'FresnoStateNews.com' == response.xml.channel.title.text()
    }

    void 'Holiday Service get holidays by country, year and month'() {
        given:
        def client = new RESTClient('http://www.holidaywebservice.com/HolidayService_v2/HolidayService2.asmx')

        when:
        def response = client.post(path: '/GetHolidaysForMonth') {
            urlenc countryCode: 'UnitedStates', year: '2011', month: '12'
        }

        then:
        200 == response.statusCode
        'text/xml' == response.contentType
        'Christmas' == response.xml
                .Holiday
                .find { it.HolidayCode.text() == 'CHRISTMAS-ACTUAL'}
                .Descriptor.text()
    }

}
