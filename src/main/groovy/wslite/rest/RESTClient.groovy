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
import wslite.http.auth.*

class RESTClient {

    String url
    HTTPClient httpClient
    HTTPAuthorization authorization

    def responseHandlers = [XmlResponse.class, TextResponse.class]

    def defaultAcceptHeader
    def defaultContentTypeHeader
    String defaultCharset = "UTF-8"

    RESTClient(HTTPClient client=new HTTPClient()) {
        this.httpClient = client
    }

    RESTClient(String url, HTTPClient client=new HTTPClient()) {
        this.url = url
        this.httpClient = client
    }

    void setAuthorization(HTTPAuthorization authorization) {
        this.httpClient.authorization = authorization
    }

    HTTPAuthorization getAuthorization() {
        return this.authorization
    }

    void addResponseHandler(Class clazz) {
        responseHandlers.remove(clazz)
        responseHandlers = [clazz] + responseHandlers
    }

    def get(Map params=[:]) {
        return executeMethod(HTTPMethod.GET, params)
    }

    def delete(Map params=[:]) {
        return executeMethod(HTTPMethod.DELETE, params)
    }

    def post(Map params=[:]) {
        // make a defensive copy of the params since getBodyContent is destructive
        def requestParams = new LinkedHashMap(params)
        String body =  getBodyContent(requestParams)
        return post(requestParams, body)
    }

    def post(Map params=[:], String content) {
        return post(params, content.getBytes(getCharset(params)))
    }

    def post(Map params=[:], byte[] content) {
        return executeMethod(HTTPMethod.POST, params, content)
    }

    def put(Map params=[:]) {
        // make a defensive copy of the params since getBodyContent is destructive
        def requestParams = new LinkedHashMap(params)
        String body =  getBodyContent(requestParams)
        return put(requestParams, body)
    }

    def put(Map params=[:], String content) {
        return put(params, content.getBytes(getCharset(params)))
    }

    def put(Map params=[:], byte[] content) {
        return executeMethod(HTTPMethod.PUT, params, content)
    }

    def executeMethod(HTTPMethod method, Map params) {
        executeMethod(method, params, null)
    }

    def executeMethod(HTTPMethod method, Map params, byte[] content) {
        // make a defensive copy of the params since setDefault* methods are destructive
        def requestParams = new LinkedHashMap(params)
        setDefaultAcceptParam(requestParams)
        if (content) {
            setDefaultContentTypeParam(requestParams)
            setDefaultCharsetParam(requestParams)
        }
        RequestBuilder builder = new RequestBuilder(method, url, requestParams, content)
        HTTPRequest request = builder.build()
        def response = httpClient.execute(request)
        return buildResponse(response)
    }

    private void setDefaultAcceptParam(params) {
        if (params?.containsKey("accept")) {
            return
        }
        if (defaultAcceptHeader) {
            params.accept = (defaultAcceptHeader instanceof ContentType) ?
                             defaultAcceptHeader.getAcceptHeader() : defaultAcceptHeader.toString()
        }
    }

    private void setDefaultContentTypeParam(params) {
        if (params.containsKey("contentType")) {
            return
        }
        if (defaultContentTypeHeader) {
            params.contentType = defaultContentTypeHeader.toString()
        }
    }

    private void setDefaultCharsetParam(params) {
        if (params.containsKey("charset")) {
            return
        }
        params.charset = getCharset(params)
    }

    private def buildResponse(HTTPResponse response) {
        def handler = getResponseHandler(response.contentType)
        if (!handler) {
            return response
        }
        return handler.newInstance(response)
    }

    private Class getResponseHandler(String contentType) {
        for (handler in responseHandlers) {
             if (handler.handles(contentType)) {
                 return handler
             }
        }
    }

    private String getBodyContent(params) {
        def body = params.remove("xml")
        if (body)
            return closureToXmlString(body)
        body = params.remove("json")
        if (body)
            return body
        body = params.remove("urlenc")
        if (body)
            return mapToURLEncodedString(body)
        return ""
    }

    private String closureToXmlString(content) {
        def xml = new groovy.xml.StreamingMarkupBuilder().bind(content)
        return xml.toString()
    }

    private String mapToURLEncodedString(params) {
        if (!params || !(params instanceof Map)) {
            return null
        }
        def encodedList = []
        for (entry in params) {
            if (entry.value != null && entry.value instanceof List) {
                for (item in entry.value) {
                    encodedList << urlEncodePair(entry.key, item)
                }
                continue
            }
            encodedList << urlEncodePair(entry.key, entry.value)
        }
        return encodedList.join('&')
    }

    private String urlEncodePair(key, value) {
        if (!key) return ""
        value = value ?: ""
        return "${URLEncoder.encode(key.toString())}=${URLEncoder.encode(value.toString())}"
    }

    private String getCharset(params) {
        if (params?.headers?."Content-Type") {
            String charset = getCharsetFromContentTypeHeader(params.headers."Content-Type")
            if (charset) return charset
        }
        return params?.charset ?: defaultCharset
    }

    private String getCharsetFromContentTypeHeader(contentType) {
        // TODO: need to move parsing of content-type and its parameters, this is just ugly
        def response = new HTTPResponse()
        response.contentType = contentType
        return response.charset
    }

}
