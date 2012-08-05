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

    @See('https://dev.twitter.com/docs/api/1/get/trends/daily')
    void 'daily trends'() {
        when:
        def response = twitterV1.get(path: '/trends/daily.json')

        then:
        200 == response.statusCode
        // The only key of the Trends JSONObject is the datetime.  Since it's not a constant value and it's the only key, we use find.
        1 < response.json.trends.find({true}).value.size()
    }

    @See('https://dev.twitter.com/docs/api/1/get/users/show')
    void 'get user'() {
        when:
        def response = twitterV1.get(path: '/users/show.json',
                query: [screen_name: 'jwagenleitner', include_entities: true])

        then:
        200 == response.statusCode
        'John Wagenleitner' == response.json.name
    }

    @See('https://dev.twitter.com/docs/api/1/get/statuses/public_timeline')
    void 'get public timeline'() {
        when:
        def response = twitterV1.get(path: '/statuses/public_timeline.json')

        then:
        200 == response.statusCode
        1 < response.json.size()
    }

    @See('https://dev.twitter.com/docs/api/1/get/statuses/mentions')
    void 'get mentions fails without auth'() {
        when:
        def response = twitterV1.get(path: '/statuses/mentions.json')

        then:
        def ex = thrown(RESTClientException)
        400 == ex.response.statusCode
    }

}
