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

    HTTPRequest request = new HTTPRequest()
    
    void 'track when connect timeout is set'() {
        when:
        request.connectTimeout = 5000

        then:
        request.isConnectTimeoutSet
    }

    void 'track when read timeout is set'() {
        when:
        request.readTimeout = 5000

        then:
        request.isReadTimeoutSet
    }

    void 'track when use caches is set'() {
        when:
        request.useCaches = false

        then:
        request.isUseCachesSet
    }

    void 'track when follow redirects is set'() {
        when:
        request.followRedirects = false

        then:
        request.isFollowRedirectsSet
    }

    void 'track when trust all SSL certs is set'() {
        when:
        request.sslTrustAllCerts = false

        then:
        request.isSSLTrustAllCertsSet
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
        request.headers = ['Content-Type': 'text/xml', Accept: 'text/json']

        then:
        'text/xml' == request.headers.'content-type'
        'text/json' == request.headers['ACCEPT']
    }

    void 'headers overwritten case insensitively'() {
        when:
        request.headers = ['Content-Type': 'text/xml', Accept: 'text/json']
        request.headers.accept = 'text/csv'

        then:
        2 == request.headers.size()
        'text/xml' == request.headers.'Content-Type'
        'text/csv' == request.headers.ACCEPT
    }

    void 'headers makes a copy if passed a Map'() {
        given:
        def myMapOfHeaders = ['Content-Type': 'text/xml', Accept: 'text/json']
        
        when:
        request.headers = myMapOfHeaders
        myMapOfHeaders.foo = 'bar'

        then:
        3 == myMapOfHeaders.size()
        2 == request.headers.size()
        null == request.headers.foo
    }

    void 'headers do not change original Map'() {
        given:
        def myMapOfHeaders = ['Content-Type': 'text/xml', Accept: 'text/json']
        
        when:
        request.headers = myMapOfHeaders
        request.headers.foo = 'bar'

        then:
        2 == myMapOfHeaders.size()
        3 == request.headers.size()
        null == myMapOfHeaders.foo
    }

    void 'headers set individually'() {
        when:
        request.headers.'Content-Type' = 'text/plain'

        then:
        'text/plain' == request.headers.'CONTENT-TYPE'
    }

    void 'headers removed case insensitively'() {
        when:
        request.headers.'Content-Type' = 'text/plain'
        request.headers.remove('content-TYPE')

        then:
        request.headers.isEmpty()
        0 == request.headers.size()
        null == request.headers.'Content-Type'
    }

    void 'can get content as string if not content type set'() {
        when:
        request.data = 'foo'.bytes

        then:
        'foo' == request.getContentAsString()
   }

   void 'can get content as string if headers has content type with charset'() {
       when:
       request.data = 'foo'.bytes
       request.headers = ['Content-Type': 'text/xml; charset=UTF-8']

       then:
       'foo' == request.getContentAsString()
    }

   void 'can get content as string if headers has content type with no charset'() {
       when:
       request.data = 'foo'.bytes
       request.headers = ['Content-Type': 'text/xml']

       then:
       'foo' == request.getContentAsString()
    }

}
