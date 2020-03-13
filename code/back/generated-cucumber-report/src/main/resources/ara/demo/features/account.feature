# No @country-all, to demo "Wrong Country Codes" = "none" in FUNCTIONALITY ASSIGNATIONS screen
Feature: Account

  # Simulate the team forgot to add "Functionality {{F-Account-Create}}:", to demo ASSIGNATIONS screen
  @severity-sanity-check
  Scenario: Create account
    Given the user is on the account creation page
    When the user enters a new login
    And the user enters a new password
    And the user validates the account creation
    Then the user is connected

  @severity-sanity-check
  Scenario: Functionalities {{F-Account-Login}} & {{F-Not-Found}}: Log in
    Given the user is on the log-in page
    When the user enters a login
    And the user enters a password
    And the user validates the connection
    Then the user is connected
