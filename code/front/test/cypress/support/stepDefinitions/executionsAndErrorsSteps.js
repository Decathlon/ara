import { Given, When, Then } from 'cypress-cucumber-preprocessor/steps';
import * as executions from '../fragments/executions';
import * as commun from '../fragments/commun.js';

const normalizeText = (s) => s.replace(/\s/g, '');
  

Given('executions and errors', () => {
  cy.server();
  cy.fixture('executions_latest.json').as('executionsLatest');
  cy.route('GET', '/api/projects/the-demo-project/executions/latest', '@executionsLatest');
  cy.visit(executions.url);
});

When('on the executions and errors page, the user clicks on the actions and job reports button {string}', (executionId) => {
  executions.getActionsAndJobReportsButton(executionId).click();
});
When('on the executions and errors page, in the cart {string}, the user clicks on the run {string}', (executionId, runId) => {
  executions.getRun(runId, executionId).click();
});


Then('on the executions and errors page, in the actions and job reports list, the {string} button {string} is visible', (label, executionId) => {
  executions.getButton(label, executionId).should('be.visible');
});

Then('on the executions and errors page, in the actions and job reports list, the {string} button {string} is disabled', (label, executionId) => {
  executions.getButton(label, executionId).should('have.class', 'ivu-dropdown-item-disabled');
});

Then('on the executions and errors page, in the actions and job reports list, the {string} button {string} is enabled', (label, executionId) => {
  executions.getButton(label, executionId).should('have.class', 'ivu-dropdown-item');
});

Then('on the executions and errors page, in the cart {string}, on the run {string}, the team {string} is visible', (executionId, runId, teamId) => {
  executions.getCartRowSubTitle(runId, teamId, executionId).should('be.visible');
});

Then('on the executions and errors page, in the cart {string}, on the run {string}, the team {string} is hidden', (executionId, runId, teamId) => {
  executions.getCartRowSubTitle(runId, teamId, executionId).should('be.not.visible');
});

Then('on the executions and errors page, in the cart {string}, on the header, in the column {string}, the quality is {string}, the number of OK is {string}, the number of KO is {string}, the color is {string}', (executionId, qualityId, qualityValue, okValue, koValue, color) => {
  executions.getCartHeader(qualityId, executionId).find('.percentStyle').then(($percent) => {
    expect(normalizeText($percent.text())).to.equal(normalizeText(qualityValue + ' %'))
  });
  executions.getCartHeader(qualityId, executionId).find('.okStyle').then(($ok) => {
    expect(normalizeText($ok.text())).to.equal(normalizeText(okValue + " OK"))
  });
  executions.getCartHeader(qualityId, executionId).find('.koStyle').then(($ko) => {
    expect(normalizeText($ko.text())).to.equal(normalizeText(koValue + " KO"))
  });
  executions.getCartHeader(qualityId, executionId).then(($background) => {
    expect($background).to.have.css('background-color', commun.getRGB(color));
  });
}); 

Then('on the executions and errors page, in the cart {string}, on the header, in the column {string}, the threshold is {string}, the color is {string}', (executionId, qualityId, thresholdValue, color) => {
  executions.getCartHeader(qualityId, executionId).find('.thresholdStyle').then(($threshold) => {
    expect(normalizeText($threshold.text())).to.equal(normalizeText(thresholdValue + " %"))
  });
  executions.getCartHeader(qualityId, executionId).find('.thresholdStyle').then(($background) => {
    expect($background).to.have.css('background-color', commun.getRGB(color));
  });
});

Then('on the executions and errors page, in the cart {string}, on the run {string} and the team {string}, in the column {string}, the number of ok is {string}, the number of problem is {string}, the number of ko is {string}, the progress bar is {int}% of success, {int}% of unhandled and {int}% of failed', (executionId, runId, teamId, qualityId, okValue, pbValue, koValue, greenValue, orangeValue, redValue) => {
  executions.getCartRowSubTitle(runId, teamId, executionId).find('.textPassed').then(($textPassed) => {
    if (okValue != 0){
      expect($textPassed.text()).to.equal(okValue);
    } else {
      expect($textPassed).to.be.not.visible;
    }
  });
  executions.getCartRowSubTitle(runId, teamId, executionId).find('.textFailed').then(($textFailed) => {
    if (koValue != 0){
      expect($textFailed.text()).to.equal(koValue);
    } else {
      expect($textFailed).to.be.not.visible;
    }
  });
  executions.getCartRowSubTitle(runId, teamId, executionId).find('.textProblem').then(($textProblem) => {
    if (pbValue != 0){
      expect($textProblem.text()).to.equal(pbValue);
    } else {
      expect($textProblem).to.be.not.visible;
    }
  });
});

Then('on the executions and errors page, in the cart {string}, on the run {string}, in the column {string}, the number of ok is {string}, the number of problem is {string}, the number of ko is {string}, the progress bar is {int}% of success, {int}% of unhandled and {int}% of failed', (executionId, runId, qualityId, okValue, pbValue, koValue, greenValue, orangeValue, redValue) => {
  executions.getCartRowHeader(qualityId, runId, executionId).find('.textPassed').then(($textPassed) => {
    if (okValue != 0){
      expect(normalizeText($textPassed.text())).to.equal(normalizeText(okValue));
    } else {
      expect(normalizeText($textPassed.text())).to.equal('');
    }
  });
  const cartRowHeaderFailed = executions.getCartRowHeader(qualityId, runId, executionId).find('.textFailed');
  cartRowHeaderFailed.then(($textFailed) => {
    if (koValue != 0){
      expect(normalizeText($textFailed.text())).to.equal(normalizeText(koValue));
    } else {
      expect(normalizeText($textFailed.text())).to.equal('');
    }
  });
  if (pbValue != 0){
    cartRowHeaderFailed.find('.textProblem').then(($textProblem) => {
      expect(normalizeText($textProblem.text())).to.equal(normalizeText(pbValue));
    });
  } else {
    cartRowHeaderFailed.find('.textProblem').should('have.length', 0)
  } 

/*
  executions.getCartRowHeader(qualityId, runId, executionId).find('.progressBar').then(($progressBar) => {
    cy.log($progressBar.find('>div'));
    const size = $progressBar.find('>div').eq(0);
    expect($progressBar.find('>div').eq(0).length()).to.have.css('width', size);
  });*/
 // executions.getCartRowHeader(qualityId, runId, executionId).find('.progressBar').children().to.have.css('background-color', 100);
  
});