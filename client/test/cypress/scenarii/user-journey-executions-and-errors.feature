Feature: Journey Executions and Errors

Background:
  Given executions and errors


@severity-medium
  Scenario: Discard an execution
    When on the executions and errors page, in the cart "5", the user clicks on the actions and job reports button
    Then on the executions and errors page, in the cart "5", in the actions and job reports list, the "DiscardExecution" button is enabled
     And on the executions and errors page, in the cart "5", in the actions and job reports list, the "DiscardExecution" button is visible
     And on the executions and errors page, in the cart "5", in the actions and job reports list, the "DiscardExecution" button is named "DISCARD EXECUTION"
     And on the executions and errors page, in the cart "5", in the actions and job reports list, the "UndiscardExecution" button is not visible

    When on the executions and errors page, in the cart "5", in the actions and job reports list, the user clicks on the "DiscardExecution" button
    Then on the executions and errors page, in the cart "5", the discard execution modal opens

    # ADD - CANCEL
    When on the executions and errors page, in the cart "5", in the discard execution modal, the user types the discard reason "Deploiement problem"
     And on the executions and errors page, in the cart "5", in the discard execution modal, the user clicks on "Cancel" button
    Then on the executions and errors page, in the cart "5", on the header, the discard reason "Deploiement problem" is not displayed
     And on the executions and errors page, in the cart "5", the discard execution modal closes
 
    # ADD - SAVE
    When on the executions and errors page, in the cart "5", the user clicks on the actions and job reports button
     And on the executions and errors page, in the cart "5", in the actions and job reports list, the user clicks on the "DiscardExecution" button
     And on the executions and errors page, in the cart "5", in the discard execution modal, the user types the discard reason "Deploiement problem"
     And on the executions and errors page, in the cart "5", in the discard execution modal, the user clicks on "Save" button
    Then on the executions and errors page, in the cart "5", on the header, the discard reason "Deploiement problem" is displayed
     And on the executions and errors page, in the cart "5", the discard execution modal closes

    # CHANGE REASON
    When on the executions and errors page, in the cart "5", the user clicks on the actions and job reports button
    Then on the executions and errors page, in the cart "5", in the actions and job reports list, the "DiscardExecution" button is enabled
     And on the executions and errors page, in the cart "5", in the actions and job reports list, the "DiscardExecution" button is visible
     And on the executions and errors page, in the cart "5", in the actions and job reports list, the "DiscardExecution" button is named "CHANGE DISCARD REASON"
     And on the executions and errors page, in the cart "5", in the actions and job reports list, the "UndiscardExecution" button is visible

    When on the executions and errors page, in the cart "5", in the actions and job reports list, the user clicks on the "DiscardExecution" button
    Then on the executions and errors page, in the cart "5", the discard execution modal opens

    When on the executions and errors page, in the cart "5", in the discard execution modal, the user types the discard reason "New Deploiement problem"
     And on the executions and errors page, in the cart "5", in the discard execution modal, the user clicks on "Update" button
    Then on the executions and errors page, in the cart "5", on the header, the discard reason "New Deploiement problem" is displayed

    # DELETE - CANCEL
    When on the executions and errors page, in the cart "5", the user clicks on the actions and job reports button
     And on the executions and errors page, in the cart "5", in the actions and job reports list, the user clicks on the "UndiscardExecution" button
    Then on the executions and errors page, in the cart "5", the undiscard execution modal opens
    When on the executions and errors page, in the cart "5", in the undiscard execution modal, the user clicks on "Cancel" button
    Then on the executions and errors page, in the cart "5", on the header, the discard reason "New Deploiement problem" is displayed
     And on the executions and errors page, in the cart "5", the undiscard execution modal closes

    # DELETE - SAVE
    When on the executions and errors page, in the cart "5", the user clicks on the actions and job reports button
     And on the executions and errors page, in the cart "5", in the actions and job reports list, the user clicks on the "UndiscardExecution" button
     And on the executions and errors page, in the cart "5", in the undiscard execution modal, the user clicks on "Save" button
    Then on the executions and errors page, in the cart "5", on the header, the discard reason "New Deploiement problem" is not displayed
     And on the executions and errors page, in the cart "5", the undiscard execution modal closes

    
