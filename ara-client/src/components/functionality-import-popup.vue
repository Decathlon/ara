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
    <Modal v-model="importPopup" title="Import functionalities..." width="300">
        Choose the functionalities to import (in ARA export format) : 
        <Upload
            type="drag"
            :action="importUrl"
            name="functionalities"
            :on-success="importCartography"
            :on-error="importCartography">
            <div style="padding: 20px 0">
                <Icon type="ios-cloud-upload" size="52" style="color: #3399ff"></Icon>
                <p>Click or drag files here to upload</p>
            </div>
        </Upload>
        <div slot="footer">
            <Button @click="importPopup = false">Cancel</Button>
        </div>
    </Modal>
</template>

<script>
import api from '../libs/api'

export default {
  data () {
    return {
      importPopup: false,
      projectCode: ''
    }
  },

  computed: {
    importUrl () {
      return api.paths.functionalities(this.projectCode) + '/import'
    }
  },

  methods: {
    openImportPopup (projectCode) {
      this.projectCode = projectCode
      this.importPopup = true
    },

    importCartography () {
      this.importPopup = false
      location.reload()
    }
  }
}
</script>