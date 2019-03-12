import util from './util'

const getBackgroundColor = function (status) {
  switch (status) {
    case 'passed':
      return '#92DD96'
    case 'failed':
      return '#F2928C'
    case 'skipped':
      return '#88AAFF'
    case 'pending':
      return '#F5F28F'
    case 'undefined':
    case 'missing':
      return '#F5B975'
    case 'element': // Special "status" for "Background:" and "Scenario:" pseudo-titles
      return 'white'
    default:
      return '#D3D3D3'
  }
}

const prettyDuration = function (nanoSeconds) {
  let invisibleSpace = '<span style="display: inline-block; width: 0;"> </span>'
  let thirdSpace = '<span style="display: inline-block; width: 0.333ch;"> </span>'

  if (nanoSeconds === null) {
    return ''
  }
  let milliseconds = Math.floor(nanoSeconds / 1000000)

  let seconds = Math.floor(milliseconds / 1000)
  milliseconds -= seconds * 1000

  let minutes = Math.floor(seconds / 60)
  seconds -= minutes * 60

  return invisibleSpace + // When copy/pasting the scenario, have a space separating the timing, but keep it invisible on screen
         (minutes ? minutes + thirdSpace + 'min ' : '') +
         (seconds ? (minutes && seconds < 10 ? '0' : '') + seconds + thirdSpace + 's ' : '') +
         (minutes || seconds ? (milliseconds < 10 ? '00' : milliseconds < 100 ? '0' : '') : '') + milliseconds + thirdSpace + 'ms'
}

export default {

  formattedScenario (executedScenario, error, monochrome) {
    if (!executedScenario) {
      return ''
    }

    let isCucumber = executedScenario.featureFile.endsWith('.feature')

    // Max width: '99 min 99 s 999 ms'
    let timeWidth = '18ch'

    let startDateTime = (executedScenario.startDateTime ? new Date(executedScenario.startDateTime) : undefined)

    let lines = executedScenario.content.split(/\r?\n/)
    let formatted = ''
    let lastLineWasCurrent = false
    let lastLineNumber = null
    let inDocString = false
    for (let i in lines) {
      let line = lines[i]
      if (line !== '') {
        // Extract line Number
        let index = line.indexOf(':')
        let lineNumber = parseInt(line.substring(0, index), 10)
        let lineText = line.substring(index + 1)

        // Extract step status & step keywork+sentence
        index = lineText.indexOf(':')
        let stepStatus = lineText.substring(0, index)
        lineText = lineText.substring(index + 1)

        // Extract the duration, if present
        // (present if the scenario describes an execution result and the current line has been executed,
        // absent if the scenario  describes what is stored in VCS;
        // also absent for Postman assertions: it has no duration concept for assertions)
        index = lineText.indexOf(':')
        let duration = null
        if (index !== -1) {
          let maybeDuration = lineText.substring(0, index)
          if (maybeDuration.match(/^[0-9]+$/)) {
            duration = parseInt(maybeDuration)
            lineText = lineText.substring(index + 1)
          }
        }

        let isHook = lineText.startsWith('@') || // '@Before'/'@After' Cucumber hooks
          lineText === '<Pre-Request Script>' || // Postman scripts
          lineText === '<Test Script>'

        // Correctly indent
        let isTableLine = false
        if (isCucumber) {
          if (lastLineNumber === lineNumber && (inDocString || lineText.startsWith('"""'))) {
            inDocString = true
            lineText = '        ' + lineText
          } else if (lineText.startsWith('Given')) {
            lineText = '  ' + lineText
          } else if (lineText.startsWith('When') || lineText.startsWith('Then')) {
            lineText = '   ' + lineText
          } else if (lineText.startsWith('|')) {
            lineText = '        ' + lineText
            isTableLine = true
          } else if (!isHook && stepStatus !== 'element') { // 'And'/'But' && 'Background:'/'Scenario:'
            lineText = '    ' + lineText
          }

          if (lastLineNumber !== lineNumber) {
            lastLineNumber = lineNumber
            inDocString = false
          }
        }

        let isCurrent = (error && lineNumber === error.stepLine) || (isTableLine && lastLineWasCurrent)

        lineText = util.escapeHtml(lineText)

        // Step duration
        if (stepStatus === 'passed' || stepStatus === 'failed') {
          let startedAttributes = (startDateTime ? ' title="Step started at ' + util.formatDate(startDateTime, true) + '" class="dataInTooltip"' : '')
          lineText += '<span' + startedAttributes + ' style="user-select: none; display: block; float: right; color: ' + (isCurrent ? 'lightgray' : 'gray') + '; margin-right: calc(-4px - ' + timeWidth + ');">' + prettyDuration(duration) + '</span>'
        }

        if (isCurrent) {
          formatted += '<b style="display: block; padding: 0 calc(2 * 4px + ' + timeWidth + ') 0 4px; background-color: #ED3F14; color: white;">' + lineText + '</b>'
          lastLineWasCurrent = true
        } else {
          formatted += '<span style="display: block; padding: 0 calc(2 * 4px + ' + timeWidth + ') 0 4px;' + (monochrome ? '' : ' background-color: ' + getBackgroundColor(stepStatus) + '; color: ' + (isHook ? 'gray' : 'black') + ';') + '">' + lineText + '</span>'
          lastLineWasCurrent = false
        }

        if (startDateTime && duration) {
          startDateTime.setMilliseconds(startDateTime.getMilliseconds() + duration / 1000000)
        }
      }
    }
    return formatted
  }

}
