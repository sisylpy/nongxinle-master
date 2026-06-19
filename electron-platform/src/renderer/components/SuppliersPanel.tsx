import type { PlatformOrderLine, PlatformSupplierItem } from '../types/platform'
import { isLineAssigned, isLinePending } from '../utils/orderLine'

interface Props {
  selectedLine: PlatformOrderLine | null
  suppliers: PlatformSupplierItem[]
  loading: boolean
  assigning: boolean
  selectedDisGoodsId: number | null
  onSelectSupplier: (item: PlatformSupplierItem) => void
  onAssign: (scope: 'ORDER_ONLY' | 'ORDER_AND_DEFAULT') => void
}

export default function SuppliersPanel({
  selectedLine,
  suppliers,
  loading,
  assigning,
  selectedDisGoodsId,
  onSelectSupplier,
  onAssign
}: Props) {
  const isPending = selectedLine != null && isLinePending(selectedLine)
  const isAssigned = selectedLine != null && isLineAssigned(selectedLine)
  const canAssign = isPending && selectedDisGoodsId != null && !assigning

  return (
    <aside className="panel panel-right">
      <div className="panel-head">
        <span>{isAssigned ? '分配结果' : '候选配送商'}</span>
        {isPending && selectedLine && <span className="badge">{suppliers.length} 家</span>}
      </div>
      <div className="panel-body">
        {!selectedLine && <div className="empty">请选择一行订单</div>}

        {isAssigned && selectedLine && (
          <div className="assigned-readonly">
            <div className="assigned-readonly-title">本单已分配，不可再次操作</div>
            <div className="assigned-readonly-row">
              <span className="label">订单号</span>
              <span>{selectedLine.orderId}</span>
            </div>
            <div className="assigned-readonly-row">
              <span className="label">标准商品</span>
              <span>{selectedLine.goodsName || `#${selectedLine.nxGoodsId}`}</span>
            </div>
            <div className="assigned-readonly-row">
              <span className="label">配送商</span>
              <span>{selectedLine.assignedDistributerId ?? '—'}</span>
            </div>
            <div className="assigned-readonly-row">
              <span className="label">配送商商品</span>
              <span>{selectedLine.assignedDisGoodsId ?? '—'}</span>
            </div>
            <div className="assigned-readonly-row">
              <span className="label">成交价</span>
              <span>{selectedLine.orderPrice != null ? `¥${selectedLine.orderPrice}` : '—'}</span>
            </div>
          </div>
        )}

        {isPending && loading && <div className="loading">加载配送商…</div>}
        {isPending && !loading && suppliers.length === 0 && (
          <div className="empty">无有效报价配送商（0.1 无效价已过滤）</div>
        )}
        {isPending &&
          !loading &&
          suppliers.map((s) => (
            <div
              key={s.disGoodsId}
              className={`supplier-item${selectedDisGoodsId === s.disGoodsId ? ' active' : ''}`}
              onClick={() => onSelectSupplier(s)}
            >
              <div className="supplier-head">
                <span className="supplier-name">
                  {s.distributerName || `配送商 #${s.distributerId}`}
                  {s.isDefaultRecommend === 1 && (
                    <span className="tag default" style={{ marginLeft: 6 }}>
                      默认
                    </span>
                  )}
                </span>
                <span className="supplier-price">¥{s.currentQuotePrice ?? '—'}</span>
              </div>
              <div className="supplier-meta">
                商品 {s.disGoodsId} · {s.goodsName || '—'}
                {s.standard ? ` · ${s.standard}` : ''}
                {s.brand ? ` · ${s.brand}` : ''}
                {s.customerHistoryPrice != null && (
                  <>
                    <br />
                    历史价 ¥{s.customerHistoryPrice}
                  </>
                )}
              </div>
            </div>
          ))}
      </div>

      {isAssigned && selectedLine ? (
        <div className="readonly-status">
          已分配 · 配送商 {selectedLine.assignedDistributerId ?? '—'} · 商品{' '}
          {selectedLine.assignedDisGoodsId ?? '—'}
          {selectedLine.orderPrice != null ? ` · ¥${selectedLine.orderPrice}` : ''}
        </div>
      ) : (
        <div className="assign-actions">
          <div className="hint">
            {selectedLine
              ? `订单 #${selectedLine.orderId} · 商品 #${selectedLine.nxGoodsId}`
              : '选择 PENDING 订单行与配送商后分配'}
          </div>
          <div className="row">
            <button className="primary" disabled={!canAssign} onClick={() => onAssign('ORDER_ONLY')}>
              临时分配本单
            </button>
            <button
              className="success"
              disabled={!canAssign}
              onClick={() => onAssign('ORDER_AND_DEFAULT')}
            >
              分配并设为以后默认
            </button>
          </div>
        </div>
      )}
    </aside>
  )
}
