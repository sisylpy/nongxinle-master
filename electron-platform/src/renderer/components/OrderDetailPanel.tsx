import type { PlatformOrderDetailData, PlatformOrderLine } from '../types/platform'
import { isLineAssigned, sortOrderLines } from '../utils/orderLine'

interface Props {
  detail: PlatformOrderDetailData | null
  selectedOrderId: number | null
  loading: boolean
  onSelectLine: (line: PlatformOrderLine) => void
}

export default function OrderDetailPanel({
  detail,
  selectedOrderId,
  loading,
  onSelectLine
}: Props) {
  const sortedLines = detail ? sortOrderLines(detail.lines ?? []) : []

  return (
    <section className="panel panel-center">
      <div className="panel-head">
        <span>订单明细</span>
        {detail && <span className="badge">{sortedLines.length} 行</span>}
      </div>
      {detail && (
        <div className="detail-context">
          <strong>{detail.departmentName || `客户 #${detail.departmentId}`}</strong>
          <span>订货日 {detail.applyDate}</span>
        </div>
      )}
      <div className="panel-body">
        {loading && <div className="loading">加载明细…</div>}
        {!loading && !detail && (
          <div className="empty">请从左侧选择待分配客户</div>
        )}
        {!loading && detail && sortedLines.length === 0 && (
          <div className="empty">该客户暂无订单行</div>
        )}
        {!loading && detail && sortedLines.length > 0 && (
          <table className="line-table">
            <thead>
              <tr>
                <th>订单号</th>
                <th>商品</th>
                <th>规格</th>
                <th>数量</th>
                <th>状态</th>
                <th>默认推荐</th>
              </tr>
            </thead>
            <tbody>
              {sortedLines.map((line) => {
                const dr = line.defaultRecommend
                const assigned = isLineAssigned(line)
                const selected = selectedOrderId === line.orderId
                const rowClass = [
                  assigned ? 'row-assigned' : '',
                  selected ? 'selected' : '',
                  selected && assigned ? 'selected-assigned' : ''
                ]
                  .filter(Boolean)
                  .join(' ')

                return (
                  <tr
                    key={line.orderId}
                    className={rowClass}
                    onClick={() => onSelectLine(line)}
                  >
                    <td>{line.orderId}</td>
                    <td>
                      <div>{line.goodsName || `#${line.nxGoodsId}`}</div>
                      {line.remark && (
                        <div className="line-remark">{line.remark}</div>
                      )}
                    </td>
                    <td>{line.standard || '—'}</td>
                    <td>{line.quantity || '—'}</td>
                    <td>
                      {assigned ? (
                        <span className="tag assigned">ASSIGNED</span>
                      ) : (
                        <span className="tag pending">PENDING</span>
                      )}
                    </td>
                    <td>
                      {dr?.defaultDisGoodsId ? (
                        <span className="tag default">
                          配送商 {dr.defaultDistributerId} / 商品 {dr.defaultDisGoodsId}
                        </span>
                      ) : (
                        <span className="muted-dash">—</span>
                      )}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>
    </section>
  )
}
