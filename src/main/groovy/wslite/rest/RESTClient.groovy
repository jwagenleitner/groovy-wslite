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
        // handle urlencoding a map ex. params.body
        throw new UnsupportedOperationException("URLEncoded body POST not support yet.")
    }

    def post(Map params=[:], Closure content) {
        def xml = new groovy.xml.StreamingMarkupBuilder().bind(content)
        return post(params, xml.toString())
    }

    def post(Map params=[:], String content) {
        return post(params, content.bytes)
    }

    def post(Map params=[:], byte[] content) {
        return executeMethod(HTTPMethod.POST, params, content)
    }

    def put(Map params=[:]) {
        // handle urlencoding a map ex. params.body
        throw new UnsupportedOperationException("URLEncoded body PUT not support yet.")
    }

    def put(Map params=[:], Closure content) {
        def xml = new groovy.xml.StreamingMarkupBuilder().bind(content)
        return put(params, xml.toString())
    }

    def put(Map params=[:], String content) {
        return put(params, content.bytes)
    }

    def put(Map params=[:], byte[] content) {
        return executeMethod(HTTPMethod.PUT, params, content)
    }

    def executeMethod(HTTPMethod method, Map params) {
        executeMethod(method, params, null)
    }

    def executeMethod(HTTPMethod method, Map params, byte[] content) {
        RequestBuilder builder = new RequestBuilder(method:method, url:url, params:params, data:content)
        def response = httpClient.execute(builder.build())
        return buildResponse(response)
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

}
