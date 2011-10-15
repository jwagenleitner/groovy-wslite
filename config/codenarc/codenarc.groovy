ruleset {
    ruleset('rulesets/basic.xml') {
        'ExplicitCallToEqualsMethod' {
            doNotApplyToClassNames = 'HTTPHeaderMap'
        }
    }
    ruleset('rulesets/braces.xml')
    ruleset('rulesets/concurrency.xml')
    ruleset('rulesets/design.xml')
    ruleset('rulesets/dry.xml') {
        exclude 'DuplicateStringLiteral'
    }
    ruleset('rulesets/exceptions.xml') {
        'CatchException' {
            doNotApplyToClassNames = 'HTTPClient,RESTClient'
        }
    }
    ruleset('rulesets/generic.xml') {
        'StatelessClass' {
            doNotApplyToClassNames = 'HTTPClient,HTTPClientException,HTTPMethod,HTTPRequest,HTTPResponse, ' +
                    'HTTPBasicAuthorization,RESTClient,ContentBuilder'
        }
    }
    ruleset('rulesets/grails.xml')
    ruleset('rulesets/imports.xml')
    ruleset('rulesets/junit.xml')
    ruleset('rulesets/logging.xml')
    ruleset('rulesets/naming.xml')
    ruleset('rulesets/size.xml')
    ruleset('rulesets/unnecessary.xml') {
        'UnnecessaryGetter' {
            doNotApplyToClassNames = 'HTTPConnectionFactory,HTTPBasicAuthorization'
        }
        exclude 'UnnecessaryReturnKeywordRule'
        exclude 'UnnecessaryReturnKeyword'
        exclude 'UnnecessaryObjectReferences'
    }
    ruleset('rulesets/unused.xml')
}
