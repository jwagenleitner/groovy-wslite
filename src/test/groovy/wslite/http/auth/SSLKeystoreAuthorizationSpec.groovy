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
package wslite.http.auth

import spock.lang.*
import javax.net.ssl.*
import java.security.*

class SSLKeystoreAuthorizationSpec extends Specification {

    def "sets SSL socket factory"() {
        setup:
        def conn = new SSLMockConnection()
        File.metaClass.constructor = {String s -> null}
        FileInputStream.metaClass.constructor = {File f -> null}

        KeyStore.metaClass.load = {String k, String p -> null}

        KeyManagerFactory.metaClass.init = {String k, String p -> }
        KeyManagerFactory.metaClass.getKeyManagers = { -> null}

        TrustManagerFactory.metaClass.init = {KeyStore ks -> }
        TrustManagerFactory.metaClass.getTrustManagers = { -> null}

        def trustingTrustManager = [
                getAcceptedIssuers: {},
                checkClientTrusted: { arg0, arg1 -> },
                checkServerTrusted: {arg0, arg1 -> }
        ] as X509TrustManager

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, [trustingTrustManager] as TrustManager[], null)

        def factory = sc.getSocketFactory()
        SSLContext.metaClass.getSocketFactory = { -> factory}

        expect:
        def auth = new SSLKeystoreAuthorization(keystore, password)
        auth.authorize(conn)

        conn.factory == factory

        where:
        keystore    | password
        "keystore"  | "password"
    }

}

class SSLMockConnection {
    SSLSocketFactory factory;
    void setSSLSocketFactory(SSLSocketFactory factory) {
        this.factory = factory
    }
}
