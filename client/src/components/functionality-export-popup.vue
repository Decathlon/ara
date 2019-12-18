<!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ~ Copyright (C) 2019 by the ARA Contributors
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ 	 http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  ~
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->
<template>
    <Modal ref="content" v-model="exportPopup" @on-cancel="cancelExport" @on-ok="exportCartography" title="Export functionalities..." width="500"
    okText="Export">
      Choose an export format :
      <Select v-model="exporterId" @on-change="changeCurrentExporter">
          <Option v-for="exporter in availableExporters" :value="exporter.id" :key="exporter.id" :label="exporter.name">
          </Option>
      </Select>
      <div :key="this.selectedExporterId">
        <div v-if="this.requiredFields && this.requiredFields !== {} ">
          <Form>
            <Form-item v-for="field in this.requiredFields" :key="field.id" :label="field.name + ' :'">
              <div>
                <form-field
                  :field="field"
                  ref="formField"
                  v-model="fieldValue[field.id]" />
              </div>
              <div style="color: gray; line-height: 1.2; margin-top: 4px;">
                {{field.description}}
              </div>
            </Form-item>
          </Form>
        </div>
      </div>
    </Modal>
</template>

<script>
import Vue from 'vue'
import api from '../libs/api'
import formFieldComponent from './form-field'

export default {
  components: {
    'form-field': formFieldComponent
  },

  data () {
    return {
      exportPopup: false,
      availableExporters: [],
      displayedFunctionalities: [],
      projectCode: '',
      exporterId: '',
      selectedExporterId: '',
      requiredFields: undefined,
      fieldValue: {}
    }
  },

  methods: {
    openExportPopup (projectCode, displayedFunctionalities) {
      this.projectCode = projectCode
      this.displayedFunctionalities = displayedFunctionalities
      Vue.http({ url: api.paths.functionalities(projectCode) + '/export', method: 'OPTIONS' })
        .then((response) => {
          this.availableExporters = response.body
          this.exportPopup = true
        }, (error) => {
          api.handleError(error)
        })
    },

    exportCartography () {
      if (!this.exporterId || this.exporterId === '') {
        this.$Modal.error({
          title: 'Error while exporting',
          content: 'You must choose an exporter type first'
        })
        return
      }
      let idsToExport = []
      this.displayedFunctionalities.forEach(f => idsToExport.push(f.id))
      let urlArgs = `exportType=${this.exporterId}&functionalities=${idsToExport.join(',')}`
      if (this.fieldValue !== {}) {
        let keys = Object.keys(this.fieldValue)
        for (var idx in keys) {
          let key = keys[idx]
          let element = this.fieldValue[key]
          urlArgs = `${urlArgs}&${key}=${element}`
        }
      }
      Vue.http
        .get(api.paths.functionalities(this.projectCode) + `/export?${urlArgs}`, api.REQUEST_OPTIONS)
        .then((response) => {
          this.triggerDownload(response.bodyText)
          this.exporterId = ''
        }, (error) => {
          api.handleError(error)
        })
    },

    triggerDownload (content) {
      const blob = new Blob([content], { type: 'text/plain' })
      const event = document.createEvent('MouseEvents')
      let anchor = document.createElement('a')
      let exporterFormat = ''
      for (let idx in this.availableExporters) {
        if (this.availableExporters[idx].id === this.exporterId) {
          exporterFormat = this.availableExporters[idx].format
        }
      }
      anchor.download = `exportAraCartography-${this.$route.params.projectCode}.${exporterFormat}`
      anchor.href = window.URL.createObjectURL(blob)
      anchor.dataset.downloadurl = ['text/json', anchor.download, anchor.href].join(':')
      event.initEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null)
      anchor.dispatchEvent(event)
    },

    cancelExport () {
      this.exporterId = ''
      this.fieldValue = {}
    },

    changeCurrentExporter (id) {
      let currentExporter = this.availableExporters.filter((ex) => ex.id === id)[0]
      this.requiredFields = (currentExporter) ? currentExporter.requiredFields : undefined
      this.selectedExporterId = id
      this.fieldValue = {}
      return id
    }
  }
}
</script>
