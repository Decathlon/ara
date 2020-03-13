Feature: Pay

  Background:
    Given the user has products in cart
    And the user chosen a delivery option
    When the user goes to the payment page

  @country-fr
  @country-us
  @severity-sanity-check
  Scenario: Functionalities {{F-Buy-Pay}} & {{F-Buy-Card}}: Pay by Card
    When the user choose the payment "Card"
    And the user validates the payment
    Then the order is accepted

  @country-all
  @severity-medium
  Scenario: Functionalities {{F-Buy-Pay}} & {{F-Buy-Gift}}: Pay by Gift Card
    When the user choose the payment "Gift Card"
    And the user validates the payment
    Then the order is accepted

  @country-us
  @severity-medium
  Scenario: Functionalities {{F-Buy-Pay}} & {{F-Buy-NFC}}: Pay by Mobile NFC
    When the user choose the payment "NFC"
    And the user validates the payment
    Then the order is accepted

  @ignore
  @country-fr
  Scenario: Functionalities {{F-Buy-Pay}} & {{F-Buy-Barter}}: Pay by barter
    When the user choose the payment "Barter"
    And the user validates the payment
    Then the order is accepted
