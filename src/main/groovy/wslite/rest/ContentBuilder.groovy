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

class ContentBuilder {

    String contentType
    String charset

    def contents = [:]

    ContentBuilder(String defaultContentType, String defaultCharset) {
        this.contentType = defaultContentType
        this.charset = defaultCharset
    }

    void type(contentType) {
        this.contentType = contentType?.toString()
    }

    void charset(charset) {
        this.charset = charset?.toString()
    }

    void bytes(data) {
        contents["bytes"] = data
    }

    void text(data) {
        contents["text"] = data?.toString()
    }

    void urlenc(data) {
        contents["urlenc"] = data
    }

    void xml(data) {
        contents["xml"] = data
    }

    byte[] getData() {
        if (contents["bytes"]) return contents["bytes"]
        if (contents["text"]) return contents["text"].getBytes(charset)
        if (contents["urlenc"]) return HTTP.mapToURLEncodedString(contents["urlenc"]).getBytes(charset)
        if (contents["xml"]) return closureToXmlString(contents["xml"]).getBytes(charset)
        return null
    }

    String getContentTypeHeader() {
        return "${contentType}; charset=${charset}"
    }

    private String closureToXmlString(content) {
        def xml = new groovy.xml.StreamingMarkupBuilder().bind(content)
        return xml.toString()
    }

}
