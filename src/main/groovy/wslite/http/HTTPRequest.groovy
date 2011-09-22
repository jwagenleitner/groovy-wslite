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

import wslite.util.ObjectHelper

class HTTPRequest {
    URL url
    HTTPMethod method

    int connectTimeout = 0
    int readTimeout = 0

    boolean useCaches = false
    boolean followRedirects = true
    boolean sslTrustAllCerts
    String sslTrustStoreFile
    String sslTrustStorePassword
    
    Proxy proxy

    HTTPHeaderMap headers = [:] as HTTPHeaderMap

    byte[] data = null

    boolean isConnectTimeoutSet, isReadTimeoutSet, isUseCachesSet, isFollowRedirectsSet, isSSLTrustAllCertsSet

    void setConnectTimeout(int timeout) {
        this.connectTimeout = timeout
        this.isConnectTimeoutSet = true
    }

    void setReadTimeout(int timeout) {
        this.readTimeout = timeout
        this.isReadTimeoutSet = true
    }

    void setUseCaches(boolean useCaches) {
        this.useCaches = useCaches
        this.isUseCachesSet = true
    }

    void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects
        this.isFollowRedirectsSet = true
    }

    void setSslTrustAllCerts(boolean sslTrustAllCerts) {
        this.sslTrustAllCerts = sslTrustAllCerts
        this.isSSLTrustAllCertsSet = true
    }

    String getContentAsString() {
        if (!data) return ""
        String charset = HTTP.DEFAULT_CHARSET
        if (headers['Content-Type']) {
            charset = HTTP.parseCharsetParamFromContentType(headers['Content-Type']) ?: charset
        }
        return new String(data, charset)
    }

    @Override
    String toString() {
        def excludes = ['isConnectTimeoutSet', 'isReadTimeoutSet', 'isUseCachesSet', 'isFollowRedirectsSet',
                        'isSSLTrustAllCertsSet', 'sslTrustStorePassword', 'data', 'contentAsString']
        ObjectHelper.dump(this, exclude:excludes)
    }
}
