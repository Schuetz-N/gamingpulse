import { onMounted, onUnmounted } from 'vue'

export function useInterval(callback: () => void, intervalMs: number, immediate = true) {
  let timer: number | null = null

  const start = () => {
    if (timer !== null) return
    timer = window.setInterval(callback, intervalMs)
  }

  const stop = () => {
    if (timer === null) return
    clearInterval(timer)
    timer = null
  }

  onMounted(() => {
    if (immediate) callback()
    start()
  })

  onUnmounted(() => {
    stop()
  })

  return { start, stop }
}