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

    def setupConnection(String url, Map headers) {
        def targetURL = new URL(url)
        def conn = targetURL.openConnection()
        conn.setUseCaches(false)
        conn.setInstanceFollowRedirects(false)
        conn.setRequestProperty('Connection', 'Close')
        for (entry in headers) {
            conn.setRequestProperty(entry.key, entry.value)
        }
        return conn
    }        

    def post(String url, byte[] content, Map headers=[:]) {
        def conn = setupConnection(url, headers)
        conn.setRequestMethod('POST')
        conn.setRequestProperty('Content-Length', "${content.size()}")
        conn.setDoInput(true)
        conn.setDoOutput(true)
        def httpout = conn.getOutputStream()
        httpout.write(content)
        httpout.flush()
        httpout.close()
        def httpin = conn.getInputStream()
        def result = httpin.text
        conn.disconnect()
        return result
    }

}