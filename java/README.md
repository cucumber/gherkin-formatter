[![Maven Central](https://img.shields.io/maven-central/v/io.cucumber/gherkin-pretty.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:io.cucumber%20AND%20a:gherkin-pretty)

Gherkin Pretty Usage and Installation
=====================================

## Installation

Download it from Maven Central.

## Usage

Use [io.cucumber:gherkin](https://github.com/cucumber/gherkin) to parse a
feature file. Then format it with Gherkin `GherkinPrettyFormatter` formatter. 

```java
GherkinParser parser = GherkinParser.builder()
        .includePickles(false)
        .build();

parser.parse(Path.of("path/to/example.feature"))
        .map(Envelope::getGherkinDocument)
        .filter(Optional::isPresent)
        .flatMap(Optional::stream)
        .findFirst()
        .ifPresent(gherkinDocument -> {
            var pretty = GherkinPrettyFormatter.builder().build();
            var formattedDocument = pretty.format(gherkinDocument)
            // Use formattedDocument for something 
        });
```
