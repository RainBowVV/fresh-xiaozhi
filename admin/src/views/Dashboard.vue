<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="item in stats" :key="item.label">
        <div class="stat-icon" :style="{ background: item.bg, color: item.color }">
          <el-icon :size="24"><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <el-card>
        <template #header>
          <div class="card-header">
            <span class="card-title">快捷操作</span>
          </div>
        </template>
        <div class="action-grid">
          <div class="action-item" @click="$router.push('/category')">
            <div class="action-icon" style="background: #e8f9ef; color: #236b45">
              <el-icon :size="20"><Grid /></el-icon>
            </div>
            <span>管理分类</span>
          </div>
          <div class="action-item" @click="$router.push('/product')">
            <div class="action-icon" style="background: #fef3c7; color: #92400e">
              <el-icon :size="20"><Goods /></el-icon>
            </div>
            <span>管理商品</span>
          </div>
          <div class="action-item" @click="$router.push('/order')">
            <div class="action-icon" style="background: #fee2e2; color: #991b1b">
              <el-icon :size="20"><List /></el-icon>
            </div>
            <span>处理订单</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Grid, Goods, List } from '@element-plus/icons-vue'
import { getCategoryList } from '../api/category'
import { getProductList } from '../api/product'
import { getOrderList } from '../api/order'

const stats = ref([
  { label: '商品数量', value: 0, icon: Goods, bg: '#e8f9ef', color: '#236b45' },
  { label: '分类数量', value: 0, icon: Grid, bg: '#fef3c7', color: '#92400e' },
  { label: '订单数量', value: 0, icon: List, bg: '#fee2e2', color: '#991b1b' }
])

onMounted(() => {
  getCategoryList({ pageNum: 1, pageSize: 1 }).then(data => { stats.value[1].value = data.total })
  getProductList({ pageNum: 1, pageSize: 1 }).then(data => { stats.value[0].value = data.total })
  getOrderList({ pageNum: 1, pageSize: 1 }).then(data => { stats.value[2].value = data.total })
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 统计卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 28px 24px;
  background: #fff;
  border-radius: var(--radius-lg);
  border: 1px solid var(--slate-200);
  box-shadow: var(--shadow-sm);
  transition: all 0.25s ease;
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.stat-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  flex-shrink: 0;
}

.stat-value {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 700;
  color: var(--slate-900);
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--slate-500);
  margin-top: 4px;
}

/* 快捷操作 */
.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px 16px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  font-weight: 500;
  color: var(--slate-700);
}

.action-item:hover {
  background: var(--slate-50);
}

.action-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 600;
  color: var(--slate-800);
}
</style>
