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
    Map params
    byte[] data

    private def path
    private def query
    private def accept
    private def headers
    private def targetURL

    RequestBuilder(HTTPMethod method, String url, Map params, byte[] data) {
        this.method = method
        this.url = url
        this.params = new LinkedHashMap(params ?: [:])
        this.data = data

        this.path = this.params?.remove("path")
        this.query = this.params?.remove("query")
        this.accept = this.params?.remove("accept")
        this.headers = new HTTPHeaderMap(this.params?.remove("headers") ?: [:])
    }

    HTTPRequest build() {
        if (!method || !url) {
            throw new IllegalStateException("URL and Method are required")
        }
        HTTPRequest request = new HTTPRequest(params ?: [:])
        buildURL()
        buildHeaders()
        request.method = this.method
        request.url = this.targetURL
        request.headers = this.headers
        request.data = this.data
        return request
    }

    private void buildURL() {
        def target = new StringBuilder(url)
        if (path && path != "/") {
            target.toString().endsWith("/") ?: target.append('/')
            path.startsWith("/") ? target.append(path[1..-1]) : target.append(path)
        }
        if (query) {
            target.toString().indexOf("?") > 0 ? target.append("&") : target.append("?")
            target.append(HTTP.mapToURLEncodedString(query))
        }
        targetURL = new URL(target.toString())
    }

    private void buildHeaders() {
        if (!accept || headers.containsKey("Accept")) {
            return
        }
        headers.Accept = (accept instanceof ContentType) ? accept.getAcceptHeader() : accept.toString()
    }

}
