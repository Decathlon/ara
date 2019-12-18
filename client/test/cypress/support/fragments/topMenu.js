const getTopMenu = () => {
  return cy.GetByDataNrt('topMenu');
}

export const exists = () => {
  return getTopMenu().should('exist');
}
