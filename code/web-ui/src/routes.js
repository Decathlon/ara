/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

import Login from './views/login.vue'

const routes = [
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
    path: '/projects/:projectCode/executions/framed/png',
    name: 'executions-framed-png',
    meta: {
      title: 'Executions',
      isFramed: true,
      isPng: true
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
    path: '/projects/:projectCode/active-admin',
    name: 'active-admin',
    meta: {
      title: 'Admin Management'
    },
    component: (resolve) => require(['./views/admin-management.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/members',
    name: 'members',
    meta: {
      title: 'Members'
    },
    component: (resolve) => require(['./views/admin-management-members.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/dashboard',
    name: 'dashboard',
    redirect: '/projects/:projectCode/executions'
  },
  {
    path: '/projects/:projectCode/admin-project-details',
    name: 'admin-project-details',
    meta: {
      title: 'Admin project details'
    },
    component: (resolve) => require(['./views/admin-project-details.vue'], resolve)
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
    path: '/projects/:projectCode/management/technologies',
    name: 'management-technologies',
    meta: {
      title: 'Technologies'
    },
    component: (resolve) => require(['./views/management-technologies.vue'], resolve)
  },
  {
    path: '/projects/:projectCode/management/settings',
    name: 'management-settings',
    meta: {
      title: 'Project Settings'
    },
    component: (resolve) => require(['./views/management-settings.vue'], resolve)
  },

  // Not found page
  {
    path: '/not-found',
    name: 'not-found',
    meta: {
      title: 'Page not found...'
    },
    component: (resolve) => require(['./views/not-found.vue'], resolve)
  },
  { path: '*', redirect: '/not-found' },

  // Login
  {
    path: '/login',
    name: 'login',
    meta: {
      title: 'ARA Login',
      public: true,
      onlyWhenLoggedOut: true
    },
    component: Login
  }
]

export default routes
