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
package wslite.http

class HTTPResponse {

    URL url
    int statusCode
    String statusMessage
    String contentType
    String charset
    String contentEncoding
    int contentLength
    Date date
    Date expiration
    Date lastModified

    HTTPHeaderMap headers
    byte[] data

    Map getHeaders() {
        return Collections.unmodifiableMap(headers)
    }

    void setContentType(String contentType) {
        if (!contentType) {
            this.contentType = null
            this.charset = null
            return
        }
        this.contentType = HTTP.parseMimeTypeFromContentType(contentType)
        this.charset = HTTP.parseCharsetParamFromContentType(contentType)
    }

    String getContentAsString() {
        if (!data) return ""
        return new String(data, charset ?: HTTP.DEFAULT_CHARSET)
    }

    @Override
    String toString() {
        "[url: ${url}, statusCode: ${statusCode}, statusMessage: ${statusMessage}, contentType: ${contentType}, charset: ${charset}, contentLength: ${contentLength}, headers: ${headers}, data: ${data != null}]"
    }

}
