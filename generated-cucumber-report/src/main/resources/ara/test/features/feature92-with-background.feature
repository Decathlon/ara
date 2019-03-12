@country-nl
@severity-high
Feature: Feature with background

  Background:
    Given A step that works
      And A step number 1 that fails with error "bad-background"

  @another-before
  Scenario: Scenario with background 1
    Given A step that works

  Scenario: Scenario with background 2
    Given A step that works
