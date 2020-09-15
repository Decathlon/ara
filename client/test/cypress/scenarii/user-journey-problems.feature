Feature: Journey Problems


Background:
  Given problems

@severity-sanity-check
  Scenario: Display problem

  When on the problems page, user clicks on checkbox non existent
  And on the problems page, user fills problem name 'test'
  
