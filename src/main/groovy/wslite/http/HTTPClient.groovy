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
    def useCaches = false
    def followRedirects = true
    def trustAllSSLCerts = true
    def defaultHeaders = [Connection:"Close"]

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
        if (content) {
            conn.setDoOutput(true)
            conn.addRequestProperty("Content-Length", "${content.size()}")
            conn.getOutputStream().bytes = content
        }
        def data = conn.getInputStream().bytes
        def response = buildResponse(conn)
        response.data = data
        conn.disconnect()
        return response
    }

    private def setupConnection(URL url, Map headers) {
        def conn = url.openConnection()
        if (url.getProtocol() == "https") {
            setupSSLTrustManager(conn)
        }
        conn.setConnectTimeout(connectTimeout)
        conn.setReadTimeout(readTimeout)
        conn.setUseCaches(useCaches)
        conn.setInstanceFollowRedirects(followRedirects)
        for (entry in headers) {
            conn.setRequestProperty(entry.key, entry.value)
        }
        for (entry in defaultHeaders) {
            conn.addRequestProperty(entry.key, entry.value)
        }
        return conn
    }

    private def buildResponse(HttpURLConnection conn) {
        def response = [:]
        def headers = [:]
        for (entry in conn.getHeaderFields()) {
            headers[entry.key] = entry.value.size() > 1 ? entry.value : entry.value[0]
        }
        response.headers = headers
        response.status = conn.getResponseCode()
        response.statusMessage = conn.getResponseMessage()
        return response
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
