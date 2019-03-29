import moment from 'moment'

import Vue from 'vue'
import api from './api'
import store from '../store'

let util = {}

const featureCoverageColors = [
  'black', // 0 = All

  '#19BE6B', // 1 = Covered (no ignored) [green]
  '#FFCC30', // 2 = Partially covered (few ignored) [orange]

  '#ED3F14', // 3 = Ignored coverage (all ignored) [red]
  '#2D8CF0', // 4 = Started [blue]
  '#B0B0B0', // 5 = Not automatable [gray]
  '#AF2100' // 6 = Not covered [dark red]
]

util.getFeatureCoverageColor = function (i) {
  return featureCoverageColors[i]
}

util.title = function (title) {
  title = (title ? title + ' - ' : '') + 'Agile Regression Analyzer'
  window.document.title = title
}

// List of HTML entities for escaping
const HTML_ESCAPES = {
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;',
  "'": '&#x27;',
  '/': '&#x2F;'
}
// Regex containing the keys listed immediately above
const HTML_ESCAPER = /[&<>"'/]/g
// Escape a string for HTML interpolation
util.escapeHtml = function (string) {
  return ('' + string).replace(HTML_ESCAPER, (match) => {
    return HTML_ESCAPES[match]
  })
}

util.formatDate = function (date, withSeconds) {
  return date ? moment(date).format('MMM D, YYYY - HH:mm' + (withSeconds ? ':ss' : '')) : ''
}

util.toSplitted = function (string) {
  return (string ? string.split(',') : [])
}

util.fromSplitted = function (array) {
  return (array && array.length > 0 ? array.join(',') : '')
}

util.toggle = function (selectedOptions, allOptions, optionToToggle) {
  let newSelectedOptions = []
  for (let i in allOptions) {
    let option = allOptions[i]
    let isOptionToToggle = (option === optionToToggle)
    let wasContaining = selectedOptions.indexOf(option) !== -1
    if ((isOptionToToggle && !wasContaining) || (!isOptionToToggle && wasContaining)) {
      newSelectedOptions.push(option)
    }
  }
  return newSelectedOptions
}

util.prettySeverity = function (severityCode, view) {
  for (let severity of store.getters['severities/severities'](view)) {
    if (severityCode === severity.code) {
      return severity.name
    }
  }
  if (!severityCode || severityCode === '_') {
    return 'No Severity!'
  } else {
    return severityCode
  }
}

util.prettyHoursMinutesFromMillisecondsDuration = function (milliseconds) {
  if (milliseconds === null) {
    return ''
  }

  let seconds = Math.floor(milliseconds / 1000)
  milliseconds -= seconds * 1000

  let minutes = Math.floor(seconds / 60)
  seconds -= minutes * 60

  let hours = Math.floor(minutes / 60)
  minutes -= hours * 60

  return (hours ? hours + (hours === 1 ? ' hour' : ' hours') + (minutes ? ' ' : '') : '') +
         (minutes || !hours ? minutes + (minutes === 1 ? ' minute' : ' minutes') : '')
}

util.ifFeatureEnabled = function (featureCode, callbackIfEnabled, callbackIfDisabled) {
  Vue.http
  .get(api.paths.features() + '/' + featureCode, api.REQUEST_OPTIONS)
  .then((response) => {
    if (response.body.enabled) {
      callbackIfEnabled()
    } else {
      callbackIfDisabled()
    }
  }, (error) => {
    api.handleError(error, callbackIfDisabled)
  })
}

export default util
