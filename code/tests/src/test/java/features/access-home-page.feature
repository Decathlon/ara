Feature: Connect to home page
  Background:
    * def signIn = call read('classpath:templates/login.feature')
    * configure headers = { 'Authorization': 'Bearer #(signIn.accessToken)' }
  @sanity-check
  Scenario: Access Home Page
    Given url araBaseUrl
    When method get
    Then status 200
