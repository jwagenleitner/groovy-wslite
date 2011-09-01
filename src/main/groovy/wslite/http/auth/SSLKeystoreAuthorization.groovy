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

import javax.net.ssl.*
import java.security.*

class SSLKeystoreAuthorization implements HTTPAuthorization {
    String keystoreFilePath
    String password

    SSLKeystoreAuthorization() {
    }

    SSLKeystoreAuthorization(String keystoreFilePath, String password) {
        this.keystoreFilePath = keystoreFilePath
        this.password = password
    }
  
    void setKeystoreFilePath(String keystoreFilePath) {
        this.keystoreFilePath = keystoreFilePath
    }
  
    String getKeystoreFilePath() {
        return keystoreFilePath
    }
  
    void setPassword(String password) {
        this.password = password
    }
  
    String getPassword() {
        return password
    }

    void authorize(conn) {
        char[] keystorepass = password.getChars()
        def keystoreFile = new FileInputStream(new File(keystoreFilePath))
        
        // create required keystores and their corresponding manager objects
        def keyStore = KeyStore.getInstance(KeyStore.getDefaultType())

        keyStore.load(keystoreFile, keystorepass)

        def kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(keyStore, keystorepass)

        def tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(keyStore)
        
        // congifure a local SSLContext to use created keystores
        def sc = SSLContext.getInstance("SSL")
        sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom())
        conn.setSSLSocketFactory(sc.getSocketFactory())
    }
}
