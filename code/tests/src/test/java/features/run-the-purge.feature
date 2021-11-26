Feature: Run the purge executions
  Background:
    * def signIn = call read('classpath:templates/login.feature')
    * configure headers = ({ Authorization: 'Bearer ' + signIn.accessToken })
    * url araBaseUrl
  @wip
  Scenario: Call force purge execution endpoint
    Given path '/api/projects/the-demo-project/purge/force'
    When method post
    Then status 200
