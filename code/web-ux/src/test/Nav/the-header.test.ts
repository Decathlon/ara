import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import Header from '../../components/Nav/TheHeader.vue'

describe('Header.vue', () => {
  it('Should open sidebar', async () => {
    const wrapper = mount(Header)

    await wrapper.find('button').trigger('click')

    wrapper.vm.$nextTick(() => {
      expect(wrapper.emitted()).toHaveProperty('opened-menu');
    })
  })  
})