import type { PlatformOrderLine } from '../types/platform'

export function isLinePending(line: PlatformOrderLine): boolean {
  return line.assignStatus === 'PENDING' || !line.assignStatus
}

export function isLineAssigned(line: PlatformOrderLine): boolean {
  return line.assignStatus === 'ASSIGNED'
}

/** PENDING 在前，ASSIGNED 在后；同状态按 orderId 升序 */
export function sortOrderLines(lines: PlatformOrderLine[]): PlatformOrderLine[] {
  return [...lines].sort((a, b) => {
    const rank = (l: PlatformOrderLine) => (isLinePending(l) ? 0 : 1)
    const diff = rank(a) - rank(b)
    if (diff !== 0) return diff
    return a.orderId - b.orderId
  })
}

/** 优先选第一条 PENDING；若无则选第一行（只读） */
export function pickDefaultLine(lines: PlatformOrderLine[]): PlatformOrderLine | null {
  const sorted = sortOrderLines(lines)
  return sorted.find(isLinePending) ?? sorted[0] ?? null
}
