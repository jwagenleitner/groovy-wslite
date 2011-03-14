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
    private def contentType
    private def charset
    private def headers
    private def connectionParams

    RequestBuilder(HTTPMethod method, String url, Map params, byte[] data) {
        this.method = method
        this.url = url
        this.params = new LinkedHashMap(params ?: [:])
        this.data = data

        this.path = params?.remove("path")
        this.query = params?.remove("query")
        this.accept = params?.remove("accept")
        this.contentType = params?.remove("contentType")
        this.charset = params?.remove("charset")
        this.headers = new HTTPHeaderMap(params?.remove("headers") ?: [:])
        this.connectionParams = params?.remove("connectionParams")
    }

    HTTPRequest build() {
        if (!method || !url) {
            throw new IllegalStateException("URL and Method are required")
        }
        HTTPRequest request = new HTTPRequest(connectionParams ?: [:])
        request.method = this.method
        request.url = this.buildURL()
        request.headers = this.buildHeaders()
        request.data = this.data
        return request
    }

    private URL buildURL() {
        def targetURL = new StringBuilder(url)
        if (path && path != "/") {
            targetURL.toString().endsWith("/") ?: targetURL.append('/')
            path.startsWith("/") ? targetURL.append(path[1..-1]) : targetURL.append(path)
        }
        if (query) {
            targetURL.toString().indexOf("?") > 0 ? targetURL.append("&") : targetURL.append("?")
            targetURL.append(toQueryString(query))
        }
        return new URL(targetURL.toString())
    }

    private String toQueryString(params) {
        params?.collect { k, v ->
            "${URLEncoder.encode(k.toString())}=${URLEncoder.encode(v.toString())}"
        }.join('&')
    }

    private HTTPHeaderMap buildHeaders() {
        if (!headers.containsKey("Accept")) {
            headers.Accept = getAcceptHeader()
        }
        if (data && !headers.containsKey("Content-Type")) {
            headers."Content-Type" = getContentType()
        }
        return headers
    }

    private String getAcceptHeader() {
        if (accept instanceof ContentType) {
            return accept.getAcceptHeader()
        }
        return accept.toString()
    }

    private String getContentType() {
        return contentType.toString()
    }

}
