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

import wslite.http.*
import wslite.util.ObjectHelper

class Response {

    HTTPRequest request
    @Delegate HTTPResponse response

    private Map parsedResponseContent = [:]

    Response(HTTPRequest httpRequest, HTTPResponse httpResponse) {
        this.request = httpRequest
        this.response = httpResponse
    }

    def propertyMissing(String name) {
        return parsedResponseContent[name]
    }

    def propertyMissing(String name, value) {
        parsedResponseContent[name] = value
    }

    @Override
    String toString() {
        def excludes = ['response', 'data', 'contentAsString'] + parsedResponseContent.keySet()
        ObjectHelper.dump(this, exclude:excludes)
    }
}
