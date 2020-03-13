# Comments for feature 2
@feature-2-tag
Feature: Feature 2
  I want to run a feature with errors (take 2)

  @severity-high @country-nl
  @fail-on-before @another-before
  Scenario: Fail before it
    Given A step that works

  @severity-medium @add-structured-embeddings
  Scenario: Table step
    Given These values are true:
      | Value 1       |  1 |
      | Another value | 42 |
      And A step number 4 that fails with error "string parameter 4"

  @severity-high
  @fail-on-after
  Scenario: Fail after it
    Given A step that works