@severity-medium
  Scenario: Check Ignored Scenarios
    Then on the executions and errors page, on ignored scenarios part, on the header, in the column "sanity-check", there is no ignored scenario
    And on the executions and errors page, on ignored scenarios part, on the header, in the column "high", there is 34% - i.e. "1/3" - of ignored scenarios
    And on the executions and errors page, on ignored scenarios part, on the header, in the column "medium", there is 13% - i.e. "1/8" - of ignored scenarios
    And on the executions and errors page, on ignored scenarios part, on the header, in the column "*", there is 10% - i.e. "2/20" - of ignored scenarios
    And on the executions and errors page, on ignored scenarios part, on the run "api", in the column "sanity-check", there is no ignored scenario
    And on the executions and errors page, on ignored scenarios part, on the run "api", in the column "high", there is no ignored scenario
    And on the executions and errors page, on ignored scenarios part, on the run "api", in the column "medium", there is no ignored scenario
    And on the executions and errors page, on ignored scenarios part, on the run "api", in the column "*", there is no ignored scenario
    And on the executions and errors page, on ignored scenarios part, on the run "web", in the column "sanity-check", there is no ignored scenario
    And on the executions and errors page, on ignored scenarios part, on the run "web", in the column "high", there is 34% - i.e. "1/3" - of ignored scenarios
    And on the executions and errors page, on ignored scenarios part, on the run "web", in the column "medium", there is 17% - i.e. "1/6" - of ignored scenarios
    And on the executions and errors page, on ignored scenarios part, on the run "web", in the column "*", there is 13% - i.e. "2/16" - of ignored scenarios
    When on the executions and errors page, on ignored scenarios part, on the run "web", in the column "high", the user clicks on the ignored scenarios
    Then on the executions and errors page, on ignored scenarios part, the ignored scenarios for the run "web" and the severity "high" are displayed
    And on the executions and errors page, on ignored scenarios part, the ignored scenarios for "Feature A" contain 1 scenario including "Functionalities 1 & 2: Feature A" and its severity is "high"
    When on the executions and errors page, on ignored scenarios part, on the run "web", in the column "medium", the user clicks on the ignored scenarios
    Then on the executions and errors page, on ignored scenarios part, the ignored scenarios for the run "web" and the severity "medium" are displayed
    And on the executions and errors page, on ignored scenarios part, the ignored scenarios for "Feature B" contain 1 scenario including "Functionality 3: Feature B" and its severity is "medium"
    When on the executions and errors page, on ignored scenarios part, on the run "web", in the column "*", the user clicks on the ignored scenarios
    Then on the executions and errors page, on ignored scenarios part, the ignored scenarios for the run "web" and the severity "*" are displayed
    And on the executions and errors page, on ignored scenarios part, the ignored scenarios for "Feature A" contain 1 scenario including "Functionalities 1 & 2: Feature A" and its severity is "high"
    And on the executions and errors page, on ignored scenarios part, the ignored scenarios for "Feature B" contain 1 scenario including "Functionality 3: Feature B" and its severity is "medium"

@severity-medium
  Scenario: Navigate between latest executions and all executions
    Then on the executions and errors page, the button "Show Raw Executions" is visible
    And on the executions and errors page, the button "Go back to Executions Dashboard" is not visible
    When on the executions and errors page, the user clicks on the button "Show Raw Executions"
    Then on the executions and errors page, the button "Show Raw Executions" is not visible
    And on the executions and errors page, the button "Go back to Executions Dashboard" is visible
    And on the executions and errors page, the list of all executions is visible
    When on the executions and errors page, the user clicks on the button "Go back to Executions Dashboard"
    Then on the executions and errors page, the latest executions are visible


