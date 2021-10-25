Feature: Connect to home page
  Background:
    * url authBaseUrl
    * header Authorization = 'Basic ' + authToken
    * form field grant_type = 'client_credentials'
    * method post
    * status 200
    * def accessToken = response.access_token
  @sanity-check
  Scenario: Access Home Page
    Given url araBaseUrl
    And header Authorization = 'Bearer ' + accessToken
    When method get
    Then status 200
