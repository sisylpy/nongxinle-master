export interface ApiResponse<T> {
  code: number
  msg?: string
  data?: T
}

export interface PlatformDefaultRecommend {
  defaultId?: number
  defaultDistributerId?: number
  defaultDisGoodsId?: number
  source?: string
}

export interface PlatformPendingCustomer {
  departmentId: number
  departmentName?: string
  departmentOrderCode?: string
  pendingLineCount?: number
  orderIds?: number[]
  lastPendingAt?: string
}

export interface PlatformPendingData {
  marketId: number
  applyDate: string
  totalPendingLines?: number
  customers: PlatformPendingCustomer[]
}

export interface PlatformOrderLine {
  orderId: number
  platformAssignId?: number
  nxGoodsId?: number
  goodsName?: string
  quantity?: string
  standard?: string
  remark?: string
  assignStatus?: string
  assignMode?: string
  assignedDistributerId?: number
  assignedDisGoodsId?: number
  orderPrice?: string
  orderSubtotal?: string
  defaultRecommend?: PlatformDefaultRecommend | null
}

export interface PlatformOrderDetailData {
  marketId: number
  departmentId: number
  departmentName?: string
  applyDate: string
  lines: PlatformOrderLine[]
  distributerSummary?: unknown[]
}

export interface PlatformSupplierItem {
  disGoodsId: number
  distributerId: number
  distributerName?: string
  nxGoodsId?: number
  goodsName?: string
  standard?: string
  brand?: string
  place?: string
  currentQuotePrice?: string
  customerHistoryPrice?: string | null
  isDefaultRecommend?: number
}

export interface PlatformAssignResult {
  orderId: number
  platformAssignId?: number
  assignStatus?: string
  nxDoDistributerId?: number
  nxDoDisGoodsId?: number
  nxDoCollaborativeNxDisId?: number
  nxDoPrice?: string
  nxDoSubtotal?: string
  defaultId?: number
  switchLogId?: number
}

export interface PlatformConfig {
  apiBase: string
  marketId: number
  operatorId: number
  applyDate: string
}
