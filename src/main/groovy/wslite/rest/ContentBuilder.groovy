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

    private static final byte[] LINE_SEPARATOR = [13, 10]
    private static final byte[] BOUNDARY_PREFIX = [45, 45]
    byte[] data

    private String contentType
    private String charset
    private String boundary

    private ContentType dataContentType

    private Closure xmlContentClosure
    private Map<String, byte[]> multipartData

    ContentBuilder(String defaultContentType, String defaultCharset) {
        contentType = defaultContentType
        charset = defaultCharset
    }

    ContentBuilder build(Closure content) {
        Closure c = content.clone()
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.delegate = this
        c.call()
        c.delegate = content.delegate
        // We defer the processing of the xml closure because we first have to reset the parent closure delegate.
        // If we do not reset the delegate then this object's methods will be called instead of methods/properties
        // of the calling object.
        if (!data && xmlContentClosure) {
            data = closureToXmlString(xmlContentClosure).getBytes(getCharset())
        }
        if (!data && multipartData) {
            data = buildMultipartRequest(multipartData)
        }
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
        data = content?.toString()?.getBytes(getCharset())
    }

    void urlenc(Map content) {
        dataContentType = ContentType.URLENC
        data = new URLParametersCodec().encode(content)?.getBytes(getCharset())
    }

    void multipart(String name, content) {
        dataContentType = ContentType.MULTIPART
        multipartData = multipartData ?: [:]
        multipartData.put(name, content)
    }

    void xml(Closure content) {
        dataContentType = ContentType.XML
        xmlContentClosure = content
    }

    void json() {
        dataContentType = ContentType.JSON
        data = null
    }

    void json(Map content) {
        dataContentType = ContentType.JSON
        data = new JSONObject(content).toString()?.getBytes(getCharset())
    }

    void json(List content) {
        dataContentType = ContentType.JSON
        data = new JSONArray(content).toString()?.getBytes(getCharset())
    }

    String getCharset() {
        return charset ?: HTTP.DEFAULT_CHARSET
    }

    String getContentTypeHeader() {
        ContentTypeHeader contentTypeHeader = new ContentTypeHeader(getContentType())
        if (boundary) {
            return contentTypeHeader.mediaType + '; boundary=' + boundary
        }
        else if (!contentTypeHeader.charset) {
            return contentTypeHeader.mediaType + '; charset=' + getCharset()
        }
        return contentTypeHeader.contentType
    }

    private String getContentType() {
        return contentType ?: dataContentType.toString()
    }

    private String closureToXmlString(content) {
        return XmlUtil.serialize(new StreamingMarkupBuilder().bind(content))
    }

    private byte[] buildMultipartRequest(content) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream()

        boundary = ('-' * 4) + 'groovy-wslite-' + (UUID.randomUUID())
        dataContentType = ContentType.MULTIPART

        multipartData.each { String name, byte[] cnt ->
            baos.write(BOUNDARY_PREFIX)
            baos.write(boundary.bytes)
            baos.write(LINE_SEPARATOR)
            baos.write("Content-Disposition: form-data; name=\"${name}\"".toString().bytes)
            baos.write(LINE_SEPARATOR)
            baos.write(LINE_SEPARATOR)
            baos.write(cnt)
            baos.write(LINE_SEPARATOR)
        }
        baos.write(BOUNDARY_PREFIX)
        baos.write(boundary.bytes)
        baos.write(BOUNDARY_PREFIX)
        baos.write(LINE_SEPARATOR)

        return baos.toByteArray()

    }

}
