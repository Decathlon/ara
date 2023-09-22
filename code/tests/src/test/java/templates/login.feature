Feature: Fetch OAuth2 accessToken
  Scenario: OAuth2 client credentials login
    Given url authBaseUrl
    And header Authorization = 'Basic ' + authToken
    And form field grant_type = 'client_credentials'
    When method post
    Then status 200
    And def accessToken = response.access_token
