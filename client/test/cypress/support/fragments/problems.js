export const url = '/projects/the-demo-project/problems';

// body for filter problems 
export const bodyProblemEmpty = '{"status":null,"blamedTeamId":null,"name":null,"defectId":null,"defectExistence":null,"rootCauseId":null}';
export const bodyProblemStatus = '{"status":null,"blamedTeamId":null,"name":null,"defectId":null,"defectExistence":null,"rootCauseId":null}';

// Header tab problem
export const getTabHeaderProblem = () => {
    return cy.GetByDataNrt('PROBLEMS_Menu');
}

// Checkbox Non Existent
export const getNonExistentCheckBox = () => {
    return cy.GetByDataNrt('Non_Existent_Checkbox').click();
}

// Select box status
export const getSelectBoxStatusProblem = () => {
    return cy.GetByDataNrt('');
}

// Select box team
export const getSelectBoxTeamProblem = () => {
    return cy.GetByDataNrt('');
}

// Input ProblemName
export const getProblemName = () => {
    return cy.GetByDataNrt('problem_name').find('input').first();
}

// Input WorkItem
export const getWorkItemProblem = () => {
    return cy.GetByDataNrt('');
}

// Select box root cause
export const getSelectBoxRootCauseProblem = (rootCauseLabel) => {
    return cy.GetByDataNrt('');
}

//Stabilities progressBar
export const getProgressBarStabilityByProblem = () => {
    return cy.GetByDataNrt('');
}

//Problem link by id
export const getProblemLinkById = () => {
    return cy.GetByDataNrt('');
}

//Result by page select box
export const getSelectResultByPage = () => {
    return cy.GetByDataNrt('');
}

//Page page
export const getPaginationPage = () => {
    return cy.GetByDataNrt('');
}

//Previous Page
export const getPreviousPage = () => {
    return cy.GetByDataNrt('');
}

//Next Page
export const getNextPage = () => {
    return cy.GetByDataNrt('');
}

//Previous 5 Pages
export const getPreviousPages = () => {
    return cy.GetByDataNrt('');
}

//Next 5 Pages
export const getNextPages = () => {
    return cy.GetByDataNrt('');
}