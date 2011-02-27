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

import spock.lang.*

class HTTPClientSpec extends Specification {

    def httpc = new HTTPClient()
    def conn = new MockHTTPClientConnection()
    def testURL = "http://test.com".toURL()

    private def getHTTPConnectionFactory(conn) {
        [getConnection:{url, proxy=null -> conn.URL = url; conn}] as HTTPConnectionFactory
    }

    def "will use its settings if not specified in request"() {
        given:
        httpc.httpConnectionFactory = getHTTPConnectionFactory(conn)
        httpc.connectTimeout = 20000
        httpc.readTimeout = 15000

        when:
        def request = new HTTPRequest(url:testURL, method:HTTPMethod.GET)
        httpc.execute(request)

        then:
        conn.connectTimeout == 20000
        conn.readTimeout == 15000
    }

    def "request settings will override settings"() {
        given:
        httpc.httpConnectionFactory = getHTTPConnectionFactory(conn)
        httpc.connectTimeout = 20000
        httpc.readTimeout = 15000

        when:
        def request = new HTTPRequest(url:testURL, method:HTTPMethod.GET)
        request.connectTimeout = 7000
        request.readTimeout = 9000
        httpc.execute(request)

        then:
        conn.connectTimeout == 7000
        conn.readTimeout == 9000
    }

}

class MockHTTPClientConnection {

    URL URL
    def requestProperties = [:]
    def headerFields = [:]

    String requestMethod
    int connectTimeout
    int readTimeout
    boolean useCaches
    boolean instanceFollowRedirects
    def SSLSocketFactory
    def hostnameVerifier
    def setRequestProperty(k, v) {
        requestProperties[k] = v
    }
    def addRequestProperty(k, v) {
        if (!requestProperties[k]) requestProperties[k] = v
    }
    boolean doOutput
    boolean doInput
    def inputStream = [bytes:null]
    def outputStream = [:]
    def errorStream = [bytes:null]
    int responseCode = 200
    String responseMessage = "OK"
    String contentEncoding = ""
    int contentLength
    String contentType = "text/xml"
    long date = new Date().time
    long expiration = new Date().time
    long lastModified = new Date().time

    def disconnect() {}
}
