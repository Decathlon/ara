Feature: Journey

Background:
  Given executions and errors

@severity-medium
  Scenario: Check the Actions Buttons
    When on the executions and errors page, the user clicks on the actions and job reports button "5"
    Then on the executions and errors page, in the actions and job reports list, the "Actions" button "5" is visible
    And on the executions and errors page, in the actions and job reports list, the "Actions" button "5" is disabled
    And on the executions and errors page, in the actions and job reports list, the "JobReports" button "5" is visible
    And on the executions and errors page, in the actions and job reports list, the "JobReports" button "5" is disabled
    And on the executions and errors page, in the actions and job reports list, the "Execution" button "5" is visible
    And on the executions and errors page, in the actions and job reports list, the "Execution" button "5" is enabled
    And on the executions and errors page, in the actions and job reports list, the "fr_Deployment" button "5" is visible
    And on the executions and errors page, in the actions and job reports list, the "fr_Deployment" button "5" is enabled
    And on the executions and errors page, in the actions and job reports list, the "fr_api" button "5" is visible
    And on the executions and errors page, in the actions and job reports list, the "fr_api" button "5" is enabled
    And on the executions and errors page, in the actions and job reports list, the "fr_desktop" button "5" is visible
    And on the executions and errors page, in the actions and job reports list, the "fr_desktop" button "5" is enabled

@severity-sanity-check
  Scenario: Check Runs
  # OPEN FIRST RUN
    When on the executions and errors page, in the cart "5", the user clicks on the run "fr_api"
    Then on the executions and errors page, in the cart "5", on the run "fr_api", the team "28" is visible
    And on the executions and errors page, in the cart "5", on the run "fr_api", the team "27" is visible
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "26" is hidden
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "27" is hidden
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "28" is hidden
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "29" is hidden
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "-404" is hidden

  # OPEN SECOND RUN
    When on the executions and errors page, in the cart "5", the user clicks on the run "fr_desktop"
    Then on the executions and errors page, in the cart "5", on the run "fr_api", the team "28" is visible
    And on the executions and errors page, in the cart "5", on the run "fr_api", the team "27" is visible
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "26" is visible
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "27" is visible
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "28" is visible
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "29" is visible
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "-404" is visible

  # CONTROL DATAS
    And on the executions and errors page, in the cart "5", on the run "fr_api", in the column "sanity-check", the number of ok is "2", the number of problem is "0", the number of ko is "0", the progress bar is 100% of success, 0% of unhandled and 0% of failed


  # CONTROL DATAS
  # HEADERS QUALITY
    And on the executions and errors page, in the cart "5", on the header, in the column "sanity-check", the quality is "100", the number of OK is "9", the number of KO is "0", the color is "green"
    And on the executions and errors page, in the cart "5", on the header, in the column "high", the quality is "100", the number of OK is "2", the number of KO is "0", the color is "green"
    And on the executions and errors page, in the cart "5", on the header, in the column "medium", the quality is "100", the number of OK is "7", the number of KO is "0", the color is "green"
    And on the executions and errors page, in the cart "5", on the header, in the column "*", the quality is "100", the number of OK is "18", the number of KO is "0", the color is "none"

  # HEADERS THRESHOLD
    And on the executions and errors page, in the cart "5", on the header, in the column "sanity-check", the threshold is "100", the color is "none"
    And on the executions and errors page, in the cart "5", on the header, in the column "high", the threshold is "95", the color is "none"
    And on the executions and errors page, in the cart "5", on the header, in the column "medium", the threshold is "90", the color is "none"
