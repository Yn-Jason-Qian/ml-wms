/**
 * WebSocket STOMP 客户端 — PC 管理后台
 * 用于 Dashboard 实时任务/库存更新
 *
 * 依赖: SockJS + STOMP.js (CDN 引入或 npm 安装)
 * 简易实现: 使用原生 WebSocket + STOMP 帧解析
 */

import { ref, onMounted, onUnmounted } from 'vue'

/**
 * WebSocket 地址：默认同源。
 * 开发期由 vite 的 /ws-stomp 代理转发，生产期由 nginx 反代，
 * 两种情况都不需要知道后端跑在哪台机器上。
 * 需要单独指定时用 VITE_WS_BASE_URL 覆盖。
 */
function resolveWebSocketUrl(): string {
  const configured = String(import.meta.env.VITE_WS_BASE_URL || '').trim()
  if (configured) {
    return configured
  }
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${window.location.host}/ws-stomp`
}

const WS_URL = resolveWebSocketUrl()

/** 重连退避：5s 起，逐次翻倍，最长 60s */
const RECONNECT_BASE_DELAY = 5000
const RECONNECT_MAX_DELAY = 60000

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
  let reconnectDelay = RECONNECT_BASE_DELAY
  let stopped = false
  const listeners: Array<(n: TaskNotification) => void> = []

  /** 只在状态真的变化时改 ref，避免每次重连失败都触发一轮重渲染 */
  function setConnected(value: boolean) {
    if (connected.value !== value) {
      connected.value = value
    }
  }

  function scheduleReconnect() {
    if (stopped || reconnectTimer) return
    reconnectTimer = setTimeout(() => {
      reconnectTimer = null
      connect()
    }, reconnectDelay)
    reconnectDelay = Math.min(reconnectDelay * 2, RECONNECT_MAX_DELAY)
  }

  function connect() {
    if (stopped || (ws && ws.readyState === WebSocket.OPEN)) return

    ws = new WebSocket(WS_URL)

    ws.onopen = () => {
      setConnected(true)
      reconnectDelay = RECONNECT_BASE_DELAY
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
      setConnected(false)
      ws = null
      scheduleReconnect()
    }

    ws.onerror = () => {
      // 交给 onclose 统一处理重连，这里再 close 一次会触发两轮重连
      setConnected(false)
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
    stopped = true
    if (reconnectTimer) clearTimeout(reconnectTimer)
    reconnectTimer = null
    ws?.close(1000, 'unmount')
    ws = null
    setConnected(false)
  })

  return { connected, lastTask, onTask, connect }
}
