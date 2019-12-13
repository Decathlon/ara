import { Given, When, Then } from 'cypress-cucumber-preprocessor/steps';
import * as demo from '../fragments/demoproject';
import * as executions from '../fragments/executions';
import * as managementProjects from '../fragments/management-projects';
import * as topMenu from '../fragments/topMenu';

Given('an user with a demo project', () => {
  cy.visit(managementProjects.url);
  demo.reset();
});

When('the user goes to the home page', () => {
  cy.visit(executions.url);
});

Then('the top menu is present', () => {
  topMenu.exists();
});
