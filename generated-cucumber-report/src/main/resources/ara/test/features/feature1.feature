# Comments for feature 1
@feature-1-tag @country-nl
Feature: Feature 1
  I want to run a feature with errors (take 1)

  @severity-sanity-check @country-nl
  Scenario: Functionality 111: Fail with two errors
    Given A step that works
     Then A step number 1 that fails with error "string parameter 1"
      And A step number 2 that fails with error "string parameter 2"

  @severity-medium       @country-be
  Scenario: Functionalities 112 & 113: Pass
    Given A step that works

  @severity-high         @country-all
  Scenario Outline: Functionality 113: Fail with name <name>
    When A step number 3 that fails with error "<name>"

    Examples:
      | name      |
      | example 1 |
      | example 2 |
