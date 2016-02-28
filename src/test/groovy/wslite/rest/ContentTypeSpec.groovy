package wslite.rest

import spock.lang.Specification

class ContentTypeSpec extends Specification {

    void 'json content type matches predefined types or custom media types'() {
        expect:
        ContentType.JSON.contains(type) == result

        where:
        type | result
        'application/json' | true
        'application/javascript' | true
        'text/javascript' | true
        'text/json' | true
        'application/vnd.github.v3+json' | true
    }

    void 'xml content type matches predefined types or custom media types'() {
        expect:
        ContentType.XML.contains(type) == result

        where:
        type | result
        'application/atom+xml' | true
        'application/xhtml+xml' | true
        'text/xml' | true
        'application/xml' | true
        'application/rss+xml' | true
    }
}
