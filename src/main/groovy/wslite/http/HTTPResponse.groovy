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

import wslite.util.ObjectHelper

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

    Map headers = new TreeMap(String.CASE_INSENSITIVE_ORDER)
    byte[] data

    Map getHeaders() {
        return headers.asImmutable()
    }

    void setHeaders(Map map) {
        headers.putAll(map)
    }

    String getContentAsString() {
        if (!data) {
            return ''
        }
        return new String(data, charset ?: HTTP.DEFAULT_CHARSET)
    }

    @Override
    String toString() {
        def excludes = ['data', 'contentAsString']
        ObjectHelper.dump(this, exclude:excludes)
    }

}
