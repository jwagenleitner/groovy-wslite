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

import groovy.xml.*
import wslite.http.*
import wslite.json.*

class ContentBuilder {

    byte[] data

    private String contentType
    private String charset

    private ContentType dataContentType

    ContentBuilder(String defaultContentType, String defaultCharset) {
        contentType = defaultContentType
        charset = defaultCharset
    }

    ContentBuilder build(Closure content) {
        content.resolveStrategy = Closure.DELEGATE_FIRST
        content.delegate = this
        content.call()
        return this
    }

    void type(contentType) {
        this.contentType = contentType?.toString()
    }

    void charset(charset) {
        this.charset = charset?.toString()
    }

    void bytes(content) {
        dataContentType = ContentType.BINARY
        data = content
    }

    void text(content) {
        dataContentType = ContentType.TEXT
        data = content?.toString().getBytes(getCharset())
    }

    void urlenc(content) {
        dataContentType = ContentType.URLENC
        data = new URLParametersCodec().encode(content).getBytes(getCharset())
    }

    void xml(content) {
        dataContentType = ContentType.XML
        data = closureToXmlString(content).getBytes(getCharset())
    }

    void json(content) {
        dataContentType = ContentType.JSON
        data = objectToJson(content).getBytes(getCharset())
    }

    String getCharset() {
        return charset ?: HTTP.DEFAULT_CHARSET
    }

    String getContentTypeHeader() {
        ContentTypeHeader contentTypeHeader = new ContentTypeHeader(getContentType())
        if (!contentTypeHeader.charset) {
            return contentTypeHeader.mediaType + '; charset=' + getCharset()
        }
        return contentTypeHeader.contentType
    }

    private String getContentType() {
        return contentType ?: dataContentType.toString()
    }

    private String closureToXmlString(content) {
        return new StreamingMarkupBuilder().bind(content).toString()
    }

    private String objectToJson(content) {
        if (content instanceof Map) {
            return new JSONObject(content).toString()
        }
        if (content instanceof List) {
            return new JSONArray(content).toString()
        }
        return content
    }

}
