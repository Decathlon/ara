Feature: Buy a product

  @country-all
  @severity-sanity-check
  Scenario: Functionality {{F-Buy-Add}}: Add a product to cart
    Given the user is on the useless "Unicorn baskets" product details page
    When the user clicks on the Add To Cart button
    Then the cart now has 1 product

  @country-all
  @severity-sanity-check
  Scenario: Functionality {{F-Buy-Cart}}: Show cart, nominal case
    Given the user has 1 product in cart
    When the user goes to the cart page
    Then the cart page shows 1 product

  @country-all
  @severity-high
  Scenario: Functionality {{F-Buy-Cart}}: Show cart, average case
    Given the user has 5 products in cart
    When the user goes to the cart page
    Then the cart page shows 5 products

  @country-all
  @severity-medium
  Scenario: Functionality {{F-Buy-Cart}}: Show cart, lots of products
    Given the user has 1000 products in cart
    When the user goes to the cart page
    Then the cart page shows 1000 products

  # @country-xx, to demo "Wrong Country Codes" = "xx" in FUNCTIONALITY ASSIGNATIONS screen
  @country-xx
  @country-fr
  @severity-sanity-check
  Scenario: Functionalities {{F-Buy-Delivery}} & {{F-Buy-Pigeon}}: Choose delivery option
    Given the user has 1 product in cart
    And the user goes to the cart page
    When the user validates the order
    And the user chooses the delivery option "By pigeon"
    Then the user is redirected to the payment page
    And the delivery option is "By pigeon"
    And the delivery price is "1 cent"
