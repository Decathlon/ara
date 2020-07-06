Cypress.Commands.add('GetByDataNrt', id => {
  return cy.get(`[data-nrt='${id}']`);
});

Cypress.Commands.add('WaitForElementByDataNrt', (id, to) => {
  return cy.get(`[data-nrt=${id}]`, {timeout:to})
});
