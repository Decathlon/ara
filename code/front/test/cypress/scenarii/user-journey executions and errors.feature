Feature: Journey

@severity-sanity-check
  Scenario: Check the exeuction cart
  Given the user goes to the home page
    And on the executions and errors page, the user clicks on the actions and job reports button "48"
    Then on the executions and errors page, in the actions and job reports list, the actions button "48" is visible