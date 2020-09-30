export const url = '/projects/the-demo-project/executions';

  
export const getActionsAndJobReportsButton = (executionId) => {
    return cy.GetByDataNrt('executions_ActionsAndJobReportsButton_' + executionId);
}

export const getButton = (label, executionId) => {
    return cy.GetByDataNrt('executions_ActionsAndJobReportsButton_' + label + '_' + executionId)
}

export const getRun = (runId, executionId) => {
    return cy.GetByDataNrt('executions_CartRowTitle_' + runId + '_' + executionId);
}

export const getCartRowSubTitle = (runId, teamId, executionId) => {
    return cy.GetByDataNrt('executions_CartRowSubTitle_' + runId + '_' + teamId + '_' + executionId);
}
export const getCartHeader = (qualityId, executionId) => {
    return cy.GetByDataNrt('executions_CartHeader_' + qualityId + '_' + executionId);
}

export const getCartRowHeader = (qualityId, runId, executionId) => {
    return cy.GetByDataNrt('executions_CartRowHeader_' + qualityId + '_' + runId + '_' + executionId);
}

export const getCartRowTeam = (qualityId, runId, teamId, executionId) => {
    return cy.GetByDataNrt('executions_CartRowTeam_' + qualityId + '_' + runId + '_' + teamId + '_' + executionId);
}

export const getVersion = (executionId) => {
    return cy.GetByDataNrt('executions_Version_' + executionId);
}
export const getTestDate = (executionId) => {
    return cy.GetByDataNrt('executions_TestDate_' + executionId);
}
export const getBuildDateAgo = (executionId) => {
    return cy.GetByDataNrt('executions_BuildDateAgo_' + executionId);
}

export const getNavigationButton = (executionId, navigation) => {
    return cy.GetByDataNrt('executions_Navigation' + navigation + 'Button_' + executionId);
}
