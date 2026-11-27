# before Feature
Feature: Comments everywhere
# after Feature

  # after Feature indent
  # before Background indent
  # before Background
  Background: foo

  # after Background
  # before Scenario
  Scenario: foo
  # after Scenario
    # before Given
    Given nothing

  # after Given
  # ####
  # And spaced out
  # ###
  # before Tag
  # after Tag
  @foo
  Scenario: Foo
    Given another

  # before Rule
  Rule:
  # after Rule

    # after Rule indent
    # background comment
    Background: foo

    # middling comment
    # another middling comment
    # scenario comment
    Scenario: foo
