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
    RequestBuilder requestBuilder = new RequestBuilder()
    ResponseBuilder responseBuilder = new ResponseBuilder()

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

    def get(Map params=[:]) {
        return executeMethod(HTTPMethod.GET, params)
    }

    def delete(Map params=[:]) {
        return executeMethod(HTTPMethod.DELETE, params)
    }

    def post(Map params=[:], Closure content) {
        return executeMethod(HTTPMethod.POST, params, content)
    }

    def put(Map params=[:], Closure content) {
        return executeMethod(HTTPMethod.PUT, params, content)
    }

    def executeMethod(HTTPMethod method, Map params) {
        executeMethod(method, params, null)
    }

    def executeMethod(HTTPMethod method, Map params, Closure content) {
        // make a defensive copy of the params since setDefault* methods are destructive
        def requestParams = new LinkedHashMap(params ?: [:])
        setDefaultAcceptParam(requestParams)
        byte[] data = null
        if (content) {
            def contentBuilder = new ContentBuilder(defaultContentTypeHeader, defaultCharset)
            content.resolveStrategy = Closure.DELEGATE_FIRST
            content.delegate = contentBuilder
            content.call()
            setDefaultContentHeader(contentBuilder, requestParams)
            data = contentBuilder.getData()
        }
        HTTPRequest request
        HTTPResponse response
        try {
            request = requestBuilder.build(method, url, requestParams, data)
            response = httpClient.execute(request)
            return responseBuilder.build(request, response)
        } catch (HTTPClientException httpException) {
            throw new RESTClientException(httpException)
        } catch (Exception ex) {
            throw new RESTClientException(ex.message, ex, request, response)
        }
    }

    private void setDefaultAcceptParam(params) {
        if (!params.containsKey("accept") && defaultAcceptHeader) {
            params.accept = (defaultAcceptHeader instanceof ContentType) ?
                             defaultAcceptHeader.getAcceptHeader() : defaultAcceptHeader.toString()
        }
    }

    private void setDefaultContentHeader(contentBuilder, params) {
        if (!params.headers) params.headers = [:]
        if (!params.headers.containsKey("Content-Type")) {
            params.headers."Content-Type" = contentBuilder.getContentTypeHeader()
        }
    }

}
