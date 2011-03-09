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

class HTTPBasicAuthorization implements HTTPAuthorization {

    String username
    String password

    private String authorization

    HTTPBasicAuthorization() {

    }

    HTTPBasicAuthorization(String username, String password) {
        this.username = username
        this.password = password
    }

    void setUsername(String username) {
        this.username = username
        this.authorization = null
    }

    void setPassword(String password) {
        this.password = password
        this.authorization = null
    }

    String getUsername() {
        return this.username
    }

    String getPassword() {
        return this.password
    }

    void authorize(conn) {
        conn.addRequestProperty("Authorization", getAuthorization())
    }

    private String getAuthorization() {
        if (!this.authorization) {
            this.authorization = "Basic " + "${username}:${password}".toString().bytes.encodeBase64()
        }
        return this.authorization
    }
}
