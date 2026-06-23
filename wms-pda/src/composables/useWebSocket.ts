/**
 * WebSocket STOMP 客户端 Composable
 * 实时接收任务推送和库存变更通知
 *
 * 注意: UniApp 原生不支持 STOMP-over-WebSocket 库，
 * 此处使用 UniApp 原生 WebSocket API 直连 STOMP 端点，
 * 或通过简易 WebSocket 订阅 /topic/tasks
 */
import { ref, onMounted, onUnmounted } from 'vue'

const WS_URL = 'ws://localhost:8080/ws-stomp'

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
  let callbacks: Array<(n: TaskNotification) => void> = []

  function connect() {
    if (socketTask) return

    socketTask = uni.connectSocket({
      url: WS_URL,
      success: () => { connected.value = true }
    })

    socketTask.onOpen(() => {
      connected.value = true
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
      connected.value = false
      socketTask = null
      // 3秒后自动重连
      reconnectTimer = setTimeout(connect, 3000)
    })

    socketTask.onError(() => {
      connected.value = false
      socketTask?.close()
    })
  }

  function disconnect() {
    if (reconnectTimer) clearTimeout(reconnectTimer)
    socketTask?.close({ code: 1000, reason: 'user disconnect' })
    socketTask = null
    connected.value = false
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
