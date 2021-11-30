@medium-check
Feature: Run the purge executions
  Background:
    * def signIn = call read('classpath:templates/login.feature')
    * configure headers = ({ Authorization: 'Bearer ' + signIn.accessToken })
    * url araBaseUrl
  Scenario: Set the purge duration
    Given path '/api/projects/tests-project/settings/execution.purge.duration.value'
    And request
      """
      {value: "0"}
      """
    When method put
    Then status 200
  Scenario: Set the purge type
    Given path '/api/projects/tests-project/settings/execution.purge.duration.type'
    And request
      """
      {value: "DAY"}
      """
    And header Accept = 'application/json'
    And header Content-Type = 'application/json;charset=UTF-8'
    When method put
    Then status 200
  Scenario: Call force purge execution endpoint
    Given path '/api/projects/tests-project/purge/force'
    When method delete
    Then status 200
