const BASE_URL = '/api'

async function fetchJson<T>(url: string): Promise<T> {
  const response = await fetch(`${BASE_URL}${url}`)
  if (!response.ok) throw new Error(`HTTP ${response.status}`)
  return response.json()
}

export interface ServiceStatus {
  status: string
  code?: number
  error?: string
}

export interface StatusResponse {
  services: {
    backend: ServiceStatus
    ollama: ServiceStatus
    n8n: ServiceStatus
  }
  stats: {
    postsToday: number
    seenUrls: number
    postsBySource: Record<string, number>
  }
}

export interface Post {
  id: number
  title: string
  link: string
  source: string
  category: string
  summary: string
  postedAt: string
  success: boolean
}

export interface ErrorEntry {
  id: number
  source: string
  message: string
  url: string | null
  occurredAt: string
}

export const api = {
  getStatus: () => fetchJson<StatusResponse>('/status'),
  getPosts: () => fetchJson<Post[]>('/posts'),
  getPostStats: () => fetchJson<{ todayCount: number; bySource: Record<string, number> }>('/posts/stats'),
  getErrors: () => fetchJson<ErrorEntry[]>('/errors'),
  getDedupCount: () => fetchJson<{ seenUrls: number }>('/dedup/count')
}
