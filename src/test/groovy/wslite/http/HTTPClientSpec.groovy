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

    def httpClient
    def conn
    def mockHttpGetRequest = new HTTPRequest(url: 'http://test.com'.toURL(), method: HTTPMethod.GET)
    def mockHttpsGetRequest = new HTTPRequest(url: 'https://foo.org'.toURL(), method: HTTPMethod.GET)

    void setup() {
        conn = new MockHTTPClientConnection()
        httpClient = new HTTPClient()
        httpClient.httpConnectionFactory = [getConnection: { url, proxy=null ->
            conn.URL = url
            return conn
        }, getConnectionUsingTrustStore: {
            url, tsfile, tspassword, proxy=null ->
            conn.URL = url
            conn.setRequestProperty('javax.net.ssl.trustStore', tsfile)
            conn.setRequestProperty('javax.net.ssl.trustStorePassword', tspassword)
            return conn
        }] as HTTPConnectionFactory
    }

    void 'will use its settings if not specified in request'() {
        given:
        httpClient.connectTimeout = 20000
        httpClient.readTimeout = 15000
        httpClient.followRedirects = false
        httpClient.useCaches = false

        when:
        httpClient.execute(mockHttpGetRequest)

        then:
        conn.connectTimeout == 20000
        conn.readTimeout == 15000
        !conn.instanceFollowRedirects
        !conn.useCaches
    }

    void 'request settings will override settings'() {
        given:
        httpClient.connectTimeout = 20000
        httpClient.readTimeout = 15000
        httpClient.followRedirects = false
        httpClient.useCaches = true

        when:
        mockHttpGetRequest.connectTimeout = 7000
        mockHttpGetRequest.readTimeout = 9000
        mockHttpGetRequest.followRedirects = true
        mockHttpGetRequest.useCaches = false
        httpClient.execute(mockHttpGetRequest)

        then:
        conn.connectTimeout == 7000
        conn.readTimeout == 9000
        conn.instanceFollowRedirects
        !conn.useCaches
    }

    void 'client will trust all ssl certs if not overridden on request'() {
        given:
        httpClient.sslTrustAllCerts = true

        when:
        httpClient.execute(mockHttpsGetRequest)

        then:
        conn.hostnameVerifier.verify('foo', null)
    }

    void 'request set to trust all ssl certs will override client'() {
        given:
        httpClient.sslTrustAllCerts = false

        when:
        mockHttpsGetRequest.sslTrustAllCerts = true
        httpClient.execute(mockHttpsGetRequest)

        then:
        conn.hostnameVerifier.verify('foo', null)
    }

    void 'request set to not trust all ssl will override client'() {
        given:
        httpClient.sslTrustAllCerts = true

        when:
        mockHttpsGetRequest.sslTrustAllCerts = false
        httpClient.execute(mockHttpsGetRequest)

        then:
        null == conn.hostnameVerifier
    }

    void 'client will use trust store if not overridden on request'() {
        given:
        httpClient.sslTrustStoreFile = '~/test.jks'

        when:
        httpClient.execute(mockHttpsGetRequest)

        then:
        '~/test.jks' == conn.requestProperties['javax.net.ssl.trustStore']
        null == conn.requestProperties['javax.net.ssl.trustStorePassword']
    }

    void 'request trust store settings will override client'() {
        given:
        httpClient.sslTrustStoreFile = '~/test.jks'
        httpClient.sslTrustStorePassword = "foo"

        when:
        mockHttpsGetRequest.sslTrustStoreFile = '~/usethis.jks'
        httpClient.execute(mockHttpsGetRequest)

        then:
        '~/usethis.jks' == conn.requestProperties['javax.net.ssl.trustStore']
        null == conn.requestProperties['javax.net.ssl.trustStorePassword']
    }

    void 'proxy setting in client'() {
        given:
        httpClient.httpConnectionFactory = Mock(HTTPConnectionFactory)
        httpClient.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress('proxy.example.com', 8080))

        when:
        httpClient.execute(mockHttpsGetRequest)

        then:
        1 * httpClient.httpConnectionFactory.getConnection(mockHttpsGetRequest.url, httpClient.proxy) >> { url, proxy ->
            conn.URL = url
            conn
        }
    }

    void 'proxy with TrustAllSSLCerts'() {
        given:
        httpClient.httpConnectionFactory = Mock(HTTPConnectionFactory)
        httpClient.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress('proxy.example.com', 8080))
        httpClient.sslTrustAllCerts = true

        when:
        httpClient.execute(mockHttpsGetRequest)

        then:
        1 * httpClient.httpConnectionFactory.getConnectionTrustAllSSLCerts(mockHttpsGetRequest.url, httpClient.proxy) >> {
            url, proxy ->
                conn.URL = url
                conn
        }
    }

    void 'proxy with TrustStore'() {
        given:
        httpClient.httpConnectionFactory = Mock(HTTPConnectionFactory)
        httpClient.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress('proxy.example.com', 8080))
        httpClient.sslTrustStoreFile = '~/test.jks'
        httpClient.sslTrustStorePassword = 'test'

        when:
        httpClient.execute(mockHttpsGetRequest)

        then:
        1 * httpClient.httpConnectionFactory.getConnectionUsingTrustStore(
                mockHttpsGetRequest.url,
                httpClient.sslTrustStoreFile,
                httpClient.sslTrustStorePassword,
                httpClient.proxy) >> { url, truststore, password, proxy ->
                    conn.URL = url
                    conn
                }
    }

    void 'proxy setting in request will override client'() {
        given:
        httpClient.httpConnectionFactory = Mock(HTTPConnectionFactory)
        httpClient.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress('proxy.example.com', 8080))
        def testproxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress('proxy2.example.com', 80))

        when:
        mockHttpsGetRequest.proxy = testproxy
        httpClient.execute(mockHttpsGetRequest)

        then:
        1 * httpClient.httpConnectionFactory.getConnection(mockHttpsGetRequest.url, testproxy) >> { url, proxy ->
            conn.URL = url
            conn
        }
    }

    void 'will use default headers if not already set'() {
        httpClient.defaultHeaders['x-foo'] = 'bar'

        when:
        httpClient.execute(mockHttpGetRequest)

        then:
        conn.requestProperties['x-foo'] == 'bar'
    }

    void 'request headers will override default headers'() {
        httpClient.defaultHeaders['x-foo'] = 'bar'
        mockHttpGetRequest.headers['x-foo'] = 'baz'

        when:
        httpClient.execute(mockHttpGetRequest)

        then:
        conn.requestProperties['x-foo'] == 'baz'
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
        requestProperties[k] << v
    }
    def getRequestProperty(k) {
        return requestProperties[k]
    }
    boolean doOutput
    boolean doInput
    def inputStream = [bytes:null]
    def outputStream = [:]
    def errorStream = [bytes:null]
    int responseCode = 200
    String responseMessage = 'OK'
    String contentEncoding = ''
    int contentLength
    String contentType = 'text/xml'
    long date = new Date().time
    long expiration = new Date().time
    long lastModified = new Date().time

    def disconnect() {}
}
