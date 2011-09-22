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

import java.util.regex.Pattern

class ContentTypeHeader {

    private final static String TOKEN = '''(?x)
        [
            \\p{ASCII}
            &&
            [^
                \'
                \\p{Cntrl}
                \\s
                \\;
                \\/
                \\=
                \\[
                \\]
                \\(
                \\)
                \\<
                \\>
                \\@
                \\,
                \\:
                \\\
                \\"
                \\?
            ]
        ]+'''

    private final static Pattern MEDIATYPE_PATTERN = Pattern.compile(
        '(' + TOKEN + '/' + TOKEN + ')'
    )

    private final static Pattern CHARSET_PATTERN = Pattern.compile(
        '(?i)charset\\s*=\\s*' +
          '[\"|\']*' +
          '(' + TOKEN + ')' +
          '[\"|\']*'
    )

    final String contentType
    final String mediaType
    final String charset

    ContentTypeHeader(final String contentType) {
        if (contentType) {
            this.contentType = contentType
            this.mediaType = parseMediaType(contentType)
            this.charset = parseCharset(contentType)
        }
    }

    @Override
    String toString() {
        return contentType
    }

    private String parseMediaType(String contentType) {
        def m = contentType =~ MEDIATYPE_PATTERN
        return m.size() > 0 ? m[0][1] : null
    }

    private String parseCharset(String contentType) {
        def m = contentType =~ CHARSET_PATTERN
        return m.size() > 0 ? m[0][1] : null
    }

}
