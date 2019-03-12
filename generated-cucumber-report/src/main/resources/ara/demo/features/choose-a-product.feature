@country-all
Feature: Choose a product

  @severity-medium
  Scenario: Functionality {{F-Marketing-Home}}: Have a friendly home page
    Given the user is on the friendly home page
    When the user pauses the annoying carousel
    Then the annoying carousel finally stops making user's head spin

  @severity-sanity-check
  Scenario: Functionality {{F-Catalog-List}}: List all our useless products
    Given the user is on the useless-products listing page
    When the user counts the visible products
    Then there are 3 useless products

  @severity-medium
  Scenario: Functionality {{F-Catalog-Details}}: Show a product with irresistible details
    Given the user is on the useless "Unicorn baskets" product details page
    When the user clicks on Reviews
    Then the review 1 is "Don't buy them: there is only one basket in the box!"

  # Don't tag it @severity-high to highlight how such missing severity is reported
  Scenario: Functionality {{F-Marketing-Sales}}: Sales Price on product details page
    Given the "Tuning stand-up paddle" product is on sale with a "50%" reduction
    When the user goes to the product details page
    Then the displayed price reduction is "50%"

  @ignore
  @severity-medium
  Scenario: Functionality {{F-Marketing-Sales}}: A 100% sales percentage should produce nondeterministic results
    Given the "Square skate-wheels" product is on sale with a "100%" reduction
    When the user goes to the product details page
    Then the displayed price reduction is "strange"
