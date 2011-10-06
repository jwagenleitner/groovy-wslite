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

public class HTTPHeaderMap<V> implements Map<String, V> {

    private final Map<String, V> map

    public HTTPHeaderMap() {
        map = new LinkedHashMap<String, V>();
    }

    public HTTPHeaderMap(Map<String, V> map) {
        this.map = new LinkedHashMap<String, V>(map)
    }

    public int size() {
        return map.size()
    }

    public boolean isEmpty() {
        return map.isEmpty()
    }

    public boolean containsKey(Object o) {
        return map.containsKey(getKeyIgnoreCase(o))
    }

    public boolean containsValue(Object o) {
        return map.containsValue(o)
    }

    public V get(Object o) {
        return map.get(getKeyIgnoreCase(o))
    }

    public V put(String s, V v) {
        Object oldKey = getKeyIgnoreCase(s)
        if (oldKey != null) {
            map.remove(oldKey)
        }
        return map.put(s, v)
    }

    public V remove(Object o) {
        return map.remove(getKeyIgnoreCase(o))
    }

    public void putAll(Map<? extends String, ? extends V> map) {
        for (entry in map) {
            put(entry.key, entry.value)
        }
    }

    public void clear() {
        map.clear()
    }

    public Set<String> keySet() {
        return map.keySet()
    }

    public Collection<V> values() {
        return map.values()
    }

    public Set<Map.Entry<String, V>> entrySet() {
        return map.entrySet()
    }

    private String getKeyIgnoreCase(Object key) {
        if (key == null) {
            return null
        }
        String aKey = (String) key
        for (existingKey in map.keySet()) {
            if (aKey.equalsIgnoreCase(existingKey)) {
                return existingKey
            }
        }
        return null
    }
}
