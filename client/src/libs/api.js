import iView from 'iview'

import util from './util'

let api = {}

const API_PATH = '/api'
const PROJECT_API_PATH = API_PATH + '/projects'

const projectPath = function (viewOrProjectCode) {
  let projectCode = (typeof viewOrProjectCode === 'string' ? viewOrProjectCode : viewOrProjectCode.$route.params.projectCode)
  return PROJECT_API_PATH + '/' + projectCode
}

api.REQUEST_OPTIONS = {
  timeout: 60 * 1000
}

api.paths = {
  communications: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/communications',
  countries: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/countries',
  cycleDefinitions: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/cycle-definitions',
  demo: () => API_PATH + '/demo',
  executions: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/executions',
  errors: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/errors',
  executedScenarios: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/executed-scenarios',
  functionalities: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/functionalities',
  info: () => '/actuator/info',
  problemPatterns: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/problem-patterns',
  problems: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/problems',
  projects: () => PROJECT_API_PATH,
  rootCauses: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/root-causes',
  scenarios: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/scenarios',
  settings: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/settings',
  severities: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/severities',
  sources: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/sources',
  teams: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/teams',
  types: (viewOrProjectCode) => projectPath(viewOrProjectCode) + '/types',
  features: () => API_PATH + '/features'
}

api.handleError = function (response, callBack) {
  // Priorities of errors to display:
  // 1. Error managed by the application (header x-ara-...)
  // 2. Exception unmanaged by the application, but exposed by Spring as a JSON
  // 3. Fallback to 404 and other generic HTTP statues

  let title

  // TODO 404 is sent back by Spring as:
  // {"timestamp":"2017-12-08T13:21:53.465Z","status":404,"error":"Not Found","message":"No message available","path":"/api/errors/distinct-properties"}

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
