import { ref } from 'vue'
import { api } from '../api/client'
import { useInterval } from './useInterval'
import type { StatusResponse, ServiceHealthRecord } from '../types'

export function useStatus() {
  const status      = ref<StatusResponse | null>(null)
  const history     = ref<ServiceHealthRecord[]>([])
  const loading     = ref(true)
  const error       = ref<string | null>(null)

  async function fetchStatus() {
    try {
      const [statusData, historyData] = await Promise.all([
        api.getStatus(),
        api.getStatusHistory(60)
      ])
      status.value  = statusData
      history.value = historyData
      error.value   = null
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Could not reach backend'
    } finally {
      loading.value = false
    }
  }

  useInterval(fetchStatus, 15000)

  return { status, history, loading, error, refresh: fetchStatus }
}