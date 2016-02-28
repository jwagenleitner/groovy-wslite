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

enum ContentType {
    JSON(['application/json', 'application/hal+json', 'application/javascript', 'text/javascript', 'text/json']),
    XML(['application/xml', 'text/xml', 'application/xhtml+xml', 'application/atom+xml']),
    HTML(['text/html']),
    URLENC(['application/x-www-form-urlencoded']),
    MULTIPART(['multipart/form-data']),
    BINARY(['application/octet-stream']),
    TEXT(['text/plain']),
    ANY(['*/*'])

    private final List contentTypes

    List getContentTypeList() {
        return contentTypes
    }

    boolean contains(String contentType) {
        boolean result = contentType in contentTypes
        if (!result) {
            switch (this) {
                case JSON:
                    result = contentType ==~ /application\/.*\+json/
                    break;
                case XML:
                    result = contentType ==~ /application\/.*\+xml/
                    break;
            }
        }
        return result
    }

    @Override
    String toString() {
        return contentTypes.first()
    }

    String getAcceptHeader() {
        return contentTypes.join(', ')
    }

    private ContentType(contentTypes) {
        this.contentTypes = contentTypes
    }

}
