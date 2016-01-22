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
package wslite.http;

public class HTTPClientException extends RuntimeException {

    private HTTPRequest request;
    private HTTPResponse response;

    public HTTPClientException() { }
    
    public HTTPClientException(String message) {
        super(message);
    } 
    
    public HTTPClientException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public HTTPClientException(Throwable cause) {
        super(cause);
    }

    public HTTPClientException(String message, HTTPRequest request, HTTPResponse response) {
        super(message);
        this.request = request;
        this.response = response;
    }

    public HTTPClientException(String message, Throwable cause, HTTPRequest httpRequest, HTTPResponse httpResponse) {
        super(message, cause);
        this.request = httpRequest;
        this.response = httpResponse;
    }
    
    public HTTPRequest getRequest() {
        return request;
    }
    
    public HTTPResponse getResponse() {
        return response;
    }

}
