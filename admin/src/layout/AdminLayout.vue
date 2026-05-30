<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '72px' : '240px'" class="sidebar">
      <div class="sidebar-inner">
        <!-- Logo -->
        <div class="logo-area">
          <div class="logo-icon">
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
              <circle cx="14" cy="14" r="14" fill="#5cc98d"/>
              <path d="M8 14c0-3.3 2.7-6 6-6s6 2.7 6 6-2.7 6-6 6-6-2.7-6-6z" fill="#1e4d35"/>
              <path d="M14 8v12M10 11l4 3 4-3" stroke="#e8f9ef" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </div>
          <transition name="fade">
            <div v-if="!isCollapsed" class="logo-text">
              <div class="logo-title">鲜小智</div>
              <div class="logo-sub">管理中心</div>
            </div>
          </transition>
        </div>

        <!-- 导航菜单 -->
        <el-menu
          :default-active="route.path"
          :collapse="isCollapsed"
          router
          class="nav-menu"
          background-color="transparent"
          text-color="rgba(232,249,239,0.7)"
          active-text-color="#fff"
        >
          <el-menu-item index="/">
            <el-icon><Odometer /></el-icon>
            <template #title>数据概览</template>
          </el-menu-item>
          <el-menu-item index="/category">
            <el-icon><Grid /></el-icon>
            <template #title>分类管理</template>
          </el-menu-item>
          <el-menu-item index="/product">
            <el-icon><Goods /></el-icon>
            <template #title>商品管理</template>
          </el-menu-item>
          <el-menu-item index="/order">
            <el-icon><List /></el-icon>
            <template #title>订单管理</template>
          </el-menu-item>
        </el-menu>

        <!-- 底部折叠按钮 -->
        <div class="collapse-btn" @click="isCollapsed = !isCollapsed">
          <el-icon :size="18">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
        </div>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="main-area">
      <el-header class="topbar">
        <div class="topbar-left">
          <h2 class="page-title">{{ route.meta.title }}</h2>
        </div>
        <div class="topbar-right">
          <div class="time-display">{{ currentTime }}</div>
          <el-dropdown @command="handleCommand">
            <div class="admin-avatar">
              <el-icon :size="18"><User /></el-icon>
              <span class="admin-name">{{ adminName }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="content-area">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Odometer, Grid, Goods, List, Fold, Expand, User, SwitchButton } from '@element-plus/icons-vue'
import { ElNotification } from 'element-plus'
import { connectWebSocket, disconnectWebSocket, onWebSocketMessage } from '../utils/websocket'

const route = useRoute()
const router = useRouter()
const isCollapsed = ref(false)
const currentTime = ref('')
const adminName = localStorage.getItem('admin_username') || '管理员'

let timer = null
let removeWsListener = null

function handleWsMessage(data) {
  if (data.type === 'order_paid') {
    ElNotification({
      title: '新订单提醒',
      message: `订单 ${data.orderNo} 已支付，金额 ¥${data.amount}`,
      type: 'success',
      duration: 5000
    })
    window.dispatchEvent(new CustomEvent('ws:order_paid', { detail: data }))
  }
}

function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleDateString('zh-CN', {
    month: 'long', day: 'numeric', weekday: 'long'
  }) + ' ' + now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 60000)
  connectWebSocket()
  removeWsListener = onWebSocketMessage(handleWsMessage)
})

onUnmounted(() => {
  clearInterval(timer)
  if (removeWsListener) removeWsListener()
  disconnectWebSocket()
})

function handleCommand(cmd) {
  if (cmd === 'logout') {
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_username')
    router.push('/login')
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

/* 侧边栏 */
.sidebar {
  background: linear-gradient(180deg, var(--green-900) 0%, #0f2b1e 100%);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.sidebar-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 0 0 20px;
}

/* Logo */
.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 20px 20px;
  margin-bottom: 8px;
}

.logo-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(92, 201, 141, 0.15);
  border-radius: 12px;
}

.logo-title {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
}

.logo-sub {
  font-size: 11px;
  color: rgba(232, 249, 239, 0.5);
  letter-spacing: 2px;
  text-transform: uppercase;
}

/* 导航菜单 */
.nav-menu {
  flex: 1;
  padding: 0 8px;
  border: none !important;
}

.nav-menu .el-menu-item {
  height: 46px;
  line-height: 46px;
  border-radius: var(--radius-md);
  margin-bottom: 4px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.nav-menu .el-menu-item:hover {
  background: rgba(92, 201, 141, 0.12) !important;
}

.nav-menu .el-menu-item.is-active {
  background: rgba(92, 201, 141, 0.2) !important;
  color: #fff !important;
  font-weight: 600;
}

.nav-menu .el-menu-item .el-icon {
  font-size: 18px;
  margin-right: 10px;
}

/* 折叠按钮 */
.collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  margin: 8px 16px 0;
  border-radius: var(--radius-md);
  color: rgba(232, 249, 239, 0.5);
  cursor: pointer;
  transition: all 0.2s;
}

.collapse-btn:hover {
  background: rgba(92, 201, 141, 0.1);
  color: rgba(232, 249, 239, 0.8);
}

/* 主内容区 */
.main-area {
  flex-direction: column;
}

/* 顶部栏 */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 32px;
  background: #fff;
  border-bottom: 1px solid var(--slate-200);
  box-shadow: var(--shadow-sm);
}

.page-title {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 600;
  color: var(--slate-900);
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.time-display {
  font-size: 13px;
  color: var(--slate-500);
}

.admin-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: var(--green-100);
  color: var(--green-700);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.admin-avatar:hover {
  background: var(--green-200);
}

.admin-name {
  font-size: 14px;
  font-weight: 500;
}

/* 内容区 */
.content-area {
  padding: 24px 32px;
  background: var(--slate-100);
  overflow-y: auto;
}

/* 页面过渡 */
.page-enter-active,
.page-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

/* 文字淡入 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
