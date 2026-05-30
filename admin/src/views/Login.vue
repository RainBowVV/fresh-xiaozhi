<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-shape bg-shape-1"></div>
    <div class="bg-shape bg-shape-2"></div>
    <div class="bg-shape bg-shape-3"></div>

    <div class="login-container">
      <!-- 左侧品牌区 -->
      <div class="brand-side">
        <div class="brand-content">
          <div class="brand-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <circle cx="24" cy="24" r="24" fill="rgba(255,255,255,0.15)"/>
              <circle cx="24" cy="24" r="16" fill="rgba(255,255,255,0.25)"/>
              <path d="M16 24c0-4.4 3.6-8 8-8s8 3.6 8 8-3.6 8-8 8-8-3.6-8-8z" fill="#fff"/>
              <path d="M24 16v16M19 20l5 4 5-4" stroke="#1e4d35" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h1 class="brand-title">鲜小智</h1>
          <p class="brand-subtitle">生鲜超市管理中心</p>
          <div class="brand-features">
            <div class="feature-item">
              <el-icon><Goods /></el-icon>
              <span>商品与分类管理</span>
            </div>
            <div class="feature-item">
              <el-icon><List /></el-icon>
              <span>订单处理与跟踪</span>
            </div>
            <div class="feature-item">
              <el-icon><DataLine /></el-icon>
              <span>数据概览与统计</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="form-side">
        <div class="form-content">
          <div class="form-header">
            <h2 class="form-title">欢迎回来</h2>
            <p class="form-desc">请登录管理员账号</p>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleLogin">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名" size="large" prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" placeholder="密码" type="password" size="large" prefix-icon="Lock" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <span class="hint">默认账号：admin / admin123</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Goods, List, DataLine } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function handleLogin() {
  formRef.value.validate((valid) => {
    if (!valid) return
    loading.value = true

    axios.post('/api/admin/login', form).then(res => {
      if (res.data.code === 200) {
        localStorage.setItem('admin_token', res.data.data.token)
        localStorage.setItem('admin_username', res.data.data.username)
        ElMessage.success('登录成功')
        router.push('/')
      } else {
        ElMessage.error(res.data.message || '登录失败')
      }
    }).catch(() => {
      ElMessage.error('登录失败')
    }).finally(() => {
      loading.value = false
    })
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--green-900) 0%, #0f2b1e 50%, #1a3a2a 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.06;
  background: #fff;
}

.bg-shape-1 {
  width: 500px;
  height: 500px;
  top: -200px;
  right: -100px;
}

.bg-shape-2 {
  width: 300px;
  height: 300px;
  bottom: -100px;
  left: -50px;
}

.bg-shape-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 30%;
}

/* 登录容器 */
.login-container {
  display: flex;
  width: 860px;
  min-height: 520px;
  background: #fff;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
  z-index: 1;
}

/* 左侧品牌区 */
.brand-side {
  width: 360px;
  background: linear-gradient(160deg, var(--green-700) 0%, var(--green-900) 100%);
  padding: 48px 36px;
  display: flex;
  align-items: center;
}

.brand-content {
  width: 100%;
}

.brand-icon {
  margin-bottom: 24px;
}

.brand-title {
  font-family: var(--font-display);
  font-size: 36px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 8px;
}

.brand-subtitle {
  font-size: 15px;
  color: rgba(232, 249, 239, 0.6);
  margin-bottom: 40px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(232, 249, 239, 0.8);
  font-size: 14px;
}

.feature-item .el-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  font-size: 16px;
}

/* 右侧表单区 */
.form-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
}

.form-content {
  width: 100%;
  max-width: 340px;
}

.form-header {
  margin-bottom: 36px;
}

.form-title {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 700;
  color: var(--slate-900);
  margin-bottom: 8px;
}

.form-desc {
  font-size: 15px;
  color: var(--slate-500);
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-md);
  margin-top: 8px;
}

.form-footer {
  text-align: center;
  margin-top: 24px;
}

.hint {
  font-size: 13px;
  color: var(--slate-400);
}

/* Element Plus 输入框样式覆盖 */
:deep(.el-input__wrapper) {
  border-radius: var(--radius-md) !important;
  padding: 4px 12px !important;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style>
