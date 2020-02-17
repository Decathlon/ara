Feature: A test of tricky duplicate scenarios and outlines
  All scenarios have the same name, steps and errors, but they must be considered distinctly

  @severity-high
  @fail-on-before
  Scenario: Duplicate scenario
    Given A step that works

  @severity-high
  @fail-on-before
  Scenario: Duplicate scenario
    Given A step that works

  @severity-high
  @fail-on-before
  Scenario Outline: Duplicate scenario
    Given A step that works

    Examples:
      | name      |
      | example 1 |
      | example 2 |

  @ignore
  @country-nl
  @country-be
  Scenario: Functionality 31: Ignored scenario
    Given A step that works
    And A step that works
