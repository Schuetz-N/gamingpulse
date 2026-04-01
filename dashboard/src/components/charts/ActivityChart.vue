<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { Chart, LineController, LineElement, PointElement, LinearScale, TimeScale, Filler, Tooltip } from 'chart.js'
import 'chartjs-adapter-date-fns'
import type { PostBucketEntry } from '../../types'

Chart.register(LineController, LineElement, PointElement, LinearScale, TimeScale, Filler, Tooltip)

const props = defineProps<{
  data:  PostBucketEntry[]
  range: '24h' | '7d' | '30d'
}>()

const canvas  = ref<HTMLCanvasElement | null>(null)
let chart: Chart | null = null

function buildChart() {
  if (!canvas.value) return

  const labels = props.data.map(e => new Date(e.bucket))
  const values = props.data.map(e => e.count)

  chart = new Chart(canvas.value, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        data:            values,
        borderColor:     '#60a5fa',
        backgroundColor: 'rgba(96, 165, 250, 0.08)',
        borderWidth:     2,
        pointRadius:     3,
        pointHoverRadius: 5,
        pointBackgroundColor: '#60a5fa',
        fill:            true,
        tension:         0.3
      }]
    },
    options: {
      responsive:          true,
      maintainAspectRatio: false,
      interaction: {
        mode:      'index',
        intersect: false
      },
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#1a1d27',
          borderColor:     '#2a2d37',
          borderWidth:     1,
          titleColor:      '#8b8fa3',
          bodyColor:       '#e1e4ea',
          padding:         10,
          callbacks: {
            title: items => items[0]?.label ?? '',
            label: item  => ` ${item.raw} posts`
          }
        }
      },
      scales: {
        x: {
          type: 'time',
          time: {
            unit:         props.range === '24h' ? 'hour' : 'day',
            tooltipFormat: props.range === '24h' ? 'HH:mm' : 'MMM d'
          },
          grid:  { color: '#1f222c' },
          ticks: { color: '#8b8fa3', maxTicksLimit: 8 }
        },
        y: {
          beginAtZero: true,
          grid:  { color: '#1f222c' },
          ticks: {
            color:     '#8b8fa3',
            precision: 0
          }
        }
      }
    }
  })
}

function updateChart() {
  if (!chart) return
  chart.data.labels   = props.data.map(e => new Date(e.bucket))
  chart.data.datasets[0].data = props.data.map(e => e.count)
  chart.update('none')
}

onMounted(buildChart)

onUnmounted(() => {
  chart?.destroy()
  chart = null
})

watch(() => props.data, updateChart)
</script>

<template>
  <div class="card">
    <h2>Post Activity</h2>
    <div class="chart-container">
      <canvas ref="canvas" />
    </div>
  </div>
</template>

<style scoped>
.chart-container {
  position: relative;
  height: 200px;
}
</style>