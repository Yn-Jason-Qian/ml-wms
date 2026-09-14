/**
 * WebSocket STOMP 客户端 Composable
 * 实时接收任务推送和库存变更通知
 *
 * 注意: UniApp 原生不支持 STOMP-over-WebSocket 库，
 * 此处使用 UniApp 原生 WebSocket API 直连 STOMP 端点，
 * 或通过简易 WebSocket 订阅 /topic/tasks
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { WS_URL } from '@/utils/env'

/** 重连退避：3s 起，逐次翻倍，最长 30s */
const RECONNECT_BASE_DELAY = 3000
const RECONNECT_MAX_DELAY = 30000

export interface TaskNotification {
  eventType: string  // CLAIMED | STARTED | COMPLETED | CANCELLED | NEW
  taskId: number
  taskNo: string
  taskType: string
  warehouseId: number
  assignTo: number
  timestamp: number
}

export function useWebSocket() {
  const connected = ref(false)
  const lastNotification = ref<TaskNotification | null>(null)
  const notifications = ref<TaskNotification[]>([])
  let socketTask: UniApp.SocketTask | null = null
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  let reconnectDelay = RECONNECT_BASE_DELAY
  let stopped = false
  let warned = false
  let callbacks: Array<(n: TaskNotification) => void> = []

  /** 只在状态真的变化时改 ref —— 否则每次失败都会触发一轮页面重渲染 */
  function setConnected(value: boolean) {
    if (connected.value !== value) {
      connected.value = value
    }
  }

  function connect() {
    if (socketTask || stopped) return

    if (!WS_URL) {
      if (!warned) {
        warned = true
        console.warn('[PDA] 未配置 WebSocket 地址，实时推送已禁用')
      }
      return
    }

    socketTask = uni.connectSocket({
      url: WS_URL,
      success: () => { setConnected(true) }
    })

    socketTask.onOpen(() => {
      setConnected(true)
      reconnectDelay = RECONNECT_BASE_DELAY
      // 订阅 STOMP 格式
      const subscribeFrame = [
        'CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\0',
        'SUBSCRIBE\nid:sub-0\ndestination:/topic/tasks\n\n\0'
      ].join('')
      socketTask?.send({ data: subscribeFrame })
    })

    socketTask.onMessage((res) => {
      try {
        // STOMP MESSAGE frame → 提取 body JSON
        const bodyStart = res.data.indexOf('\n\n')
        if (bodyStart < 0) return
        const jsonStr = res.data.substring(bodyStart + 2).replace('\0', '')
        const notification = JSON.parse(jsonStr) as TaskNotification

        lastNotification.value = notification
        notifications.value.unshift(notification)
        if (notifications.value.length > 50) notifications.value.pop()

        for (const cb of callbacks) cb(notification)
      } catch {
        // 非 JSON 消息忽略（STOMP heartbeat 等）
      }
    })

    socketTask.onClose(() => {
      setConnected(false)
      socketTask = null
      scheduleReconnect()
    })

    socketTask.onError(() => {
      // 不在这里 close()：close 事件会自己到达，重复调用会触发两轮重连
      setConnected(false)
    })
  }

  function scheduleReconnect() {
    if (stopped || reconnectTimer || !WS_URL) return
    reconnectTimer = setTimeout(() => {
      reconnectTimer = null
      connect()
    }, reconnectDelay)
    reconnectDelay = Math.min(reconnectDelay * 2, RECONNECT_MAX_DELAY)
  }

  function disconnect() {
    stopped = true
    if (reconnectTimer) clearTimeout(reconnectTimer)
    reconnectTimer = null
    socketTask?.close({ code: 1000, reason: 'user disconnect' })
    socketTask = null
    setConnected(false)
  }

  function onNotification(cb: (n: TaskNotification) => void) {
    callbacks.push(cb)
    return () => {
      callbacks = callbacks.filter(c => c !== cb)
    }
  }

  onMounted(connect)
  onUnmounted(disconnect)

  return { connected, lastNotification, notifications, onNotification, connect, disconnect }
}
