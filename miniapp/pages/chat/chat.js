const app = getApp()

function uuid() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

function md2html(text) {
  if (!text) return ''
  var s = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  var lines = s.split('\n')
  var html = []
  var inList = false
  var inTable = false
  var tableRows = []

  function flushTable() {
    if (!inTable) return
    if (tableRows.length > 0) {
      html.push('<table style="width:100%;border-collapse:collapse;margin:8px 0;font-size:24rpx">')
      for (var t = 0; t < tableRows.length; t++) {
        var tag = t === 0 ? 'th' : 'td'
        var bg = t === 0 ? 'background:#f0f7f2;' : (t % 2 === 0 ? 'background:#fafafa;' : '')
        html.push('<tr style="' + bg + '">')
        for (var c = 0; c < tableRows[t].length; c++) {
          html.push('<' + tag + ' style="border:1rpx solid #e0e0e0;padding:8rpx 12rpx;text-align:left">' + inline(tableRows[t][c]) + '</' + tag + '>')
        }
        html.push('</tr>')
      }
      html.push('</table>')
    }
    tableRows = []
    inTable = false
  }

  function inline(t) {
    return t.replace(/\*\*(.+?)\*\*/g, '<b>$1</b>')
  }

  for (var i = 0; i < lines.length; i++) {
    var line = lines[i]

    // 表格行
    if (/^\|(.+)\|$/.test(line.trim())) {
      var cells = line.trim().slice(1, -1).split('|').map(function(c) { return c.trim() })
      // 跳过分隔行 |---|---|
      if (/^[\s\-:|]+$/.test(line.trim())) continue
      if (!inTable) { if (inList) { html.push('</div>'); inList = false } inTable = true }
      tableRows.push(cells)
      continue
    }
    if (inTable) flushTable()

    if (!line.trim()) {
      if (inList) { html.push('</div>'); inList = false }
      html.push('<div style="height:10px"></div>')
      continue
    }
    if (/^### (.+)$/.test(line)) {
      if (inList) { html.push('</div>'); inList = false }
      html.push('<div style="font-weight:700;font-size:1.05em;margin:8px 0 4px">' + line.replace(/^### /, '') + '</div>')
      continue
    }
    if (/^## (.+)$/.test(line)) {
      if (inList) { html.push('</div>'); inList = false }
      html.push('<div style="font-weight:700;font-size:1.1em;margin:8px 0 4px">' + line.replace(/^## /, '') + '</div>')
      continue
    }
    var olMatch = line.match(/^(\d+)\. (.+)$/)
    if (olMatch) {
      if (!inList) { html.push('<div style="padding-left:4px;margin:6px 0">'); inList = true }
      html.push('<div style="margin:3px 0;display:flex;gap:8px"><span style="color:#2d6a4f;font-weight:600;min-width:28px">' + olMatch[1] + '.</span><span>' + inline(olMatch[2]) + '</span></div>')
      continue
    }
    var ulMatch = line.match(/^- (.+)$/)
    if (ulMatch) {
      if (!inList) { html.push('<div style="padding-left:4px;margin:6px 0">'); inList = true }
      html.push('<div style="margin:3px 0;display:flex;gap:8px"><span style="color:#2d6a4f">•</span><span>' + inline(ulMatch[1]) + '</span></div>')
      continue
    }
    if (inList) { html.push('</div>'); inList = false }
    html.push('<div style="margin:3px 0;line-height:1.7">' + inline(line) + '</div>')
  }
  if (inTable) flushTable()
  if (inList) html.push('</div>')
  return html.join('')
}

var msgId = 0

Page({
  data: {
    messages: [],
    inputValue: '',
    hasInput: false,
    sending: false,
    scrollToId: '',
    userAvatar: '/images/default-avatar.png',
    conversationId: ''
  },

  onLoad() {
    var userInfo = app.globalData.userInfo
    if (userInfo && userInfo.avatarUrl) {
      this.setData({ userAvatar: userInfo.avatarUrl })
    }
    this.setData({ conversationId: uuid() })
  },

  onUnload() {
    var baseUrl = app.globalData.baseUrl
    var token = app.globalData.token
    wx.request({
      url: baseUrl + '/ai/chat/clear',
      method: 'GET',
      data: { conversationId: this.data.conversationId },
      header: { 'Authorization': 'Bearer ' + token }
    })
  },

  onInput: function(e) {
    var value = e.detail.value
    this.setData({ inputValue: value, hasInput: value.trim().length > 0 })
  },

  sendMessage: function() {
    var that = this
    var content = this.data.inputValue.trim()
    if (!content || this.data.sending) return

    var userMsg = { id: 'msg-' + (++msgId), role: 'user', content: content }
    var thinkMsg = { id: 'msg-' + (++msgId), role: 'bot', content: '', htmlContent: '', thinking: true }

    this.setData({
      messages: this.data.messages.concat([userMsg, thinkMsg]),
      inputValue: '',
      hasInput: false,
      sending: true,
      scrollToId: 'msg-bottom'
    })

    var baseUrl = app.globalData.baseUrl
    var token = app.globalData.token
    var streamBuffer = ''
    var botId = thinkMsg.id

    var reqTask = wx.request({
      url: baseUrl + '/ai/chat/send',
      method: 'GET',
      data: { message: content, conversationId: that.data.conversationId },
      header: { 'Authorization': 'Bearer ' + token },
      enableChunked: true,
      timeout: 180000,
      success: function() {
        var clean = streamBuffer.replace(/\[ADD_CART:\d+:\d+\]/g, '').trim()
        var finalContent = clean || '抱歉，暂时无法回答。'
        var msgs = that.data.messages.map(function(m) {
          return m.id === botId ? { id: m.id, role: 'bot', content: finalContent, htmlContent: md2html(finalContent), thinking: false } : m
        })
        that.setData({ messages: msgs, sending: false })
      },
      fail: function() {
        var msgs = that.data.messages.map(function(m) {
          return m.id === botId ? { id: m.id, role: 'bot', content: '网络异常，请稍后再试。', htmlContent: '网络异常，请稍后再试。', thinking: false } : m
        })
        that.setData({ messages: msgs, sending: false })
      }
    })

    if (reqTask && reqTask.onChunkReceived) {
      reqTask.onChunkReceived(function(res) {
        var text = ab2str(res.data)
        streamBuffer += text
        var msgs = that.data.messages.map(function(m) {
          return m.id === botId ? { id: m.id, role: 'bot', content: streamBuffer, htmlContent: md2html(streamBuffer), thinking: false } : m
        })
        that.setData({ messages: msgs, scrollToId: 'msg-bottom' })
      })
    }
  }
})

function ab2str(buffer) {
  var bytes = new Uint8Array(buffer)
  var str = ''
  for (var i = 0; i < bytes.length; i++) {
    str += String.fromCharCode(bytes[i])
  }
  try { return decodeURIComponent(escape(str)) } catch (e) { return str }
}
