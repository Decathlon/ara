export default [
  // Home page and other URLs without any project code
  {
    path: '/', // Home page
    alias: [ // Legacy URLs when there was no project concept
      '/executions',
      '/executions/framed',
      '/executions/raw',
      '/executions/:id(\\d+)',
      '/executions/errors/:id(\\d+)',
      '/functionalities/cartography',
      '/functionalities/coverage',
      '/functionalities/assignations',
      '/management/communications',
      '/management/countries',
      '/management/cycle-definitions',
      '/management/root-causes',
      '/management/severities',
      '/management/sources',
      '/management/teams',
      '/management/types',
      '/problems',
      '/problems/:id(\\d+)',
      '/problems/:problemId(\\d+)/patterns/:patternId(\\d+)',
      '/scenario-writing-helps'
    ],
    name: 'redirecter',
    component: (resolve) => require(['./views/redirecter.vue'], resolve)
  },

  // Executions & Errors
  {
    path: '/projects/:projectCode/executions',
    name: 'executions',
    meta: {
      title: 'Executions'
    },
    component: (resolve) => require(['./views/executions.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/executions/framed',
    name: 'executions-framed',
    meta: {
      title: 'Executions',
      isFramed: true
    },
    component: (resolve) => require(['./views/executions.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/executions/raw',
    name: 'raw-executions',
    meta: {
      title: 'Raw executions'
    },
    component: (resolve) => require(['./views/raw-executions.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/executions/:id(\\d+)',
    name: 'execution',
    meta: {
      title: 'Execution'
    },
    component: (resolve) => require(['./views/execution.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/executions/errors/:id(\\d+)',
    name: 'error',
    meta: {
      title: 'Error'
    },
    component: (resolve) => require(['./views/error.vue'], resolve)
  },

  // Known Problems
  {
    path: '/projects/:projectCode/problems',
    name: 'problems',
    meta: {
      title: 'Known Problems'
    },
    component: (resolve) => require(['./views/problems.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/problems/:id(\\d+)',
    name: 'problem',
    meta: {
      title: 'Known Problem'
    },
    component: (resolve) => require(['./views/problem.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/problems/:problemId(\\d+)/patterns/:patternId(\\d+)',
    name: 'problem-pattern',
    meta: {
      title: 'Edit Problem Pattern'
    },
    component: (resolve) => require(['./views/problem-pattern.vue'], resolve)
  },

  // Functionalities
  {
    path: '/projects/:projectCode/functionalities',
    name: 'functionalities',
    redirect: '/projects/:projectCode/functionalities/coverage' // First tab
  },
  {
    path: '/projects/:projectCode/functionalities/cartography',
    name: 'functionality-cartography',
    meta: {
      title: 'Functionality Cartography'
    },
    component: (resolve) => require(['./views/functionality-cartography.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/functionalities/coverage',
    name: 'functionality-coverage',
    meta: {
      title: 'Functionality Coverage'
    },
    component: (resolve) => require(['./views/functionality-coverage.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/functionalities/assignations',
    name: 'functionality-assignations',
    meta: {
      title: 'Functionality Assignations'
    },
    component: (resolve) => require(['./views/functionality-assignations.vue'], resolve)
  },

  // Scenario-Writing Helps
  {
    path: '/projects/:projectCode/scenario-writing-helps',
    name: 'scenario-writing-helps',
    meta: {
      title: 'Scenario-Writing Helps'
    },
    component: (resolve) => require(['./views/scenario-writing-helps.vue'], resolve)
  },

  // Management
  {
    path: '/projects/:projectCode/management',
    name: 'management',
    redirect: '/projects/:projectCode/management/communications' // First tab
  },
  {
    path: '/projects/:projectCode/management/communications',
    name: 'management-communications',
    meta: {
      title: 'Communications'
    },
    component: (resolve) => require(['./views/management-communications.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/management/teams',
    name: 'management-teams',
    meta: {
      title: 'Teams'
    },
    component: (resolve) => require(['./views/management-teams.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/management/root-causes',
    name: 'management-root-causes',
    meta: {
      title: 'Root Causes'
    },
    component: (resolve) => require(['./views/management-root-causes.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/management/countries',
    name: 'management-countries',
    meta: {
      title: 'Countries'
    },
    component: (resolve) => require(['./views/management-countries.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/management/cycle-definitions',
    name: 'management-cycle-definitions',
    meta: {
      title: 'Cycles'
    },
    component: (resolve) => require(['./views/management-cycle-definitions.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/management/severities',
    name: 'management-severities',
    meta: {
      title: 'Severities'
    },
    component: (resolve) => require(['./views/management-severities.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/management/sources',
    name: 'management-sources',
    meta: {
      title: 'Sources'
    },
    component: (resolve) => require(['./views/management-sources.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/management/types',
    name: 'management-types',
    meta: {
      title: 'Types'
    },
    component: (resolve) => require(['./views/management-types.vue'], resolve)
  },
  {
    path: '/management/projects',
    name: 'management-projects',
    meta: {
      title: 'Projects'
    },
    component: (resolve) => require(['./views/management-projects.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/management/settings',
    name: 'management-settings',
    meta: {
      title: 'Project Settings'
    },
    component: (resolve) => require(['./views/management-settings.vue'], resolve)
  }
]
