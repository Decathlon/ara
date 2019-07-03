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
  <div>
    <management-menu/>
    <crud :url="url" name="communication" titleCaseName="Communication" :introduction="introduction" :fields="fields" :editOnly="true" />
  </div>
</template>

<script>
  import api from '../libs/api'
  import managementMenuComponent from '../components/management-menu'
  import crudComponent from '../components/crud'

  export default {
    name: 'management-communications',

    components: {
      'management-menu': managementMenuComponent,
      'crud': crudComponent
    },

    data () {
      return {
        introduction: 'Communications are messages broadcasted to other members of your project, ' +
                      'to inform them of scheduling or status of tests, or document how to modify tests.',
        fields: [
          {
            code: 'code',
            name: 'Code',
            columnTitle: 'Code',
            type: 'hidden',
            primaryKey: true
          },
          {
            code: 'name',
            name: 'Name',
            columnTitle: 'Communication',
            type: 'string',
            readOnly: true,
            businessKey: true,
            width: 256
          },
          {
            code: 'type',
            name: 'Type',
            columnTitle: 'Type',
            type: 'select',
            options: [
              { value: 'TEXT', label: 'Text' },
              { value: 'HTML', label: 'HTML' }
            ],
            required: true,
            width: 96
          },
          {
            code: 'message',
            name: 'Message',
            columnTitle: 'Message',
            type: 'textarea',
            required: false,
            width: undefined
          }
        ]
      }
    },

    computed: {
      url () {
        return api.paths.communications(this)
      }
    }
  }
</script>