@severity-medium
  Scenario: Check the Actions Buttons
    When on the executions and errors page, in the cart "5", the user clicks on the actions and job reports button
    Then on the executions and errors page, in the cart "5", in the actions and job reports list, the "Actions" button is visible
    And on the executions and errors page, in the cart "5", in the actions and job reports list, the "Actions" button is disabled
    And on the executions and errors page, in the cart "5", in the actions and job reports list, the "JobReports" button is visible
    And on the executions and errors page, in the cart "5", in the actions and job reports list, the "JobReports" button is disabled
    And on the executions and errors page, in the cart "5", in the actions and job reports list, the "Execution" button is visible
    And on the executions and errors page, in the cart "5", in the actions and job reports list, the "Execution" button is enabled
    And on the executions and errors page, in the cart "5", in the actions and job reports list, the "fr_Deployment" button is visible
    And on the executions and errors page, in the cart "5", in the actions and job reports list, the "fr_Deployment" button is enabled
    And on the executions and errors page, in the cart "5", in the actions and job reports list, the "fr_api" button is visible
    And on the executions and errors page, in the cart "5", in the actions and job reports list, the "fr_api" button is enabled
    And on the executions and errors page, in the cart "5", in the actions and job reports list, the "fr_desktop" button is visible
    And on the executions and errors page, in the cart "5", in the actions and job reports list, the "fr_desktop" button is enabled

@severity-sanity-check
  Scenario: Check Header & Navigate between previous and next execution

  # VERSION AND DATE
    Then on the executions and errors page, in the cart "5", the version is "e023ff218f5ef838cf635ed8842572f99b726fb8" and the build date is "Feb 28, 2020 - 08:55"
    And on the executions and errors page, in the cart "5", the test date is "Feb 28, 2020 - 09:00"
  
  # EXECUTIONS NAVIGATION
    And on the executions and errors page, in the cart "5", the "Previous" execution button is clickable
    And on the executions and errors page, in the cart "5", the "Next" execution button is not clickable
  # Go to the previous execution  
    When on the executions and errors page, in the cart "5", the user clicks on the "Previous" execution button
    And on the executions and errors page, in the cart "4", the version is "1a4b22b8c9a55f666f66666d7e856d210c05e64" and the build date is "Feb 27, 2020 - 08:55"
    And on the executions and errors page, in the cart "4", the test date is "Feb 27, 2020 - 09:00"
    And on the executions and errors page, in the cart "4", the "Previous" execution button is clickable
    And on the executions and errors page, in the cart "4", the "Next" execution button is clickable
  # Go to the next execution  
    When on the executions and errors page, in the cart "4", the user clicks on the "Next" execution button
    And on the executions and errors page, in the cart "5", the version is "e023ff218f5ef838cf635ed8842572f99b726fb8" and the build date is "Feb 28, 2020 - 08:55"
    And on the executions and errors page, in the cart "5", the test date is "Feb 28, 2020 - 09:00"
    And on the executions and errors page, in the cart "5", the "Previous" execution button is clickable
    And on the executions and errors page, in the cart "5", the "Next" execution button is not clickable


