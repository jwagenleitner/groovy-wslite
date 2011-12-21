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

    URLParametersCodec urlParametersCodec = new URLParametersCodec()

    HTTPRequest build(HTTPMethod method, String url, Map params, byte[] data) {
        if (!method || !url) {
            throw new IllegalArgumentException('URL and Method are required')
        }
        params = new LinkedHashMap(params ?: [:])
        def path = params.remove('path')
        def query = params.remove('query')
        def accept = params.remove('accept')
        def headers = params.remove('headers') ?: [:]
        HTTPRequest request = new HTTPRequest(params)
        request.method = method
        request.url = buildURL(url, path, query)
        request.headers = headers
        if (accept && !request.headers.containsKey('Accept')) {
            request.headers.Accept = (accept instanceof ContentType) ? accept.getAcceptHeader() : accept.toString()
        }
        request.data = data
        return request
    }

    private URL buildURL(url, path, query) {
        def target = new StringBuilder(url)
        if (path && path != '/') {
            url.endsWith('/') ?: target.append('/')
            path.startsWith('/') ? target.append(path[1..-1]) : target.append(path)
        }
        if (query) {
            target.indexOf('?') == -1 ? target.append('?') : target.append('&')
            target.append(urlParametersCodec.encode(query))
        }
        return new URL(target.toString())
    }

}
