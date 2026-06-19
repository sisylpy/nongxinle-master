import type { PlatformPendingCustomer } from '../types/platform'

interface Props {
  customers: PlatformPendingCustomer[]
  selectedDepartmentId: number | null
  loading: boolean
  onSelect: (departmentId: number) => void
}

export default function PendingPanel({ customers, selectedDepartmentId, loading, onSelect }: Props) {
  return (
    <aside className="panel panel-left">
      <div className="panel-head">
        <span>待分配客户</span>
        <span className="badge">{customers.length} 家</span>
      </div>
      <div className="panel-body">
        {loading && <div className="loading">加载中…</div>}
        {!loading && customers.length === 0 && (
          <div className="empty">暂无 PENDING 订单</div>
        )}
        {!loading &&
          customers.map((c) => (
            <div
              key={c.departmentId}
              className={`customer-item${selectedDepartmentId === c.departmentId ? ' active' : ''}`}
              onClick={() => onSelect(c.departmentId)}
            >
              <div className="customer-name">{c.departmentName || `客户 #${c.departmentId}`}</div>
              <div className="customer-meta">
                {c.pendingLineCount ?? 0} 行待分配
                {c.departmentOrderCode ? ` · ${c.departmentOrderCode}` : ''}
              </div>
            </div>
          ))}
      </div>
    </aside>
  )
}
