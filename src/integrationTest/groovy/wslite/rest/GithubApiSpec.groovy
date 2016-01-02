/* Copyright 2011-2014 the original author or authors.
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
import wslite.http.HTTP
import wslite.http.auth.HTTPBasicAuthorization

/**
 * @author John Wagenleitner
 */
class GithubApiSpec extends Specification {

    RESTClient github = new RESTClient('https://api.github.com/')

    String gistId = 'f07dc306c7798a3fe9ef'


    void 'get user'() {
        when:
        def response = github.get(path: 'users/jwagenleitner')

        then:
        'jwagenleitner' == response.json.login
    }

    void 'get a wslite issue'() {
        when:
        def response = github.get(path: 'repos/jwagenleitner/groovy-wslite/issues/76')

        then:
        'closed' == response.json.state
        'jwagenleitner' == response.json.closed_by.login
        'Parsing of Content-Type containing \'charset\' parameter.' == response.json.title
    }

    void 'get a public gist'() {
		given:
		String username = System.getProperty('github.username', 'GUEST')
        String password = System.getProperty('github.password', 'GUEST')
        when:
		github.authorization = new HTTPBasicAuthorization(username, password)
        def response = github.get(path: "gists/${gistId}")

        then:
        true == response.json.public
        "jwagenleitner" == response.json.owner.login
    }

    void 'edit a public gist'() {
        given:
        String testToken = new Date()
        String username = System.getProperty('github.username', 'GUEST')
        String password = System.getProperty('github.password', 'GUEST')

        when:
        github.authorization = new HTTPBasicAuthorization(username, password)
        def response = github.patch(path: "gists/${gistId}") {
            type ContentType.JSON
            json files: ['spec_test.txt':[content: testToken]]
        }

        then:
        200 == response.statusCode
        testToken == response.json.files.'spec_test.txt'.content
    }

}
