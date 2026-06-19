import type { ApiResponse } from '../types/platform'

const API_BASE = (import.meta.env.VITE_PLATFORM_API_BASE as string) || ''

export function getApiBase(): string {
  return API_BASE
}

export class ApiError extends Error {
  constructor(message: string) {
    super(message)
    this.name = 'ApiError'
  }
}

async function parseResponse<T>(res: Response): Promise<T> {
  const text = await res.text()
  let json: ApiResponse<T>
  try {
    json = JSON.parse(text)
  } catch {
    throw new ApiError(`HTTP ${res.status}: 响应非 JSON`)
  }
  if (json.code !== 0) {
    throw new ApiError(json.msg || '请求失败')
  }
  return json.data as T
}

export async function apiPost<T>(path: string, body?: unknown): Promise<T> {
  const base = API_BASE.replace(/\/$/, '')
  const res = await fetch(`${base}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: body == null ? undefined : JSON.stringify(body)
  })
  if (!res.ok) {
    throw new ApiError(`HTTP ${res.status}`)
  }
  return parseResponse<T>(res)
}
