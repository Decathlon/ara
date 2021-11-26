Feature: Connect to home page
  Background:
    * def signIn = call read('classpath:templates/login.feature')
    * configure headers = ({ Authorization: 'Bearer ' + signIn.accessToken })
    * url araBaseUrl
  @sanity-check
  Scenario: Access Home Page
    Given path '/'
    When method get
    Then status 200
  @sanity-check
  Scenario: Get project list
    Given path '/api/projects'
    When method get
    Then status 200
