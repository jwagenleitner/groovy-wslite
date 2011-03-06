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

    HTTPRequest build() {
        if (!url || !method) {
            throw new IllegalStateException("URL and Method are required")
        }
        HTTPRequest request = new HTTPRequest(params?.connectionParams ?: [:])
        request.url = this.getURL()
        request.method = this.method
        request.headers = this.getHeaders()
        request.data = this.data
        return request
    }

    void setParams(params) {
        this.params = new HashMap(params)
    }

    private URL getURL() {
        def targetURL = new StringBuilder(url)
        def path = params?.path
        if (path && path != "/") {
            targetURL.toString().endsWith("/") ?: targetURL.append('/')
            path.startsWith("/") ? targetURL.append(path[1..-1]) : targetURL.append(path)
        }
        if (params?.query) {
            targetURL.toString().indexOf("?") > 0 ? targetURL.append("&") : targetURL.append("?")
            targetURL.append(toQueryString(params.query))
        }
        return new URL(targetURL.toString())
    }

    private String toQueryString(params) {
        params?.collect { k, v -> "${URLEncoder.encode(k.toString())}=${URLEncoder.encode(v.toString())}" }.join('&')
    }

    private HTTPHeaderMap getHeaders() {
        HTTPHeaderMap headers = new HTTPHeaderMap(params?.headers ?: [:])
        if (!headers.containsKey("Accept")) {
            headers.Accept = getAcceptHeader()
        }
        if (!headers.containsKey("Content-Type")) {
            headers."Content-Type" = getContentType()
        }
        return headers
    }

    private String getAcceptHeader() {
        def acceptParam = params?.accept
        if (!acceptParam) {
            return ContentType.ANY.getAcceptHeader()
        }
        if (acceptParam instanceof ContentType) {
            return acceptParam.getAcceptHeader()
        }
        if (acceptParam instanceof String) {
            return acceptParam
        }
        if (acceptParam instanceof GString) {
            return acceptParam.toString()
        }
        throw new IllegalArgumentException("accept parameter must be an instace of ContentType or String")
    }

    private String getContentType() {
        def contentType = params?.contentType
        if (!contentType) {
            return ContentType.TEXT.toString()
        }
        return contentType.toString()
    }

}
