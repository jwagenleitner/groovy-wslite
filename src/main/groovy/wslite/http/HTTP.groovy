/* Copyright 2011-2014 the original author or authors.
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

class HTTP {

    static final String DEFAULT_CHARSET = 'ISO-8859-1' // http://tools.ietf.org/html/rfc2616#section-3.7.1

    static final String CONTENT_TYPE_HEADER = 'Content-Type'
    static final String CONTENT_LENGTH_HEADER = 'Content-Length'
    static final String AUTHORIZATION_HEADER = 'Authorization'
    static final String ACCEPT_HEADER = 'Accept'
    static final String X_HTTP_METHOD_OVERRIDE_HEADER = 'X-HTTP-Method-Override'

}
