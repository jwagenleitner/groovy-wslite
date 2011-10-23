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
package wslite.util

class ObjectHelper {

    static String dump(Object delegate) {
        dump([:], delegate)
    }

    static String dump(Map fieldList, Object delegate) {
        if (delegate == null) {
            return 'null'
        }
        def excludedProps = ['metaClass', 'class'] + fieldList.exclude
        def includedProps = fieldList.include
        def sb = new StringBuilder()
        sb.append '<'
        sb.append delegate.getClass().getName()
        sb.append '@'
        sb.append Integer.toHexString(delegate.hashCode())
        def props = delegate.properties
        for (propName in props.keySet()) {
            if (excludedProps.contains(propName)) {
                continue
            }
            if (includedProps && !includedProps.contains(propName)) {
                continue
            }
            sb.append ' '
            sb.append propName
            sb.append '='
            try {
                sb.append props[propName]
            } catch (Exception ex) {
                sb.append ex
            }
        }
        sb.append '>'
        return sb.toString()
    }

}
