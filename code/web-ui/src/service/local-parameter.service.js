const LOCAL_PARAMETER_MEDIA_DISPLAY = 'local_parameter_media_display'
const DEFAULT_MEDIA_DISPLAY_IN_SAME_PAGE_VALUE = true

const LocalParameterService = {

  isMediaDisplayedOnSamePage () {
    const valueAsString = localStorage.getItem(LOCAL_PARAMETER_MEDIA_DISPLAY)
    if (!valueAsString) {
      this.saveMediaDisplayValue(DEFAULT_MEDIA_DISPLAY_IN_SAME_PAGE_VALUE)
      return DEFAULT_MEDIA_DISPLAY_IN_SAME_PAGE_VALUE
    }
    return JSON.parse(valueAsString)
  },

  saveMediaDisplayValue (displayOnSamePage) {
    localStorage.setItem(LOCAL_PARAMETER_MEDIA_DISPLAY, JSON.parse(displayOnSamePage))
  }
}

export { LocalParameterService }
