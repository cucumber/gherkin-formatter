<h1 align="center">
  <img alt="" width="75" src="https://github.com/cucumber.png"/>
  <br>
  gherkin-pretty
</h1>
<p align="center">
  <b>Pretty formatter for Gherkin document</b>
</p>

<p align="center">
  <a href="https://search.maven.org/search?q=g:io.cucumber%20AND%20a:gherkin-pretty" style="text-decoration: none"><img src="https://img.shields.io/maven-central/v/io.cucumber/gherkin-pretty.svg?label=Maven%20Central" alt="Latest version on Maven Central"></a>
</p>

This package provides a utility to pretty format Gherkin documents.

```gherkin
Feature: Examples Tables

  Sometimes it can be desirable to run the same scenario multiple times with
  different data each time - this can be done by placing an Examples table
  underneath a Scenario, and use <placeholders> in the Scenario which match the
  table headers.

  ...

  Scenario Outline: Eating cucumbers
    Given there are <start> cucumbers
    When I eat <eat> cucumbers
    Then I should have <left> cucumbers

    @passing
    Examples: These are passing
      | start | eat | left |
      |    12 |   5 |    7 |
      |    20 |   5 |   15 |

    @failing
    Examples: These are failing
      | start | eat | left |
      |    12 |  20 |    0 |
      |     0 |   1 |    0 |

  Scenario Outline: Eating cucumbers with <friends> friends
    Given there are <friends> friends
    And there are <start> cucumbers
    Then each person can eat <share> cucumbers

    Examples:
      | friends | start | share |
      |      11 |    12 |     1 |
      |       1 |     4 |     2 |
      |       0 |     4 |     4 |
```

## Basic features

In the default formatting:
 * Elements are indented relative to their parent.
 * The feature keyword is surrounded by empty lines.
 * The rule keyword is surrounded by empty lines.
 * Descriptions are surrounded by empty lines.
 * Blank lines are converted to empty lines.
 * Consecutive empty lines are collapsed into a single empty line.
 * EOF is preceded by a new line.

### Comment blocks

A sequence of consecutive comment lines is called a comment block. The pretty
formatter treats all comments in the block as a single multi-line text block.  

#### Indentation

When a comment block is indented relative to its parent the relative
orientation of all lines in the block is preserved. For example:   

```gherkin
...
  Scenario Outline: ASCII art
    Given an ASCII art <animal>
#                 /\_/\
#    This is cat ( o.o )
#                 > ^ <
#
...
```

Becomes:

```gherkin
...
  Scenario Outline: ASCII art
    Given an ASCII art <animal>

    #              /\_/\
    # This is cat ( o.o )
    #              > ^ <
...
```

#### Attached and Detached

If the line immediately after a comment block is not empty the comment block is
considered attached to that line. Otherwise, it is detached. Attached comment
blocks stick with their attached line, but other elements may insert an empty
line between themselves and the comment block. Detached comment blocks are always
surrounded by empty lines.

For example:

```gherkin
...
  Scenario Outline: Attached or attached?
    Given ...
    # This comment is attached to the when step
    When ...

    # This comment is detached.

    Then ..

    # This comment is attached to the examples block
    # It always has an empty line before it.
    Examples:
...
```

### Numbers in DataTables

Number like values in data and example tables  are right aligned.

```gherkin
...
    Given a data table with numbers:
      | text | numbers |
      | a    |       1 |
      | ab   |      10 |
      | abc  |     100 |
...
```

A value is number like it if matches the regex:

```regexp
[-+]?(?:\d+(?:[,.]\d+)*(?:[,.]\d+)?|[,.]\d+)(?:E[-+]?\d+)?`
```

## Usage and installation

* [Java](./java/README.md)

## Contributing

Each language implementation validates itself against the examples in the
`testdata` folder. See the different READMEs for more information.