@severity-sanity-check
  Scenario: Check Runs - everything is Green
    # HEADERS QUALITY
    Then on the executions and errors page, in the cart "5", on the header, in the column "sanity-check", the quality is 100, the number of OK is 9, the number of KO is 0, the color is "green"
    And on the executions and errors page, in the cart "5", on the header, in the column "high", the quality is 100, the number of OK is 2, the number of KO is 0, the color is "green"
    And on the executions and errors page, in the cart "5", on the header, in the column "medium", the quality is 100, the number of OK is 7, the number of KO is 0, the color is "green"
    And on the executions and errors page, in the cart "5", on the header, in the column "*", the quality is 100, the number of OK is 18, the number of KO is 0, the color is "none"

  # HEADERS THRESHOLD
    And on the executions and errors page, in the cart "5", on the header, in the column "sanity-check", the threshold is 100, the color is "none"
    And on the executions and errors page, in the cart "5", on the header, in the column "high", the threshold is 95, the color is "none"
    And on the executions and errors page, in the cart "5", on the header, in the column "medium", the threshold is 90, the color is "none"

  # HEADERS CONTROL DATAS
    And on the executions and errors page, in the cart "5", on the run "fr_api", in the column "sanity-check", the number of ok is 2, the number of problem is 0, the number of ko is 0
    And on the executions and errors page, in the cart "5", on the run "fr_api", in the column "medium", the number of ok is 2, the number of problem is 0, the number of ko is 0
    And on the executions and errors page, in the cart "5", on the run "fr_api", in the column "*", the number of ok is 4, the number of problem is 0, the number of ko is 0

    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "sanity-check", the number of ok is 7, the number of problem is 0, the number of ko is 0
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "high", the number of ok is 2, the number of problem is 0, the number of ko is 0
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "medium", the number of ok is 5, the number of problem is 0, the number of ko is 0
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "*", the number of ok is 14, the number of problem is 0, the number of ko is 0

  # RUNS HIDDEN  
    And on the executions and errors page, in the cart "5", on the run "fr_api", the team "1" is hidden
    And on the executions and errors page, in the cart "5", on the run "fr_api", the team "2" is hidden
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "1" is hidden
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "2" is hidden
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "3" is hidden
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "4" is hidden
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "-404" is hidden
    
  # RUN1 - OPEN FIRST RUN
    When on the executions and errors page, in the cart "5", the user clicks on the run "fr_api"
    Then on the executions and errors page, in the cart "5", on the run "fr_api", the team "1" is visible
    And on the executions and errors page, in the cart "5", on the run "fr_api", the team "2" is visible
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "1" is hidden
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "2" is hidden
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "3" is hidden
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "4" is hidden
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "-404" is hidden

  # RUN1 - CONTROL DATAS
  # TEAM 1
    And on the executions and errors page, in the cart "5", on the run "fr_api", in the column "sanity-check", for the team "1", the progress bar is 100.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "5", on the run "fr_api", in the column "*", for the team "1", the progress bar is 100.0% of success and 0.0% of unhandled
  # TEAM 2
    And on the executions and errors page, in the cart "5", on the run "fr_api", in the column "sanity-check", for the team "2", the progress bar is 100.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "5", on the run "fr_api", in the column "medium", for the team "2", the progress bar is 100.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "5", on the run "fr_api", in the column "*", for the team "2", the progress bar is 100.0% of success and 0.0% of unhandled
 
  # RUN2 - OPEN SECOND RUN
    When on the executions and errors page, in the cart "5", the user clicks on the run "fr_desktop"
    Then on the executions and errors page, in the cart "5", on the run "fr_api", the team "1" is visible
    And on the executions and errors page, in the cart "5", on the run "fr_api", the team "2" is visible
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "1" is visible
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "2" is visible
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "3" is visible
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "4" is visible
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", the team "-404" is visible
 
  # RUN2 - CONTROL DATAS
  # TEAM -404
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "sanity-check", for the team "-404", the progress bar is 100.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "*", for the team "-404", the progress bar is 100.0% of success and 0.0% of unhandled
  # TEAM 1
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "high", for the team "1", the progress bar is 100.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "medium", for the team "1", the progress bar is 100.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "*", for the team "1", the progress bar is 100.0% of success and 0.0% of unhandled
  # TEAM 2
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "sanity-check", for the team "2", the progress bar is 100.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "medium", for the team "2", the progress bar is 100.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "*", for the team "2", the progress bar is 100.0% of success and 0.0% of unhandled
  # TEAM 3
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "sanity-check", for the team "3", the progress bar is 100.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "high", for the team "3", the progress bar is 100.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "medium", for the team "3", the progress bar is 100.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "*", for the team "3", the progress bar is 100.0% of success and 0.0% of unhandled
  # TEAM 4
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "sanity-check", for the team "4", the progress bar is 100.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "5", on the run "fr_desktop", in the column "*", for the team "4", the progress bar is 100.0% of success and 0.0% of unhandled
  
