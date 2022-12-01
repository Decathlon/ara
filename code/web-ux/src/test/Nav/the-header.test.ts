import { mount } from '@vue/test-utils'
import { describe, expect, test } from 'vitest'
import Header from '../../components/Nav/TheHeader.vue'

describe('Header.vue', () => {
  test('Should open sidebar', async () => {
    const wrapper = mount(Header)

    await wrapper.find('button').trigger('click')

    wrapper.vm.$nextTick(() => {
      expect(wrapper.emitted()).toHaveProperty('opened-menu');
      expect(wrapper.text()).toBeTruthy();
    })
  })  
})