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

class RESTClient {

    String url
    HTTPClient http

    String defaultCharset = "UTF-8"
    def charContentTypes = [ContentType.HTML, ContentType.JSON, ContentType.TEXT, ContentType.XML]

    RESTClient(String url, HTTPClient client=new HTTPClient()) {
        this.url = url
        this.http = client
    }

    def get(Map params=[:], String path, String mimeType=ContentType.JSON) {
        return executeMethod("GET", path, params, mimeType, null)
    }

    def delete(Map params=[:], String path) {
        return executeMethod("DELETE", path, params, null, null)
    }

    def post(Map params=[:], String path, String mimeType=ContentType.JSON, String content) {
        return post(params, path, mimeType, content?.bytes)
    }

    def post(Map params=[:], String path, String mimeType=ContentType.JSON, byte[] content) {
        return executeMethod("POST", path, params, mimeType, content)
    }

    def put(Map params=[:], String path, String mimeType=ContentType.JSON, String content) {
        return put(params, path, mimeType, content?.bytes)
    }

    def put(Map params=[:], String path, String mimeType=ContentType.JSON, byte[] content) {
        return executeMethod("PUT", path, params, mimeType, content)
    }

    def executeMethod(String method, String path, Map params, String mimeType, byte[] content) {
        RequestBuilder builder = new RequestBuilder(url:url, path:path, params:params)
        def headers = builder.getHeaders()
        if (method in ["PUT", "POST"]) {
            headers["Content-Type"] = mimeType
        } else {
            headers["Accept"] = mimeType
        }
        def response = http.executeMethod(method, builder.getURL(), content, headers)
        parseResponseData(response)
        return response
    }

    def parseResponseData(response) {
        def contentType = parseContentType(response.contentType)
        if (isTextContentType(contentType)) {
            def charset = parseCharset(response.contentType) ?: defaultCharset
            response.data = new String(response.data, charset.toUpperCase())
        }
    }

    boolean isTextContentType(String contentType) {
        for (entry in charContentTypes) {
            if (entry.contains(contentType)) {
                return true
            }
        }
        return false
    }

    String parseContentType(String contentType) {
        int semicolon = contentType.indexOf(";")
        return (semicolon < 0) ? contentType : contentType.substring(0, semicolon)
    }

    String parseCharset(String contentType) {
        String marker = "charset="
        int start = contentType.indexOf(marker)
        return (start < 0) ? null : contentType.substring(start + marker.size())
    }

}
