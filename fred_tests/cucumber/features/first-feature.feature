Feature: Journey

  @severity-sanity-check
  Scenario: Check the status of a build
    Given an user with a demo project
    When the user goes to the home page
    Then the top menu is present
