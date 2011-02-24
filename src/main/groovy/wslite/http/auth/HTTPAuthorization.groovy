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
package wslite.http.auth

class HTTPAuthorization {

    String authorization

    void basic(UsernamePasswordToken token) {
        basic(token.username, token.password)
    }

    void basic(String username, String password) {
        this.authorization = "Basic " + "${username}:${password}".toString().bytes.encodeBase64()
    }

    void authorize(URLConnection conn) {
        if (!authorization) return
        conn.addRequestProperty("Authorization", authorization)
    }
}
