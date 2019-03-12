@country-nl
@severity-high
Feature: Feature with tags

  Scenario: Scenario without tags (inheriting tags from feature)
    Given A step that works

  @country-be
  @severity-high
  Scenario: Functionality not, an & id: Scenario with tags (merging tags with feature ones)
    Given A step that works

  Scenario: Functionality 1: Trying to assign a scenario to a folder
    Given A step that works
