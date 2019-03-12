@ignore
Feature: Ignored feature

  @country-all
  @severity-sanity-check
  Scenario: Functionality 22: Scenario in ignored feature
    Given A step that works
     When A step number 1 that fails with error "not-run-anyway"

  # Duplicate tags must be de-duplicated (and the multiple same severities should be treated as a single one) _NOT_ in alphabetical order!
  @severity-high
  @severity-high
  @severity-high
  @country-be
  @country-nl
  @country-nl
  @country-nl
  Scenario: Functionality 112: Ignored scenario with undefined step
    Given A step that works
     Then A step that does not exist!
