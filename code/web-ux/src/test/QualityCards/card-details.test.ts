import { shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi, afterEach } from 'vitest'
import cardDetails from '../../components/QualityCards/cardDetails.vue'

describe('cardDetails.vue', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('Should open line content correctly', async () => {
    expect(cardDetails).toBeTruthy()

    const wrapper = shallowMount(cardDetails, {
      props: {
        cardInfo: {
          status: 'SUCCESS'
        },
        cardActive: false,
        cardValue: {
          line1: 'country',
          column: 'severity',
          cell: 'team'
        }
      }
    })

    const mockMethod = vi.spyOn(wrapper.vm, 'calcHeight')

    await wrapper.find('.lineCountry ').trigger('click')
  
    wrapper.vm.calcHeight('first-line')

    expect(mockMethod).toHaveBeenCalledWith('first-line')
    expect(wrapper.vm.showFirstLines).toBeTruthy()
    expect(wrapper.emitted()).toHaveProperty('changeHeight')
  })
})