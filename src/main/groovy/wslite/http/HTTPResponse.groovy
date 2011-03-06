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
        this.contentType = parseContentType(contentType)
        this.charset = parseCharsetParam(contentType)
    }

    private String parseContentType(String contentType) {
        int delim = contentType.indexOf(';')
        this.contentType = (delim < 1) ? contentType : contentType[0..delim-1]
    }

    private String parseCharsetParam(String contentType) {
        int start = contentType.toLowerCase().indexOf("charset=")
        if (start == -1) return null
        String charset = contentType.substring(start)
        int end = charset.indexOf(' ')
        if (end != -1) charset = charset.substring(0, end)
        this.charset = charset.split("=")[1]
    }
}
