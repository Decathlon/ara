<template>
  <div>
    <span v-if="field.readOnly || (!editingNew && (field.primaryKey || field.createOnly))"
          style="padding: 0 8px;">{{value}}</span>
    <Checkbox v-else-if="field.type === 'boolean'"
              :value="value"
              @on-change="value => $emit('input', value)">{{hideCheckboxLabel ? '' : field.name}}</Checkbox>
    <Input v-else-if="field.type === 'textarea' || field.type === 'password' || field.type === 'string'"
           :type="field.type === 'textarea' ? 'textarea' : field.type === 'password' ? 'password' : 'text'"
           :autosize="{ minRows: 2, maxRows: 15 }"
           :value="value"
           @on-change="$emit('input', $event.target.value)"
           @keydown.esc.native="escape"
           ref="fieldInput"
           @on-enter="field.type === 'textarea' ? null : enter()" />
    <Select v-else-if="field.type === 'select'"
            :value="value"
            @on-change="value => $emit('input', value)">
      <Option v-for="option in field.options" :value="option.value" :key="option.value">{{!option.label || option.label === ' ' ? '\u00A0' : option.label}}</Option>
    </Select>
    <InputNumber v-else-if="field.type === 'int'"
                 :value="value"
                 @on-change="value => $emit('input', value)"
                 ref="fieldInput"/>
    <div v-if="field.primaryKey || field.createOnly" style="color: lightgray; line-height: 1.2; margin-top: 4px;">
      Once created, this field is unmodifiable because {{field.createOnlyBecause ? field.createOnlyBecause : 'it is referenced elsewhere'}}.
    </div>
  </div>
</template>

<script>
  export default {
    name: 'form-field',

    props: {
      field: {
        // The object describing the field to edit.
        // @see crud.vue fields property documentation for all available properties and values
        type: Object,
        required: true
      },
      editingNew: {
        // True if currently editing a new entity (creating a new one) instead of just editing: primaryKey and createOnly fields will be read-only
        type: Boolean,
        required: false
      },
      value: {
        // The actual value to load at startup.
        // Use <form-field v-model="fieldValue"/> instead of <form-field :value="fieldValue"/> for automatic two-way binding
      },
      hideCheckboxLabel: {
        // True to hide the label of a checkbox field (if it is already present elsewhere, for instance)
        type: Boolean
      }
    },

    methods: {
      focus () {
        // No ref for Checkbox & Select (not focusable)
        if (this.$refs.fieldInput) {
          this.$refs.fieldInput.focus()
        }
      },

      enter () {
        this.$emit('enter')
      },

      escape () {
        this.$emit('escape')
      }
    }
  }
</script>
