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

class TwitterApiSpec extends Specification {

    RESTClient twitterV1 = new RESTClient('http://api.twitter.com/1/')

    @See('https://dev.twitter.com/docs/api/1/get/statuses/mentions')
    void 'get mentions fails without auth'() {
        when:
        def response = twitterV1.get(path: '/statuses/mentions.json')

        then:
        def ex = thrown(RESTClientException)
        403 == ex.response.statusCode
    }

}