@severity-sanity-check
  Scenario: Check Runs - everything is not Green
  # HEADERS QUALITY
    When on the executions and errors page, in the cart "5", the user clicks on the "Previous" execution button
    Then on the executions and errors page, in the cart "4", on the header, in the column "sanity-check", the quality is 100, the number of OK is 6, the number of KO is 5, the color is "green"
    And on the executions and errors page, in the cart "4", on the header, in the column "high", the quality is 100, the number of OK is 12, the number of KO is 3, the color is "green"
    And on the executions and errors page, in the cart "4", on the header, in the column "medium", the quality is 100, the number of OK is 2, the number of KO is 0, the color is "green"
    And on the executions and errors page, in the cart "4", on the header, in the column "*", the quality is 100, the number of OK is 20, the number of KO is 7, the color is "none"
 
  # HEADERS THRESHOLD
    And on the executions and errors page, in the cart "4", on the header, in the column "sanity-check", the threshold is 100, the color is "none"
    And on the executions and errors page, in the cart "4", on the header, in the column "high", the threshold is 95, the color is "none"
    And on the executions and errors page, in the cart "4", on the header, in the column "medium", the threshold is 90, the color is "none"

  # HEADERS CONTROL DATAS
    And on the executions and errors page, in the cart "4", on the run "fr_api", in the column "sanity-check", the number of ok is 2, the number of problem is 1, the number of ko is 1
    And on the executions and errors page, in the cart "4", on the run "fr_api", in the column "high", the number of ok is 7, the number of problem is 3, the number of ko is 0
    And on the executions and errors page, in the cart "4", on the run "fr_api", in the column "medium", the number of ok is 2, the number of problem is 0, the number of ko is 0
    And on the executions and errors page, in the cart "4", on the run "fr_api", in the column "*", the number of ok is 12, the number of problem is 3, the number of ko is 1
    
    And on the executions and errors page, in the cart "4", on the run "fr_desktop", in the column "sanity-check", the number of ok is 4, the number of problem is 0, the number of ko is 3
    And on the executions and errors page, in the cart "4", on the run "fr_desktop", in the column "high", the number of ok is 5, the number of problem is 0, the number of ko is 0
    And on the executions and errors page, in the cart "4", on the run "fr_desktop", in the column "medium", the number of ok is 0, the number of problem is 0, the number of ko is 0
    And on the executions and errors page, in the cart "4", on the run "fr_desktop", in the column "*", the number of ok is 9, the number of problem is 0, the number of ko is 3
 
  # RUNS HIDDEN  
    And on the executions and errors page, in the cart "4", on the run "fr_api", the team "1" is hidden
    And on the executions and errors page, in the cart "4", on the run "fr_api", the team "2" is hidden
    And on the executions and errors page, in the cart "4", on the run "fr_desktop", the team "1" is hidden
    And on the executions and errors page, in the cart "4", on the run "fr_desktop", the team "2" is hidden
    And on the executions and errors page, in the cart "4", on the run "fr_desktop", the team "-404" is hidden
  
  # RUN1 - CONTROL DATAS
    When on the executions and errors page, in the cart "4", the user clicks on the run "fr_api"
  # TEAM 1
    Then on the executions and errors page, in the cart "4", on the run "fr_api", in the column "sanity-check", for the team "1", the progress bar is 50.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "4", on the run "fr_api", in the column "high", for the team "1", the progress bar is 100.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "4", on the run "fr_api", in the column "medium", for the team "1", the progress bar is 100.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "4", on the run "fr_api", in the column "*", for the team "1", the progress bar is 87.5% of success and 0.0% of unhandled
  # TEAM 2
    And on the executions and errors page, in the cart "4", on the run "fr_api", in the column "sanity-check", for the team "2", the progress bar is 50.0% of success and 50.0% of unhandled
    And on the executions and errors page, in the cart "4", on the run "fr_api", in the column "high", for the team "2", the progress bar is 50.0% of success and 50.0% of unhandled
    And on the executions and errors page, in the cart "4", on the run "fr_api", in the column "*", for the team "2", the progress bar is 50.0% of success and 50.0% of unhandled
 
  # RUN1 - CONTROL DATAS
    When on the executions and errors page, in the cart "4", the user clicks on the run "fr_desktop"
  # TEAM -404
    Then on the executions and errors page, in the cart "4", on the run "fr_desktop", in the column "sanity-check", for the team "-404", the progress bar is 100.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "4", on the run "fr_desktop", in the column "high", for the team "-404", the progress bar is 100.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "4", on the run "fr_desktop", in the column "*", for the team "-404", the progress bar is 100.0% of success and 0.0% of unhandled
  # TEAM 1
    And on the executions and errors page, in the cart "4", on the run "fr_desktop", in the column "sanity-check", for the team "1", the progress bar is 75.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "4", on the run "fr_desktop", in the column "*", for the team "1", the progress bar is 75.0% of success and 0.0% of unhandled
  # TEAM 2
    And on the executions and errors page, in the cart "4", on the run "fr_desktop", in the column "sanity-check", for the team "2", the progress bar is 0.0% of success and 0.0% of unhandled
    And on the executions and errors page, in the cart "4", on the run "fr_desktop", in the column "*", for the team "2", the progress bar is 0.0% of success and 0.0% of unhandled
