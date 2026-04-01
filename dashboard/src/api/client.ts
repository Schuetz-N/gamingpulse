import type {
  StatusResponse,
  ServiceHealthRecord,
  Post,
  PostPage,
  PostBucketEntry,
  ErrorEntry
} from '../types'

const BASE_URL = '/api'
const API_TOKEN = import.meta.env.VITE_API_AUTH_TOKEN

async function fetchJson<T>(url: string): Promise<T> {
  const response = await fetch(`${BASE_URL}${url}`, {
    headers: {
      'Authorization': `Bearer ${API_TOKEN}`
    }
  })
  if (!response.ok) throw new Error(`HTTP ${response.status}`)
  return response.json()
}

export const api = {
  getStatus: () =>
    fetchJson<StatusResponse>('/status'),

  getStatusHistory: (minutes = 60) =>
    fetchJson<ServiceHealthRecord[]>(`/status/history?minutes=${minutes}`),

  getPosts: (page = 0, size = 50) =>
    fetchJson<PostPage>(`/posts?page=${page}&size=${size}`),

  getPostStats: () =>
    fetchJson<{ todayCount: number; bySource: Record<string, number> }>('/posts/stats'),

  getPostHistory: (range: '24h' | '7d' | '30d' = '24h') =>
    fetchJson<PostBucketEntry[]>(`/posts/history?range=${range}`),

  getErrors: () =>
    fetchJson<ErrorEntry[]>('/errors'),

  getErrorStats: () =>
    fetchJson<Record<string, number>>('/errors/stats'),

  getDedupCount: () =>
    fetchJson<{ seenUrls: number }>('/dedup/count')
}