// Services & Status
export interface ServiceStatus {
  status: 'up' | 'down' | 'degraded' | 'starting'
  error?: string
  code?: number
}

export interface StatusResponse {
  services: {
    backend: ServiceStatus
    n8n:     ServiceStatus
    groq:    ServiceStatus
  }
  stats: {
    postsToday:    number
    seenUrls:      number
    postsBySource: Record<string, number>
  }
}

export interface ServiceHealthRecord {
  id:          number
  service:     string
  status:      'up' | 'down' | 'degraded' | 'starting'
  errorDetail: string | null
  checkedAt:   string
}

// Posts
export interface Post {
  id:       number
  title:    string
  link:     string
  source:   string
  category: string
  summary:  string
  postedAt: string
  success:  boolean
}

export interface PostPage {
  content:          Post[]
  totalElements:    number
  totalPages:       number
  number:           number
  size:             number
  first:            boolean
  last:             boolean
}

export interface PostBucketEntry {
  bucket: string
  count:  number
}

// Errors
export interface ErrorEntry {
  id:         number
  source:     string
  message:    string
  url:        string | null
  occurredAt: string
}