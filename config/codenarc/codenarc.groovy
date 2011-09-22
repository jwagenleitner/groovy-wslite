ruleset {
    ruleset('rulesets/basic.xml')
    ruleset('rulesets/braces.xml')
    ruleset('rulesets/concurrency.xml')
    ruleset('rulesets/design.xml')
    ruleset('rulesets/dry.xml')
    ruleset('rulesets/exceptions.xml')
    //ruleset('rulesets/formatting.xml')
    ruleset('rulesets/generic.xml')
    ruleset('rulesets/grails.xml')
    ruleset('rulesets/imports.xml')
    ruleset('rulesets/junit.xml')
    ruleset('rulesets/logging.xml')
    ruleset('rulesets/naming.xml') {
        'ConfusingMethodName'  {
            doNotApplyToClassNames='Groovy1059Foo'
        }
    }
    //ruleset('rulesets/security.xml')
    //ruleset('rulesets/serialization.xml')
    ruleset('rulesets/size.xml')
    ruleset('rulesets/unnecessary.xml') {
        exclude 'UnnecessaryReturnKeywordRule'
        exclude 'UnnecessaryReturnKeyword'
    }
    ruleset('rulesets/unused.xml')
}
