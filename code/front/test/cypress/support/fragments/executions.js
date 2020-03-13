export const url = '/projects/the-demo-project/executions';

  
export const getActionsAndJobReportsButton = (id) => {
    return cy.GetByDataNrt('ExecutionsAndErrorsActionsAndJobReportsButton_' + id);
}

export const getActionsButton = (id) => {
    return cy.GetByDataNrt('ExecutionsAndErrorsActionsAndJobReportsButton_Actions_' + id)
}