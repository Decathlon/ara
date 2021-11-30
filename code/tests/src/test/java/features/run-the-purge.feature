@medium-check
Feature: Run the purge executions
  Background:
    * def signIn = call read('classpath:templates/login.feature')
    * configure headers = ({ Authorization: 'Bearer ' + signIn.accessToken })
    * url araBaseUrl
  Scenario: Set the purge settings
    Given path '/api/projects/tests-project/settings/execution.purge.duration.value'
    And request
      """
      {value: "0"}
      """
    When method put
    Then status 200
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
    Given path '/api/projects/tests-project/executions/latest'
    When method get
    Then status 200
    And def lastId = response[0].id

    Given path '/api/projects/tests-project/purge/force'
    When method delete
    Then status 200

    Given path '/api/projects/tests-project/executions/latest'
    When method get
    Then status 200
    And def lastIdAfterPurge = response[0].id
    And assert lastIdAfterPurge == lastId
    * def previousId = lastId - 1

    Given path '/api/projects/tests-project/executions/' + previousId
    When method get
    Then status 404
