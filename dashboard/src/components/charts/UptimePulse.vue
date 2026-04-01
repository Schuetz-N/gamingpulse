<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import {
  Chart, LineController, LineElement, PointElement,
  LinearScale, TimeScale, Tooltip, Legend
} from 'chart.js'
import 'chartjs-adapter-date-fns'
import type { ServiceHealthRecord } from '../../types'

Chart.register(LineController, LineElement, PointElement, LinearScale, TimeScale, Tooltip, Legend)

const props = defineProps<{
  records: ServiceHealthRecord[]
}>()

const canvas = ref<HTMLCanvasElement | null>(null)
let chart: Chart<'line'> | null = null

const SERVICE_COLORS: Record<string, string> = {
  backend: '#34d399',
  n8n:     '#60a5fa',
  groq:    '#a78bfa'
}

function statusToValue(status: string): number {
  if (status === 'up')       return 1
  if (status === 'degraded') return 0.5
  return 0
}

function buildDatasets() {
  const grouped: Record<string, ServiceHealthRecord[]> = {}
  for (const r of props.records) {
    if (!grouped[r.service]) grouped[r.service] = []
    grouped[r.service].push(r)
  }

  return Object.entries(grouped).map(([service, entries]) => ({
    label:                service,
    data:                 entries.map(e => ({
                            x: new Date(e.checkedAt).getTime(),
                            y: statusToValue(e.status)
                          })),
    borderColor:          SERVICE_COLORS[service] ?? '#8b8fa3',
    backgroundColor:      'transparent',
    borderWidth:          2,
    pointRadius:          2,
    pointHoverRadius:     4,
    stepped:              'before' as const,
    tension:              0
  }))
}

function buildChart() {
  if (!canvas.value) return

  chart = new Chart<'line'>(canvas.value, {
    type: 'line',
    data: { datasets: buildDatasets() },
    options: {
      responsive:          true,
      maintainAspectRatio: false,
      interaction: {
        mode:      'index',
        intersect: false
      },
      plugins: {
        legend: {
          display:  true,
          position: 'top',
          labels: {
            color:     '#8b8fa3',
            boxWidth:  12,
            boxHeight: 2,
            padding:   16,
            font:      { size: 12 }
          }
        },
        tooltip: {
          backgroundColor: '#1a1d27',
          borderColor:     '#2a2d37',
          borderWidth:     1,
          titleColor:      '#8b8fa3',
          bodyColor:       '#e1e4ea',
          padding:         10,
          callbacks: {
            label: item => {
              const v = item.raw as { x: number; y: number }
              const label = v.y === 1 ? 'up' : v.y === 0.5 ? 'degraded' : 'down'
              return ` ${item.dataset.label}: ${label}`
            }
          }
        }
      },
      scales: {
        x: {
          type: 'time',
          time: {
            unit:          'minute',
            tooltipFormat: 'HH:mm'
          },
          grid:  { color: '#1f222c' },
          ticks: { color: '#8b8fa3', maxTicksLimit: 6 }
        },
        y: {
          min:  -0.1,
          max:   1.1,
          grid:  { color: '#1f222c' },
          ticks: {
            color:    '#8b8fa3',
            stepSize: 0.5,
            callback: (v) => {
              if (v === 1)   return 'up'
              if (v === 0.5) return 'degr.'
              if (v === 0)   return 'down'
              return ''
            }
          }
        }
      }
    }
  })
}

function updateChart() {
  if (!chart) return
  chart.data.datasets = buildDatasets()
  chart.update('none')
}

onMounted(buildChart)

onUnmounted(() => {
  chart?.destroy()
  chart = null
})

watch(() => props.records, updateChart)
</script>

<template>
  <div class="card">
    <h2>Service Uptime</h2>
    <div v-if="records.length === 0" class="empty">
      Collecting data — first records appear after 30s.
    </div>
    <div v-else class="chart-container">
      <canvas ref="canvas" />
    </div>
  </div>
</template>

<style scoped>
.chart-container {
  position: relative;
  height: 160px;
}
</style>