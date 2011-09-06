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

import wslite.http.auth.*

class HTTPClient {

    int connectTimeout = 0
    int readTimeout = 0

    boolean useCaches = false
    boolean followRedirects = true
    boolean sslTrustAllCerts
    String sslTrustStoreFile
    String sslTrustStorePassword

    def defaultHeaders = [Connection:"Close"]

    HTTPConnectionFactory httpConnectionFactory
    HTTPAuthorization authorization

    HTTPClient() {
        httpConnectionFactory = new HTTPConnectionFactory()
    }

    HTTPClient(HTTPConnectionFactory httpConnectionFactory) {
         this.httpConnectionFactory = httpConnectionFactory
    }

    HTTPResponse execute(HTTPRequest request) {
        def conn = createConnection(request)
        setupConnection(conn, request)
        HTTPResponse response
        try {
            doOutput(conn, request.data)
            response = buildResponse(conn, doInput(conn))
        } catch(Exception ex) {
            response = buildResponse(conn, conn.getErrorStream()?.bytes)
            throw new HTTPClientException(response.statusCode + " " + response.statusMessage, ex, request, response)
        } finally {
            conn.disconnect()
        }
        return response
    }

    private def createConnection(HTTPRequest request) {
        if (isSecureConnectionRequest(request)) {
            if (shouldTrustAllSSLCerts(request)) {
                return httpConnectionFactory.getConnectionTrustAllSSLCerts(request.url)
            }
            if (shouldTrustSSLCertsUsingTrustStore(request)) {
                String trustStoreFile
                String trustStorePassword
                if (request.sslTrustStoreFile) {
                    trustStoreFile = request.sslTrustStoreFile
                    trustStorePassword = request.sslTrustStorePassword
                } else {
                    trustStoreFile = sslTrustStoreFile
                    trustStorePassword = sslTrustStorePassword
                }
                return httpConnectionFactory.getConnectionUsingTrustStore(request.url, trustStoreFile, trustStorePassword)
            }
        }
        return httpConnectionFactory.getConnection(request.url)
    }

    private boolean isSecureConnectionRequest(HTTPRequest request) {
        return request.url.getProtocol().toLowerCase() == "https"
    }

    private boolean shouldTrustAllSSLCerts(HTTPRequest request) {
        return request.isSSLTrustAllCertsSet ? request.sslTrustAllCerts : sslTrustAllCerts
    }

    private boolean shouldTrustSSLCertsUsingTrustStore(HTTPRequest request) {
        return request.sslTrustStoreFile !=null || sslTrustStoreFile !=null
    }

    private void setupConnection(conn, HTTPRequest request) {
        conn.setRequestMethod(request.method.toString())
        conn.setConnectTimeout(request.isConnectTimeoutSet ? request.connectTimeout : connectTimeout)
        conn.setReadTimeout(request.isReadTimeoutSet ? request.readTimeout : readTimeout)
        conn.setUseCaches(request.isUseCachesSet ? request.useCaches : useCaches)
        conn.setInstanceFollowRedirects(request.isFollowRedirectsSet ? request.followRedirects : followRedirects)
        setRequestHeaders(conn, request)
        setAuthorizationHeader(conn)
    }

    private void setRequestHeaders(conn, request) {
        for (entry in request.headers) {
            conn.setRequestProperty(entry.key, entry.value)
        }
        for (entry in defaultHeaders) {
            conn.addRequestProperty(entry.key, entry.value)
        }
    }

    private void setAuthorizationHeader(conn) {
        if (!authorization) return
        authorization.authorize(conn)
    }

    private void doOutput(conn, content) {
        if (content) {
            conn.setDoOutput(true)
            conn.addRequestProperty("Content-Length", "${content.size()}")
            conn.getOutputStream().bytes = content
        }
    }

    private byte[] doInput(conn) {
        return conn.getInputStream().bytes
    }

    private HTTPResponse buildResponse(conn, data) {
        def response = new HTTPResponse()
        response.data = data
        response.statusCode = conn.getResponseCode()
        response.statusMessage = conn.getResponseMessage()
        response.url = conn.getURL()
        response.contentEncoding = conn.getContentEncoding()
        response.contentLength = conn.getContentLength()
        response.contentType = conn.getContentType()
        response.date = new Date(conn.getDate())
        response.expiration = new Date(conn.getExpiration())
        response.lastModified = new Date(conn.getLastModified())
        response.headers = headersToMap(conn)
        return response
    }

    private def headersToMap(conn) {
        def headers = new HTTPHeaderMap()
        for (entry in conn.getHeaderFields()) {
            headers[entry.key] = entry.value.size() > 1 ? entry.value : entry.value[0]
        }
        return headers
    }

}
