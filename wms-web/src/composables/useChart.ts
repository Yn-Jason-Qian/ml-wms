import { ref, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'

export function useChart() {
  const chartRef = ref<HTMLElement>()
  let chartInstance: echarts.ECharts | null = null

  function initChart() {
    if (chartRef.value) {
      chartInstance = echarts.init(chartRef.value)
      window.addEventListener('resize', resizeHandler)
    }
    return chartInstance
  }

  function resizeHandler() {
    chartInstance?.resize()
  }

  function disposeChart() {
    window.removeEventListener('resize', resizeHandler)
    chartInstance?.dispose()
    chartInstance = null
  }

  onBeforeUnmount(disposeChart)

  return { chartRef, initChart, disposeChart }
}
