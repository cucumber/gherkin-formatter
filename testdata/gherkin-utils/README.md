# Acceptance test data

The pretty formatter uses features from [gherkin-utils](https://github.com/cucumber/gherkin-utils)
for acceptance testing. These features were used to test `gherkin-utils`
implementation. The outcome should be somewhat similar.

Please do not add any new features to this collection.

Should there be changes, the Java tests can be used to update the expected
features.

```shell
cd java
UPDATE_EXPECTED_FILES=true mvn test
```
