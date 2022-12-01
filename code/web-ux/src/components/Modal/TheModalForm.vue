<script setup>
const props = defineProps(["show"]);
</script>

<template>
  <Transition name="modal">
    <div v-if="props.show" class="modal-mask">
      <div class="modal-wrapper">
        <div class="modal-container">
          <div class="modal-header">
            <slot name="header">default header</slot>
          </div>

          <div class="modal-body">
            <slot name="body">default body</slot>
          </div>

          <div class="modal-footer vtmn-flex vtmn-w-full vtmn-justify-center">
            <slot name="footer">
              <button class="modal-default-button" @click="$emit('close')">
                Confirm
              </button>
            </slot>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style>
.modal-mask {
  position: fixed;
  z-index: 9998;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: table;
  transition: opacity 0.3s ease;
}

.modal-wrapper {
  display: table-cell;
  vertical-align: middle;
}

.modal-container {
  width: 60%;
  margin: 0px auto;
  padding: 20px 30px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
  transition: all 0.3s ease;
}

.modal-header h3 {
  margin-top: 0;
  color: var(--vtmn-semantic-color_content-active);
}

.modal-body {
  margin: 20px 0;
}

.modal-default-button {
  float: right;
}

/*
 * The following styles are auto-applied to elements with
 * transition="modal" when their visibility is toggled
 * by Vue.js.
 *
 * You can easily play with the modal transition by editing
 * these styles.
 */

.modal-enter-from {
  opacity: 0;
}

.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  -webkit-transform: scale(1.1);
  transform: scale(1.1);
}

.modalDesc {
  color: var(--vtmn-semantic-color_content-tertiary);
}

.cancelModalBtn,
.cancelModalBtn:hover {
  background-color: var(--vtmn-semantic-color_background-tertiary) !important;
  color: var(--vtmn-semantic-color_content-inactive) !important;
}

.confirmModalBtn {
  background-color: var(--vtmn-semantic-color_content-tertiary);
}

.confirmModalBtn.active {
  background-color: var(--vtmn-semantic-color_content-positive) !important;
  color: var(--vtmn-semantic-color_content-action-reversed) !important;
}
</style>
