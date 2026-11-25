# Acceptance test data

The pretty formatter uses features from [gherkin-utils](https://github.com/cucumber/gherkin-utils)
for acceptance testing. These features were used to test `gherkin-utils`
implementation. The outcome should be somewhat similar.

Should there be changes, the Java tests can be used to update the  expected data.

```shell
cd java
UPDATE_EXPECTED_FILES=true mvn test
```
