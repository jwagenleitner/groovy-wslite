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

import wslite.http.HTTP
import wslite.util.ObjectHelper

class HTTPBasicAuthorization implements HTTPAuthorization {

    String username
    String password

    private String authorization

    HTTPBasicAuthorization() { }

    HTTPBasicAuthorization(String username, String password) {
        this.username = username
        this.password = password
    }

    void setUsername(String username) {
        this.username = username
        authorization = null
    }

    void setPassword(String password) {
        this.password = password
        authorization = null
    }

    String getUsername() {
        return username
    }

    String getPassword() {
        return password
    }

    void authorize(conn) {
        conn.addRequestProperty(HTTP.AUTHORIZATION_HEADER, getAuthorization())
    }

    private String getAuthorization() {
        if (!authorization) {
            authorization = 'Basic ' + "${username}:${password}".toString().bytes.encodeBase64()
        }
        return authorization
    }

    @Override
    String toString() {
        ObjectHelper.dump(this, include:['username'])
    }

}
