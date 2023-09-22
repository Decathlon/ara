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
    # Get last id
    Given path '/api/projects/tests-project/executions/latest'
    When method get
    Then status 200
    And def lastId = response[0].id

    # Run the purge
    Given path '/api/projects/tests-project/purge/force'
    When method delete
    Then status 200

    # Check last execution exists
    Given path '/api/projects/tests-project/executions/latest'
    When method get
    Then status 200
    And def lastIdAfterPurge = response[0].id
    And assert lastIdAfterPurge == lastId
    * def previousId = lastId - 1

    # Check previous execution is purged
    Given path '/api/projects/tests-project/executions/' + previousId
    When method get
    Then status 404

    # Check problem exists
    Given path '/api/projects/tests-project/problems/filter'
    And param page = 0
    And param size = 10
    And request
      """
      {
        "status": null,
        "blamedTeamId": null,
        "name": null,
        "defectId": null,
        "defectExistence": null,
        "rootCauseId": null
      }
      """
    When method post
    Then status 200
    And match response.content[0].id == '#number'
    * def problemId = response.content[0].id

    # Check problem pattern exists
    Given path '/api/projects/tests-project/problems/' + problemId
    When method get
    Then status 200
    And response.patterns == '#[1]'

    # Check error still exists
    Given path '/api/projects/tests-project/problems/' + problemId + '/errors'
    When method get
    Then status 200
    And response.content == '#[1]'
