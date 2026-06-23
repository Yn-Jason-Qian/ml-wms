/**
 * WebSocket STOMP 客户端 — PC 管理后台
 * 用于 Dashboard 实时任务/库存更新
 *
 * 依赖: SockJS + STOMP.js (CDN 引入或 npm 安装)
 * 简易实现: 使用原生 WebSocket + STOMP 帧解析
 */

import { ref, onMounted, onUnmounted } from 'vue'

const WS_URL = `ws://${window.location.hostname}:8080/ws-stomp`

export interface TaskNotification {
  eventType: string
  taskId: number
  taskNo: string
  taskType: string
  warehouseId: number
  assignTo: number
  timestamp: number
}

export function useWebSocket() {
  const connected = ref(false)
  const lastTask = ref<TaskNotification | null>(null)
  let ws: WebSocket | null = null
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  const listeners: Array<(n: TaskNotification) => void> = []

  function connect() {
    if (ws && ws.readyState === WebSocket.OPEN) return

    ws = new WebSocket(WS_URL)

    ws.onopen = () => {
      connected.value = true
      // STOMP CONNECT + SUBSCRIBE frames
      ws!.send('CONNECT\naccept-version:1.1,1.0\n\n\0')
      ws!.send('SUBSCRIBE\nid:sub-0\ndestination:/topic/tasks\n\n\0')
    }

    ws.onmessage = (event) => {
      try {
        const data = event.data
        if (!data.includes('MESSAGE')) return
        const bodyStart = data.indexOf('\n\n')
        if (bodyStart < 0) return
        const jsonStr = data.substring(bodyStart + 2).replace(/\0/g, '')
        const notification = JSON.parse(jsonStr) as TaskNotification
        lastTask.value = notification
        listeners.forEach(cb => cb(notification))
      } catch { /* non-JSON frame, ignore */ }
    }

    ws.onclose = () => {
      connected.value = false
      ws = null
      reconnectTimer = setTimeout(connect, 5000)
    }

    ws.onerror = () => {
      ws?.close()
    }
  }

  function onTask(cb: (n: TaskNotification) => void) {
    listeners.push(cb)
    return () => {
      const idx = listeners.indexOf(cb)
      if (idx >= 0) listeners.splice(idx, 1)
    }
  }

  onMounted(connect)

  onUnmounted(() => {
    if (reconnectTimer) clearTimeout(reconnectTimer)
    ws?.close(1000, 'unmount')
    ws = null
  })

  return { connected, lastTask, onTask, connect }
}
