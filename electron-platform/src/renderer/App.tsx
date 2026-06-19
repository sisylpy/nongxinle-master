import { useCallback, useEffect, useState } from 'react'
import { getApiBase } from './api/client'
import { platformApi } from './api/platform'
import PendingPanel from './components/PendingPanel'
import OrderDetailPanel from './components/OrderDetailPanel'
import SuppliersPanel from './components/SuppliersPanel'
import type {
  PlatformOrderDetailData,
  PlatformOrderLine,
  PlatformPendingCustomer,
  PlatformSupplierItem
} from './types/platform'
import { isLinePending, pickDefaultLine } from './utils/orderLine'

function todayStr(): string {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

export default function App() {
  const [marketId, setMarketId] = useState(1)
  const [operatorId, setOperatorId] = useState(1)
  const [applyDate, setApplyDate] = useState(todayStr())

  const [customers, setCustomers] = useState<PlatformPendingCustomer[]>([])
  const [selectedDepartmentId, setSelectedDepartmentId] = useState<number | null>(null)
  const [detail, setDetail] = useState<PlatformOrderDetailData | null>(null)
  const [selectedLine, setSelectedLine] = useState<PlatformOrderLine | null>(null)
  const [suppliers, setSuppliers] = useState<PlatformSupplierItem[]>([])
  const [selectedDisGoodsId, setSelectedDisGoodsId] = useState<number | null>(null)

  const [pendingLoading, setPendingLoading] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [suppliersLoading, setSuppliersLoading] = useState(false)
  const [assigning, setAssigning] = useState(false)

  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)

  const loadPending = useCallback(async () => {
    setPendingLoading(true)
    setError(null)
    try {
      const data = await platformApi.pending(marketId, applyDate)
      setCustomers(data.customers ?? [])
      if (selectedDepartmentId != null) {
        const still = data.customers?.some((c) => c.departmentId === selectedDepartmentId)
        if (!still) {
          setSelectedDepartmentId(null)
          setDetail(null)
          setSelectedLine(null)
          setSuppliers([])
          setSelectedDisGoodsId(null)
        }
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : '加载待分配列表失败')
    } finally {
      setPendingLoading(false)
    }
  }, [marketId, applyDate, selectedDepartmentId])

  const loadDetail = useCallback(
    async (departmentId: number) => {
      setDetailLoading(true)
      setError(null)
      try {
        const data = await platformApi.detail(marketId, departmentId, applyDate)
        setDetail(data)
        const defaultLine = pickDefaultLine(data.lines ?? [])
        setSelectedLine(defaultLine)
        if (!defaultLine || !isLinePending(defaultLine)) {
          setSuppliers([])
          setSelectedDisGoodsId(null)
        }
      } catch (e) {
        setError(e instanceof Error ? e.message : '加载订单明细失败')
      } finally {
        setDetailLoading(false)
      }
    },
    [marketId, applyDate]
  )

  const loadSuppliers = useCallback(
    async (line: PlatformOrderLine, departmentId: number) => {
      if (!line.nxGoodsId) return
      setSuppliersLoading(true)
      setError(null)
      try {
        const list = await platformApi.suppliers(
          marketId,
          departmentId,
          line.nxGoodsId,
          line.standard
        )
        setSuppliers(list ?? [])
        const defaultItem = list?.find((s) => s.isDefaultRecommend === 1)
        if (defaultItem) {
          setSelectedDisGoodsId(defaultItem.disGoodsId)
        } else if (line.defaultRecommend?.defaultDisGoodsId) {
          const match = list?.find(
            (s) => s.disGoodsId === line.defaultRecommend!.defaultDisGoodsId
          )
          setSelectedDisGoodsId(match?.disGoodsId ?? null)
        } else {
          setSelectedDisGoodsId(list?.[0]?.disGoodsId ?? null)
        }
      } catch (e) {
        setError(e instanceof Error ? e.message : '加载配送商失败')
      } finally {
        setSuppliersLoading(false)
      }
    },
    [marketId]
  )

  useEffect(() => {
    void loadPending()
  }, [loadPending])

  useEffect(() => {
    if (selectedDepartmentId != null) {
      void loadDetail(selectedDepartmentId)
    }
  }, [selectedDepartmentId, loadDetail])

  useEffect(() => {
    if (!selectedLine || selectedDepartmentId == null) return
    if (isLinePending(selectedLine)) {
      void loadSuppliers(selectedLine, selectedDepartmentId)
    } else {
      setSuppliers([])
      setSelectedDisGoodsId(null)
      setSuppliersLoading(false)
    }
  }, [selectedLine, selectedDepartmentId, loadSuppliers])

  const handleSelectCustomer = (departmentId: number) => {
    setSelectedDepartmentId(departmentId)
    setMessage(null)
  }

  const handleSelectLine = (line: PlatformOrderLine) => {
    setSelectedLine(line)
    setMessage(null)
  }

  const handleSelectSupplier = (item: PlatformSupplierItem) => {
    setSelectedDisGoodsId(item.disGoodsId)
  }

  const handleAssign = async (scope: 'ORDER_ONLY' | 'ORDER_AND_DEFAULT') => {
    if (!selectedLine || selectedDisGoodsId == null || !isLinePending(selectedLine)) return
    setAssigning(true)
    setError(null)
    setMessage(null)
    try {
      const result = await platformApi.assign({
        marketId,
        orderId: selectedLine.orderId,
        disGoodsId: selectedDisGoodsId,
        switchScope: scope,
        operatorId
      })
      const scopeLabel =
        scope === 'ORDER_ONLY' ? '临时分配本单' : '分配并设为以后默认'
      setMessage(
        `分配成功（${scopeLabel}）：订单 ${result.orderId} → 配送商 ${result.nxDoDistributerId}，` +
          `价格 ¥${result.nxDoPrice ?? '—'}`
      )
      await loadPending()
      if (selectedDepartmentId != null) {
        await loadDetail(selectedDepartmentId)
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : '分配失败')
    } finally {
      setAssigning(false)
    }
  }

  const apiBase = getApiBase()

  return (
    <div className="app">
      <header className="header">
        <h1>平台订单分配</h1>
        <div className="header-meta">
          <label>
            市场
            <input
              type="number"
              value={marketId}
              onChange={(e) => setMarketId(Number(e.target.value) || 1)}
            />
          </label>
          <label>
            操作员
            <input
              type="number"
              value={operatorId}
              onChange={(e) => setOperatorId(Number(e.target.value) || 1)}
            />
          </label>
          <label>
            订货日
            <input
              className="date"
              type="date"
              value={applyDate}
              onChange={(e) => setApplyDate(e.target.value)}
            />
          </label>
          <button className="toolbar-btn" disabled={pendingLoading} onClick={() => void loadPending()}>
            刷新
          </button>
        </div>
      </header>

      {error && <div className="error-banner">{error}</div>}
      {message && <div className="success-banner">{message}</div>}

      <main className="main">
        <PendingPanel
          customers={customers}
          selectedDepartmentId={selectedDepartmentId}
          loading={pendingLoading}
          onSelect={handleSelectCustomer}
        />
        <OrderDetailPanel
          detail={detail}
          selectedOrderId={selectedLine?.orderId ?? null}
          loading={detailLoading}
          onSelectLine={handleSelectLine}
        />
        <SuppliersPanel
          selectedLine={selectedLine}
          suppliers={suppliers}
          loading={suppliersLoading}
          assigning={assigning}
          selectedDisGoodsId={selectedDisGoodsId}
          onSelectSupplier={handleSelectSupplier}
          onAssign={handleAssign}
        />
      </main>

      <footer className="footer-status">
        API: {apiBase || '（未配置 VITE_PLATFORM_API_BASE）'} · Phase 2a 第一版 · 不含快照/司机/大屏
      </footer>
    </div>
  )
}
