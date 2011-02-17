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

class HTTPClient {

    def get(String url, Map headers=[:]) {
        return executeMethod('GET', url, headers)
    }

    def delete(String url, Map headers=[:]) {
        return executeMethod('DELETE', url, headers)
    }

    def post(String url, byte[] content, Map headers=[:]) {
        return executeMethod('POST', url, content, headers)
    }

    def put(String url, byte[] content, Map headers=[:]) {
        return executeMethod('PUT', url, content, headers)
    }

    private def executeMethod(String method, String url, Map headers) {
        HttpURLConnection conn = setupConnection(url, headers)
        conn.setRequestMethod(method)
        String data
        conn.getInputStream().withStream {
            data = it.text
        }
        def response = buildResponse(conn)
        response.data = data
        conn.disconnect()
        return response
    }

    private def executeMethod(String method, String url, byte[] content, Map headers) {
        HttpURLConnection conn = setupConnection(url, headers)
        conn.setRequestMethod(method)
        conn.setRequestProperty('Content-Length', "${content.size()}")
        conn.setDoInput(true)
        conn.setDoOutput(true)
        conn.getOutputStream().withStream {
            it.write(content)
        }
        String data
        conn.getInputStream().withStream {
            data = it.text
        }
        def response = buildResponse(conn)
        response.data = data
        conn.disconnect()
        return response
    }

    private HttpURLConnection setupConnection(String url, Map headers) {
        URL targetURL = new URL(url)
        HttpURLConnection conn = (HttpURLConnection)targetURL.openConnection()
        conn.setUseCaches(false)
        conn.setInstanceFollowRedirects(false)
        conn.setRequestProperty('Connection', 'Close')
        for (entry in headers) {
            conn.setRequestProperty(entry.key, entry.value)
        }
        return conn
    }

    private Map buildResponse(HttpURLConnection conn) {
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

}