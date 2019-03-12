const ERROR_MAX_LENGTH = 250

const STARTS_TO_REMOVE = [
  '\tat ', // Java stack-trace
  '   at ', // Newman stack-trace
  'For documentation on this error, please visit: ', // Doc links for NotFoundException
  'Build info: version: ', // Web-driver info on WebDriverException
  '  (Session info:', // Web-driver info on WebDriverException
  '  (Driver info:', // Web-driver info on WebDriverException
  'But found:', // java.lang.AssertionError: Expected event: ... But found: ...
  'TIP: You can use the sentence ', // Unexpected error during scenario (see video): ... TIP: ...
  'Available displayed texts are: ' // Cannot select item ... in Project select.\nAvailable ...
]

function reduceException (string, replacement) {
  var regex = /(.+\.)+.*Exception:/
  if (string && regex.test(string)) {
    return string.replace(regex, replacement)
  }
  return string
}

function replaceStart (string, search, replacement) {
  if (string && string.startsWith(search)) {
    if (string === search) {
      return replacement
    } else {
      return replacement + string.substring(search.length)
    }
  }
  return string
}

function startsWithAny (text, starts) {
  for (let i = 0; i < starts.length; i++) {
    if (text.startsWith(starts[i])) {
      return true
    }
  }
  return false
}

function minIndexOfAllStarts (text, starts) {
  let index = -1
  for (let i = 0; i < starts.length; i++) {
    let indexI = text.indexOf('\n' + starts[i])
    if (indexI !== -1 && (index === -1 || index > indexI)) {
      index = indexI
    }
  }
  return index
}

function getFirstLines (text, n) {
  let lines = text.replace('\r\n', '\n').split(/[\r\n]/)
  let lines2 = []
  for (let i = 0; i < n; i++) {
    if (i < lines.length) {
      lines2.push(lines[i])
    }
  }
  lines = lines2

  // Remove extra lines (only after the first one) if they are irrelevant
  if (lines.length > 1) {
    let maxIndex = 1
    for (; maxIndex < lines.length; maxIndex++) {
      if (startsWithAny(lines[maxIndex], STARTS_TO_REMOVE)) {
        break
      }
    }
    if (maxIndex < lines.length) {
      let newLines = []
      for (let i = 0; i < maxIndex; i++) {
        newLines[i] = lines[i]
      }
      lines = newLines
    }
  }

  return lines.join('\n')
}

export default {

  getErrorSummary (error) {
    let summary = getFirstLines(error, 10)

    // We will retain the first 160 characters
    // This is short, so compactify exception classes
    summary = replaceStart(summary, 'org.openqa.selenium.TimeoutException:', 'Timeout:') // Keep it short, because message can be long, with important information
    summary = replaceStart(summary, 'org.openqa.selenium.NoSuchElementException:', 'NotFound:') // Same reason
    summary = replaceStart(summary, 'org.openqa.selenium.ElementNotVisibleException:', 'NotVisible:') // Same reason
    summary = replaceStart(summary, 'java.lang.AssertionError:', 'Assertion:') // Message contains expected vs actual value
    summary = replaceStart(summary, 'org.junit.ComparisonFailure:', 'Assertion:') // It's an assertion (message contains expected vs actual value)
    summary = replaceStart(summary, 'java.lang.NullPointerException', 'NullPointerException') // No big reason, but be consistent
    summary = replaceStart(summary, 'org.openqa.selenium.WebDriverException:', 'Selenium:') // Serious technical error
    summary = replaceStart(summary, 'org.openqa.selenium.NoSuchWindowException:', 'Selenium:') // Serious technical error
    summary = reduceException(summary, 'Exception:') // Reduce all other exceptions stacktraces

    if (summary.length > ERROR_MAX_LENGTH) {
      summary = summary.substring(0, ERROR_MAX_LENGTH) + '...'
    }

    return summary
  },

  simplifyException (exception) {
    if (!exception) {
      return ''
    }
    let index = minIndexOfAllStarts(exception, STARTS_TO_REMOVE)
    if (index !== -1) {
      return exception.substring(0, index).trim()
    }
    return exception.trim()
  }

}
