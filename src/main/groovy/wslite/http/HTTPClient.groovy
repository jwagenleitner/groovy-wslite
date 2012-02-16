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

import java.util.zip.GZIPInputStream

class HTTPClient {

    int connectTimeout = 0
    int readTimeout = 0
    boolean followRedirects = true
    boolean useCaches
    boolean sslTrustAllCerts
    String sslTrustStoreFile
    String sslTrustStorePassword

    Proxy proxy = Proxy.NO_PROXY

    def defaultHeaders = [Connection:'Close', 'Accept-Encoding':'gzip']

    HTTPConnectionFactory httpConnectionFactory
    HTTPAuthorization authorization

    HTTPClient() {
        httpConnectionFactory = new HTTPConnectionFactory()
    }

    HTTPClient(HTTPConnectionFactory httpConnectionFactory) {
         this.httpConnectionFactory = httpConnectionFactory
    }

    HTTPResponse execute(HTTPRequest request) {
        if (!(request?.url && request?.method)) {
            throw new IllegalArgumentException('HTTP Request must contain a url and method')
        }
        HTTPResponse response
        def conn
        try {
            conn = createConnection(request)
            setupConnection(conn, request)
            def connstream = (conn.inputStream && (conn.contentEncoding == 'gzip')) ? (new GZIPInputStream(conn.inputStream)) : conn.inputStream
            response = buildResponse(conn, connstream?.bytes)
        } catch(Exception ex) {
            if (!conn) {
                throw new HTTPClientException(ex.message, ex, request, response)
            } else {
                response = buildResponse(conn, conn.errorStream?.bytes)
                throw new HTTPClientException(response.statusCode + ' ' + response.statusMessage,
                        ex, request, response)
            }
        } finally {
            conn?.disconnect()
        }
        return response
    }

    private createConnection(HTTPRequest request) {
        def usedProxy = request.proxy ?: proxy
        if (isSecureConnectionRequest(request)) {
            if (shouldTrustAllSSLCerts(request)) {
                return httpConnectionFactory.getConnectionTrustAllSSLCerts(request.url, usedProxy)
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
                return httpConnectionFactory.getConnectionUsingTrustStore(request.url,
                        trustStoreFile, trustStorePassword, usedProxy)
            }
        }
        return httpConnectionFactory.getConnection(request.url, usedProxy)
    }

    private boolean isSecureConnectionRequest(HTTPRequest request) {
        return request.url.protocol.toLowerCase() == 'https'
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
        if (request.data) {
            conn.setDoOutput(true)
            if (conn.getRequestProperty(HTTP.CONTENT_LENGTH_HEADER) == null) {
                conn.setRequestProperty(HTTP.CONTENT_LENGTH_HEADER, "${request.data.size()}")
            }
            conn.outputStream.bytes = request.data
        }
    }

    private void setRequestHeaders(conn, request) {
        for (entry in request.headers) {
            setConnectionRequestProperty(conn, entry.key, entry.value)
        }
        for (entry in defaultHeaders) {
            if (conn.getRequestProperty(entry.key) == null) {
                setConnectionRequestProperty(conn, entry.key, entry.value)
            }
        }
    }

    private void setConnectionRequestProperty(conn, String key, List values) {
        for (val in values) {
            setConnectionRequestProperty(conn, key, val.toString())
        }
    }

    private void setConnectionRequestProperty(conn, String key, String value) {
        conn.setRequestProperty(key, value)
    }

    private void setAuthorizationHeader(conn) {
        if (authorization) {
            authorization.authorize(conn)
        }
    }

    private HTTPResponse buildResponse(conn, responseData) {
        def response = new HTTPResponse()
        response.data = responseData
        response.statusCode = conn.responseCode
        response.statusMessage = conn.responseMessage
        response.url = conn.URL
        response.contentEncoding = conn.contentEncoding
        response.contentLength = conn.contentLength
        ContentTypeHeader contentTypeHeader = new ContentTypeHeader(conn.contentType)
        response.contentType = contentTypeHeader.mediaType
        response.charset = contentTypeHeader.charset
        response.date = new Date(conn.date)
        response.expiration = new Date(conn.expiration)
        response.lastModified = new Date(conn.lastModified)
        response.headers = headersToMap(conn)
        return response
    }

    private Map headersToMap(conn) {
        def headers = [:]
        for (entry in conn.headerFields) {
            headers[entry.key ?: ''] = entry.value.size() > 1 ? entry.value : entry.value[0]
        }
        return headers
    }

}
