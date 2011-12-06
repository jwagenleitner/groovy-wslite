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
package wslite.soap

class SOAP {

    static final String SOAP11_NS = 'http://schemas.xmlsoap.org/soap/envelope/'
    static final String SOAP12_NS = 'http://www.w3.org/2003/05/soap-envelope'
    static final String SOAP_NS_PREFIX = 'soap-env'

    static final String SOAP_V11_MEDIA_TYPE = 'text/xml'
    static final String SOAP_V12_MEDIA_TYPE = 'application/soap+xml'

    static final String DEFAULT_CHAR_ENCODING = 'UTF-8'

    static final String SOAP_ACTION_HEADER = 'SOAPAction'
    static final String SOAP_ACTION_V12_HEADER = 'action'
    

    static final String ENVELOPE_ELEMENT_NAME = 'Envelope'
    static final String HEADER_ELEMENT_NAME = 'Header'
    static final String BODY_ELEMENT_NAME = 'Body'
    static final String FAULT_ELEMENT_NAME = 'Fault'

}
