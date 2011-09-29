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

class HTTPHeaderMap<String,String> extends LinkedHashMap<String, String> {

    @Override
    String get(String key) {
         super.get(getKeyIgnoreCase(key) ?: key)
    }

    @Override
    String put(String k, String v) {
        String key = getKeyIgnoreCase(k)
        if (key && !key.equals(k)) {
            super.remove(key)
        }
        super.put(k, v)
    }

    @Override
    String remove(String key) {
        super.remove(getKeyIgnoreCase(key) ?: key)
    }

    @Override
    boolean containsKey(String key) {
        return getKeyIgnoreCase(key) != null
    }

    private String getKeyIgnoreCase(String key) {
        if (!key) {
            return key
        }
        for (k in this.keySet()) {
            if (k && k.toLowerCase() == key.toLowerCase()) {
                return k
            }
        }
        return null
    }

}
