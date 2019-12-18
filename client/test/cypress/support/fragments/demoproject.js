export const reset = () => {
  cy.get('body').then(body => {
    if (body.find('[data-nrt="deleteDemo"]').length >  0) {
      cy.GetByDataNrt('deleteDemo').click()
      // TODO(Issue224) : Replace the basic Modal component of iviewui by a custom one
      // to have the posibitlity to add custom attributes (like a data-nrt).
      cy.get('.ivu-modal-confirm-footer').children().next().click()
    }
  });
  cy.GetByDataNrt('createDemo').click();
  cy.WaitForElementByDataNrt('deleteDemo', 5000).should('exist');
}
