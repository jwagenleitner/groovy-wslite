/* Copyright 2012 the original author or authors.
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

import wslite.http.HTTPRequest
import wslite.http.HTTPResponse
import wslite.json.JSONArray
import wslite.json.JSONObject

class ResponseBuilder {

    Response build(HTTPRequest httpRequest, HTTPResponse httpResponse) {
        Response response = new Response(httpRequest, httpResponse)
        if (isTextResponse(httpResponse)) {
            String responseText = httpResponse.getContentAsString()
            response.text = responseText
            if (responseText && isXmlResponse(httpResponse)) {
                response.xml = parseXmlContent(responseText)
            }
            if (responseText && isJsonResponse(httpResponse)) {
                response.json = parseJsonContent(responseText)
            }
        }
        return response
    }

    private boolean isTextResponse(HTTPResponse httpResponse) {
        return httpResponse.contentType?.startsWith('text/') ||
               httpResponse.contentType in ( ContentType.TEXT.getContentTypeList() +
                                             ContentType.HTML.getContentTypeList() +
                                             ContentType.XML.getContentTypeList()  +
                                             ContentType.JSON.getContentTypeList() )
    }

    private boolean isXmlResponse(HTTPResponse httpResponse) {
        return httpResponse.contentType in ContentType.XML.getContentTypeList()
    }

    private boolean isJsonResponse(HTTPResponse httpResponse) {
        return httpResponse.contentType in ContentType.JSON.getContentTypeList()
    }

    private parseXmlContent(String content) {
        return new XmlSlurper().parseText(content)
    }

    private parseJsonContent(String content) {
        return content.trim().startsWith('[') ? new JSONArray(content) : new JSONObject(content)
    }

}
