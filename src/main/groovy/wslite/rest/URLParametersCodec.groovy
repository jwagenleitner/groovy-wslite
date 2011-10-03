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
package wslite.rest

class URLParametersCodec {

    String encode(Map params) {
        if (!(params instanceof Map)) {
            return null
        }
        def encodedList = []
        for (entry in params) {
            if (entry.value instanceof List) {
                for (item in entry.value) {
                    encodedList << urlEncodePair(entry.key, item)
                }
                continue
            }
            encodedList << urlEncodePair(entry.key, entry.value)
        }
        return encodedList.join('&')
    }

    Map decode(String urlEncodedParams) {
        Map params = [:]
        def pairs = urlEncodedParams.split('&')
        for (pair in pairs) {
            String key
            String value
            (key, value) = pair.split('=')
            key = URLDecoder.decode(key)
            value = URLDecoder.decode(value)
            if (!params.containsKey(key)) {
                params[key] = value
                continue
            }
            def existingValue = params[key]
            if (existingValue instanceof List) {
                params[key] << value
            } else {
                params[key] = [existingValue, value]
            }
        }
        return params
    }

    private static String urlEncodePair(key, value) {
        if (!key) {
            return ''
        }
        value = value ?: ''
        return "${URLEncoder.encode(key.toString())}=${URLEncoder.encode(value.toString())}"
    }

}
