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

class HTTPRequestSpec extends Specification {

    void 'track when connect timeout is set'() {
        when:
        def req = new HTTPRequest(connectTimeout:5000)

        then:
        req.isConnectTimeoutSet
    }

    void 'track when read timeout is set'() {
        when:
        def req = new HTTPRequest(readTimeout:5000)

        then:
        req.isReadTimeoutSet
    }

    void 'track when use caches is set'() {
        when:
        def req = new HTTPRequest(useCaches:false)

        then:
        req.isUseCachesSet
    }

    void 'track when follow redirects is set'() {
        when:
        def req = new HTTPRequest(followRedirects:false)

        then:
        req.isFollowRedirectsSet
    }

    void 'track when trust all SSL certs is set'() {
        when:
        def req = new HTTPRequest(sslTrustAllCerts:false)

        then:
        req.isSSLTrustAllCertsSet
    }

    void 'is set flags should all be false for a new request'() {
        when:
        def req = new HTTPRequest()

        then:
        !req.isConnectTimeoutSet
        !req.isReadTimeoutSet
        !req.isUseCachesSet
        !req.isFollowRedirectsSet
        !req.isSSLTrustAllCertsSet
    }

    void 'headers retrieved case insensitively'() {
        when:
        def req = new HTTPRequest()
        req.headers = ['Content-Type': 'text/xml', Accept: 'text/json']

        then:
        'text/xml' == req.headers.'content-type'
        'text/json' == req.headers['ACCEPT']
    }

    void 'headers overwritten case insensitively'() {
        when:
        def req = new HTTPRequest()
        req.headers = ['Content-Type': 'text/xml', Accept: 'text/json']
        req.headers.accept = 'text/csv'

        then:
        2 == req.headers.size()
        'text/xml' == req.headers.'Content-Type'
        'text/csv' == req.headers.ACCEPT
    }

    void 'headers makes a copy if passed a Map'() {
        when:
        def req = new HTTPRequest()
        def myMapOfHeaders = ['Content-Type': 'text/xml', Accept: 'text/json']
        req.headers = myMapOfHeaders
        myMapOfHeaders.foo = 'bar'

        then:
        3 == myMapOfHeaders.size()
        2 == req.headers.size()
        null == req.headers.foo
    }

    void 'headers do not change original Map'() {
        when:
        def req = new HTTPRequest()
        def myMapOfHeaders = ['Content-Type': 'text/xml', Accept: 'text/json']
        req.headers = myMapOfHeaders
        req.headers.foo = 'bar'

        then:
        2 == myMapOfHeaders.size()
        3 == req.headers.size()
        null == myMapOfHeaders.foo
    }

    void 'headers set individually'() {
        when:
        def req = new HTTPRequest()
        req.headers.'Content-Type' = 'text/plain'

        then:
        'text/plain' == req.headers.'CONTENT-TYPE'
    }

    void 'headers removed case insensitively'() {
        when:
        def req = new HTTPRequest()
        req.headers.'Content-Type' = 'text/plain'
        req.headers.remove('content-TYPE')

        then:
        req.headers.isEmpty()
        0 == req.headers.size()
        null == req.headers.'Content-Type'
    }

    void 'can get content as string if not content type set'() {
        given:
        def request = new HTTPRequest(data: 'foo'.bytes)

        when:
        String content = request.getContentAsString()

        then:
        'foo' == content
   }

   void 'can get content as string if headers has content type with charset'() {
       given:
       def request = new HTTPRequest(data: 'foo'.bytes,
                                    headers:['Content-Type': 'text/xml; charset=UTF-8'])

       when:
       String content = request.getContentAsString()

       then:
       'foo' == content
    }

   void 'can get content as string if headers has content type with no charset'() {
       given:
       def request = new HTTPRequest(data: 'foo'.bytes,
                                     headers:['Content-Type': 'text/xml'])

       when:
       String content = request.getContentAsString()

       then:
       'foo' == content
    }

}
