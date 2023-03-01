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
import iView from 'iview'

import util from './util'

let api = {}

const IGNORED_ERROR_STATUS_CODE = [401, 403]

const API_PATH = '/api'
const PROJECT_API_PATH = `${API_PATH}/projects`
const MEMBER_API_PATH = `${API_PATH}/member`
const MEMBER_USER_API_PATH = `${MEMBER_API_PATH}/user`

const USER_ACCOUNTS_API_PATH = `${MEMBER_USER_API_PATH}/accounts`
const SCOPES_USER_ACCOUNTS_API_PATH = `${USER_ACCOUNTS_API_PATH}/scoped`
const CURRENT_USER_ACCOUNTS_API_PATH = `${USER_ACCOUNTS_API_PATH}/current`
const CURRENT_USER_ACCOUNTS_DEFAULT_PROJECT_API_PATH = `${CURRENT_USER_ACCOUNTS_API_PATH}/default-project`

const USER_GROUP_API_PATH = `${MEMBER_USER_API_PATH}/groups`

const AUTH = '/oauth'

const projectPath = function (viewOrProjectCode) {
  let projectCode = (typeof viewOrProjectCode === 'string' ? viewOrProjectCode : viewOrProjectCode.$route.params.projectCode)
  return PROJECT_API_PATH + '/' + projectCode
}

api.REQUEST_OPTIONS = {
  timeout: 60 * 1000
}

api.paths = {
  authenticationConfiguration: `${AUTH}/configuration`,
  loggedStatus: `${AUTH}/status`,

  currentUser: CURRENT_USER_ACCOUNTS_API_PATH,
  currentUserDefaultProjectClear: CURRENT_USER_ACCOUNTS_DEFAULT_PROJECT_API_PATH,
  currentUserDefaultProjectUpdate: (projectCode) => `${CURRENT_USER_ACCOUNTS_DEFAULT_PROJECT_API_PATH}/${projectCode}`,
  allUsers: `${USER_ACCOUNTS_API_PATH}/all`,
  scopedUsers: SCOPES_USER_ACCOUNTS_API_PATH,
  scopedUsersByProject: (projectCode) => `${SCOPES_USER_ACCOUNTS_API_PATH}/project/${projectCode}`,
  userProjectScopeManagement: (userLogin, projectCode) => `${USER_ACCOUNTS_API_PATH}/login/${userLogin}/scopes/project/${projectCode}`,
  userProfileUpdate: (userLogin) => `${USER_ACCOUNTS_API_PATH}/login/${userLogin}/profile`,

  groupBasePath: USER_GROUP_API_PATH,
  groupById: (groupId) => `${USER_GROUP_API_PATH}/${groupId}`,
  allGroups: `${USER_GROUP_API_PATH}/all`,
  groupsContainingUser: (userLogin) => `${USER_GROUP_API_PATH}/containing/account/login/${userLogin}`,
  groupsContainingCurrentUser: `${USER_GROUP_API_PATH}/containing/account/current`,
  groupsMembersManagement: (userLogin, groupId) => `${USER_GROUP_API_PATH}/containing/account/login/${userLogin}/groups/${groupId}`,
  groupsManagedByUser: (userLogin) => `${USER_GROUP_API_PATH}/managed/account/login/${userLogin}`,
  groupsManagedByCurrentUser: `${USER_GROUP_API_PATH}/managed/account/current`,
  groupsManagersManagement: (userLogin, groupId) => `${USER_GROUP_API_PATH}/managed/account/login/${userLogin}/groups/${groupId}`,
  groupScopeManagement: (groupId, projectCode) => `${USER_GROUP_API_PATH}/${groupId}/scopes/project/${projectCode}`,

  communications: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/communications',
  countries: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/countries',
  cycleDefinitions: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/cycle-definitions',
  demo: `${API_PATH}/demo`,
  executions: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/executions',
  errors: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/errors',
  executedScenarios: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/executed-scenarios',
  functionalities: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/functionalities',
  info: '/actuator/info',
  problemPatterns: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/problem-patterns',
  problems: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/problems',
  projects: PROJECT_API_PATH,
  projectByCode: (projectCode) => `${PROJECT_API_PATH}/${projectCode}`,
  rootCauses: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/root-causes',
  scenarios: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/scenarios',
  settings: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/settings',
  severities: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/severities',
  sources: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/sources',
  teams: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/teams',
  types: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/types',

  features: `${API_PATH}/features`
}

api.handleError = function (response, callBack) {
  // Priorities of errors to display:
  // 1. Error managed by the application (header x-ara-...)
  // 2. Exception unmanaged by the application, but exposed by Spring as a JSON
  // 3. Fallback to 404 and other generic HTTP statues

  let title

  // TODO 404 is sent back by Spring as:
  // {"timestamp":"2017-12-08T13:21:53.465Z","status":404,"error":"Not Found","message":"No message available","path":"/api/errors/distinct-properties"}

  const accessDenied = IGNORED_ERROR_STATUS_CODE.includes(response?.status)
  if (accessDenied) {
    return
  }

  let message = response.headers.map['x-ara-message']
  if (message) {
    title = 'Error'
    let messages = message[0].split('{{NEW_LINE}}')
    if (messages.length === 1) {
      message = util.escapeHtml(messages)
    } else {
      message = '<ul>'
      for (let line of messages) {
        message += '<li>' + util.escapeHtml(line) + '</li>'
      }
      message += '</ul>'
    }
  } else {
    title = 'Error while Communicating with Server'
    let status
    if (response.body && ((response.body.message && response.body.message !== 'No message available') || response.body.exception)) {
      message = response.body.message
      status = response.body.exception
    } else {
      message = response.statusText
      status = response.status

      if (status === 0) {
        message = 'Either your device has no internet access, or the server is currently being updated to a new version. Please check your connexion and/or retry in a few minutes.'
        status = 'Unknown'
      }
    }
    message = util.escapeHtml(message) + '<br>' +
      '<br>' +
      '<span style="color: gray;">Error: ' +
      util.escapeHtml(status) +
      '</span>'
  }

  iView.Modal.error({
    title: title,
    content: message,
    okText: 'OK',
    onOk: () => {
      if (callBack) {
        callBack()
      }
    }
  })
}

api.pageUrl = function (url, paging) {
  return url + '?page=' + paging.page + '&size=' + paging.size
}

export default api
