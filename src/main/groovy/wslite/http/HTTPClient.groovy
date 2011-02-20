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

import javax.net.ssl.*;

class HTTPClient {

    int connectTimeout = 10000
    int readTimeout = 30000

    boolean useCaches = false
    boolean followRedirects = true
    boolean trustAllSSLCerts = true

    def defaultHeaders = [Connection:"Close"]

    HTTPConnectionFactory httpConnectionFactory
    HTTPAuthorization auth = new HTTPAuthorization()

    HTTPClient(HTTPConnectionFactory httpConnectionFactory=new HTTPConnectionFactory()) {
         this.httpConnectionFactory = httpConnectionFactory
    }

    def get(String url, Map headers=[:]) {
        return get(new URL(url), headers)
    }

    def get(URL url, Map headers=[:]) {
        return executeMethod("GET", url, null, headers)
    }

    def delete(String url, Map headers=[:]) {
        return delete(new URL(url), headers)
    }

    def delete(URL url, Map headers=[:]) {
        return executeMethod("DELETE", url, null, headers)
    }

    def post(String url, byte[] content, Map headers=[:]) {
        return post(new URL(url), content, headers)
    }

    def post(URL url, byte[] content, Map headers=[:]) {
        return executeMethod("POST", url, content, headers)
    }

    def put(String url, byte[] content, Map headers=[:]) {
        return put(new URL(url), content, headers)
    }

    def put(URL url, byte[] content, Map headers=[:]) {
        return executeMethod("PUT", url, content, headers)
    }

    private def executeMethod(String method, URL url, byte[] content, Map headers) {
        HttpURLConnection conn = setupConnection(url, headers)
        conn.setRequestMethod(method)
        def response
        try {
            doOutput(conn, content)
            def data = doInput(conn)
            response = buildResponse(conn)
            response.data = data
        } catch(Exception ex) {
            response = buildResponse(conn)
            response.data = conn.getErrorStream().bytes
            throw new HTTPClientException(response.status + " " + response.statusMessage, ex, response)
        } finally {
            conn.disconnect()
        }
        return response
    }

    private void doOutput(conn, content) {
        if (content) {
            conn.setDoOutput(true)
            conn.addRequestProperty("Content-Length", "${content.size()}")
            conn.getOutputStream().bytes = content
        }
    }

    private def doInput(conn) {
        return conn.getInputStream().bytes
    }

    private def setupConnection(URL url, Map headers) {
        def conn = httpConnectionFactory.getConnection(url)
        if (url.getProtocol() == "https") {
            setupSSLTrustManager(conn)
        }
        conn.setConnectTimeout(connectTimeout)
        conn.setReadTimeout(readTimeout)
        conn.setUseCaches(useCaches)
        conn.setInstanceFollowRedirects(followRedirects)
        setRequestHeaders(conn, headers)
        setAuthorizationHeader(conn)
        return conn
    }

    private void setRequestHeaders(conn, headers) {
        for (entry in headers) {
            conn.setRequestProperty(entry.key, entry.value)
        }
        for (entry in defaultHeaders) {
            conn.addRequestProperty(entry.key, entry.value)
        }
    }

    private def setAuthorizationHeader(conn) {
        auth.authorize(conn)
    }

    private def buildResponse(HttpURLConnection conn) {
        def response = [:]
        response.status = conn.getResponseCode()
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
        def headers = [:]
        for (entry in conn.getHeaderFields()) {
            headers[entry.key] = entry.value.size() > 1 ? entry.value : entry.value[0]
        }
        return headers
    }

    private def setupSSLTrustManager(conn) {
        if (!trustAllSSLCerts) return
        def trustingTrustManager = [getAcceptedIssuers:{}, checkClientTrusted:{arg0, arg1 -> }, checkServerTrusted:{arg0, arg1 -> }] as X509TrustManager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, [trustingTrustManager] as TrustManager[], null)
        conn.setSSLSocketFactory(sc.getSocketFactory())
        conn.setHostnameVerifier({arg0, arg1 -> return true} as HostnameVerifier)
    }

}
