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

class RequestBuilder {

    HTTPMethod method
    String url
    String path
    Map params
    byte[] data

    def getURL() {
        def targetURL = new StringBuilder(url)
        targetURL.toString().endsWith("/") ?: targetURL.append('/')
        if (path && path != "/") {
            path.startsWith("/") ? targetURL.append(path[1..-1]) : targetURL.append(path)
        }
        if (params?.params) {
            targetURL.toString().indexOf("?") > 0 ? targetURL.append("&") : targetURL.append("?")
            targetURL.append(toQueryString(params.params))
        }
        return new URL(targetURL.toString())
    }

    def getHeaders() {
        return params?.headers
    }

    def toQueryString(params) {
        params?.collect { k, v -> "${URLEncoder.encode(k.toString())}=${URLEncoder.encode(v.toString())}" }.join('&')
    }

    HTTPRequest build() {
        HTTPRequest request = new HTTPRequest()
        request.url = this.getURL()
        request.method = this.method
        request.headers = this.getHeaders()
        return request
    }

}
