import { apiPost } from './client'
import type {
  PlatformAssignResult,
  PlatformOrderDetailData,
  PlatformPendingData,
  PlatformSupplierItem
} from '../types/platform'

export const platformApi = {
  pending(marketId: number, applyDate?: string) {
    return apiPost<PlatformPendingData>('/api/platform/orders/pending', { marketId, applyDate })
  },

  detail(marketId: number, departmentId: number, applyDate?: string, orderIds?: number[]) {
    return apiPost<PlatformOrderDetailData>('/api/platform/orders/detail', {
      marketId,
      departmentId,
      applyDate,
      orderIds
    })
  },

  suppliers(marketId: number, departmentId: number, nxGoodsId: number, standard?: string) {
    return apiPost<PlatformSupplierItem[]>('/api/platform/goods/suppliers', {
      marketId,
      departmentId,
      nxGoodsId,
      standard
    })
  },

  assign(params: {
    marketId: number
    orderId: number
    disGoodsId: number
    switchScope: 'ORDER_ONLY' | 'ORDER_AND_DEFAULT'
    operatorId: number
    reasonCode?: string
    reasonNote?: string
  }) {
    return apiPost<PlatformAssignResult>('/api/platform/orders/assign', {
      reasonCode: 'OTHER',
      ...params
    })
  }
}
