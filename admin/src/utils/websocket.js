const WS_URL = `ws://${window.location.hostname}:8080/ws/admin`

let socket = null
let reconnectTimer = null
let isConnecting = false
const listeners = []

export function connectWebSocket() {
  if (socket && socket.readyState === WebSocket.OPEN) return
  if (isConnecting) return
  isConnecting = true

  socket = new WebSocket(WS_URL)

  socket.onopen = () => {
    isConnecting = false
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }

  socket.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      listeners.forEach(fn => fn(data))
    } catch (e) {
      console.error('WebSocket消息解析失败:', e)
    }
  }

  socket.onclose = () => {
    isConnecting = false
    socket = null
    reconnectTimer = setTimeout(connectWebSocket, 5000)
  }

  socket.onerror = () => {
    isConnecting = false
  }
}

export function onWebSocketMessage(fn) {
  listeners.push(fn)
  return () => {
    const idx = listeners.indexOf(fn)
    if (idx !== -1) listeners.splice(idx, 1)
  }
}

export function disconnectWebSocket() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  if (socket) {
    socket.close()
    socket = null
  }
}
