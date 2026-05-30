<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="card-title">订单列表</span>
            <el-select v-model="statusFilter" placeholder="全部状态" clearable @change="handleFilterChange" style="margin-left: 16px; width: 140px">
              <el-option v-for="(label, key) in statusMap" :key="key" :label="label" :value="Number(key)" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="orders" stripe v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="180">
          <template #default="{ row }">
            <span class="order-no">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="110">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :color="tagColor(row.status)" size="small" style="border:none;color:#fff">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" min-width="160" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
            <el-dropdown v-if="row.status >= 1 && row.status <= 3" @command="(cmd) => changeStatus(row, cmd)" style="margin-left: 12px">
              <el-button link type="primary">
                修改状态 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="row.status === 1" :command="2">备货中</el-dropdown-item>
                  <el-dropdown-item v-if="row.status === 2" :command="3">配送中</el-dropdown-item>
                  <el-dropdown-item v-if="row.status === 3" :command="4">已完成</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadOrders"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="640px" destroy-on-close>
      <div class="detail-header">
        <div class="detail-status" :class="'status-' + detail.status">
          {{ statusMap[detail.status] }}
        </div>
        <div class="detail-no">订单号：{{ detail.orderNo }}</div>
      </div>

      <el-descriptions :column="2" border class="detail-info">
        <el-descriptions-item label="下单时间">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ detail.payTime || '未支付' }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">
          <span class="price-text">¥{{ detail.totalAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <div class="detail-section" v-if="detail.address">
        <div class="section-title">收货信息</div>
        <div class="address-info">
          <span class="addr-name">{{ detail.address.name }}</span>
          <span class="addr-phone">{{ detail.address.phone }}</span>
          <div class="addr-detail">{{ detail.address.province }}{{ detail.address.city }}{{ detail.address.district }}{{ detail.address.detail }}</div>
        </div>
      </div>

      <div class="detail-section">
        <div class="section-title">商品明细</div>
        <el-table :data="detail.items || []" stripe size="small">
          <el-table-column prop="productName" label="商品" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="100">
            <template #default="{ row }">
              <span class="price-text">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { getOrderList, getOrderDetail, updateOrderStatus } from '../api/order'

function onOrderPaid() { loadOrders() }

onMounted(() => {
  loadOrders()
  window.addEventListener('ws:order_paid', onOrderPaid)
})

onUnmounted(() => {
  window.removeEventListener('ws:order_paid', onOrderPaid)
})

const statusMap = { 0: '待付款', 1: '已付款', 2: '备货中', 3: '配送中', 4: '已完成', 5: '已取消' }

const orders = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref(null)
const detailVisible = ref(false)
const detail = ref({})

function tagColor(status) {
  const map = {
    0: '#e6a23c',  // 待付款 - 橙色
    1: '#67c23a',  // 已付款 - 绿色
    2: '#409eff',  // 备货中 - 蓝色
    3: '#e040fb',  // 配送中 - 紫色
    4: '#909399',  // 已完成 - 灰色
    5: '#f56c6c'   // 已取消 - 红色
  }
  return map[status] || '#909399'
}

function loadOrders() {
  loading.value = true
  const params = { pageNum: pageNum.value, pageSize: pageSize.value }
  if (statusFilter.value !== null && statusFilter.value !== '') {
    params.status = statusFilter.value
  }
  getOrderList(params)
    .then(data => {
      orders.value = data.records
      total.value = data.total
    })
    .finally(() => { loading.value = false })
}

function handleFilterChange() {
  pageNum.value = 1
  loadOrders()
}

function handleSizeChange() {
  pageNum.value = 1
  loadOrders()
}

function showDetail(row) {
  getOrderDetail(row.id).then(data => { detail.value = data; detailVisible.value = true })
}

function changeStatus(row, newStatus) {
  updateOrderStatus(row.id, newStatus).then(() => {
    ElMessage.success('状态已更新')
    loadOrders()
  })
}

</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 24px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.header-left { display: flex; align-items: center; }
.card-title { font-family: var(--font-display); font-size: 16px; font-weight: 600; color: var(--slate-800); }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
.order-no { font-family: var(--font-display); font-size: 13px; color: var(--slate-500); }
.price-text { font-family: var(--font-display); font-weight: 600; color: #e64340; }

/* 订单详情 */
.detail-header { margin-bottom: 20px; }
.detail-status { display: inline-block; padding: 4px 16px; border-radius: 20px; font-size: 14px; font-weight: 600; margin-bottom: 8px; }
.status-0 { background: var(--amber-100); color: #92400e; }
.status-1 { background: var(--green-100); color: var(--green-800); }
.status-2 { background: #dbeafe; color: #1e40af; }
.status-3 { background: #fce7f3; color: #9d174d; }
.status-4 { background: var(--slate-100); color: var(--slate-600); }
.status-5 { background: var(--slate-100); color: var(--slate-600); }
.detail-no { font-size: 13px; color: var(--slate-500); }
.detail-info { margin-bottom: 20px; }
.detail-section { margin-bottom: 16px; }
.section-title { font-family: var(--font-display); font-size: 14px; font-weight: 600; color: var(--slate-800); margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px solid var(--slate-100); }
.address-info { padding: 12px 16px; background: var(--slate-50); border-radius: var(--radius-md); }
.addr-name { font-weight: 600; color: var(--slate-800); margin-right: 12px; }
.addr-phone { color: var(--slate-500); }
.addr-detail { margin-top: 6px; font-size: 14px; color: var(--slate-600); }
</style>
