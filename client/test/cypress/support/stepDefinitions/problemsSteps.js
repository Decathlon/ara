import { Given, When, Then } from 'cypress-cucumber-preprocessor/steps';
import * as problems from '../fragments/problems';

Given('problems', () => {
    cy.server();
  
    cy.fixture('problems.json').as('problems');
    
    cy.route({
      method: "POST",
      url: "/api/projects//the-demo-project/problems/filter?page=0&size=10",
      body: {
        "status":null,
        "blamedTeamId":null,
        "name":null,
        "defectId":null,
        "defectExistence":null,
        "rootCauseId":null
      },
      response: "fixture:problems.json",
    })
    
    cy.visit(problems.url);

  });


  When('on the problems page, user clicks on problem {string} ', (problemId) => {
    //problems.get
  });


  /**************** Filters : Selects And inputs Search ************/

  When('on the problems page, user selects status {string}', (statusLabel) => {
    problems.getSelectBoxStatusProblem.select(statusLabel).click();

  });

  When('on the problems page, user selects team {string}', (teamLabel) => {
    problems.getSelectBoxRootCauseProblem.select(rootCauseLabel).click();
  });

  When('on the problems page, user fills problem name {string}', (problemName) => {
    problems.getProblemName.find('input').click().type(problemName);
  });

  When('on the problems page, user fills work item  {string}', (workItem) => {
    problems.getWorkItemProblem.type(workItem);
  });

  When('on the problems page, user clicks on checkbox non existent', () => {
    //problems.getNonExistentCheckBox.click();
  });

  When('on the problems page, user selects root cause {string}', (rootCauseLabel) => {
    problems.getSelectBoxRootCauseProblem(rootCauseLabel).click();
  });


/**************** Pagination elements ************/
