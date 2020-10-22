Feature: ASN creation on Seller Toolbox

  @severity-High
  Scenario: Check the ASN creation for 1 product
    Given a seller with MF contract
    When the user connects to his toolbox
    And the user create an ASN for 1 product
    Then the ASN should be created 
    And the ASN should be displayed on the dedicated page
    And the ASN should be displayed on the OS front page
