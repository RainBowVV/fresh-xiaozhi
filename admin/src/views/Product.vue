<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="card-title">商品列表</span>
            <el-select v-model="categoryId" placeholder="全部分类" clearable @change="handleFilterChange" style="margin-left: 16px; width: 160px">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </div>
          <el-button type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon>新增商品
          </el-button>
        </div>
      </template>

      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image :src="row.image" style="width: 48px; height: 48px; border-radius: 8px" fit="cover" v-if="row.image" />
              <div class="product-placeholder" v-else>
                <el-icon :size="20" color="#94a3b8"><Goods /></el-icon>
              </div>
              <span class="product-name">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
          @current-change="loadList"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="560px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="所属分类">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
            accept="image/*"
          >
            <el-image v-if="form.image" :src="form.image" fit="cover" class="uploaded-image" />
            <div v-else class="upload-placeholder">
              <el-icon :size="28"><Plus /></el-icon>
              <span>点击上传</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="价格">
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位">
              <el-input v-model="form.unit" placeholder="斤/个/份" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="商品描述信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Goods } from '@element-plus/icons-vue'
import { getCategoryList } from '../api/category'
import { getProductList, saveProduct, deleteProduct, updateProductStatus } from '../api/product'
import { uploadImage } from '../api/upload'

const list = ref([])
const categories = ref([])
const categoryId = ref(null)
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const form = ref({ id: null, categoryId: null, name: '', image: '', price: 0, unit: '', description: '', status: 1 })

function loadCategories() {
  getCategoryList({ pageNum: 1, pageSize: 999 }).then(data => { categories.value = data.records })
}

function loadList() {
  loading.value = true
  const params = { pageNum: pageNum.value, pageSize: pageSize.value }
  if (categoryId.value) params.categoryId = categoryId.value
  getProductList(params)
    .then(data => {
      list.value = data.records
      total.value = data.total
    })
    .finally(() => { loading.value = false })
}

function handleFilterChange() {
  pageNum.value = 1
  loadList()
}

function handleSizeChange() {
  pageNum.value = 1
  loadList()
}

function openDialog(row) {
  form.value = row ? { ...row } : { id: null, categoryId: null, name: '', image: '', price: 0, unit: '', description: '', status: 1 }
  dialogVisible.value = true
}

function beforeUpload(file) {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) { ElMessage.error('仅支持 jpg/png/gif/webp 格式'); return false }
  if (!isLt5M) { ElMessage.error('图片大小不能超过5MB'); return false }
  return true
}

function handleUpload({ file }) {
  uploadImage(file).then(data => {
    form.value.image = data.url
    ElMessage.success('上传成功')
  })
}

function handleSave() {
  if (!form.value.name.trim() || !form.value.categoryId) { ElMessage.warning('请填写完整信息'); return }
  saveProduct(form.value).then(() => {
    ElMessage.success(form.value.id ? '修改成功' : '新增成功')
    dialogVisible.value = false
    loadList()
  })
}

function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  ElMessageBox.confirm(`确定${action}「${row.name}」？`, '提示', { type: 'warning' }).then(() => {
    updateProductStatus(row.id, newStatus).then(() => { ElMessage.success('操作成功'); loadList() })
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除「${row.name}」？`, '提示', { type: 'warning' }).then(() => {
    deleteProduct(row.id).then(() => { ElMessage.success('删除成功'); loadList() })
  }).catch(() => {})
}

onMounted(() => { loadCategories(); loadList() })
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 24px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.header-left { display: flex; align-items: center; }
.card-title { font-family: var(--font-display); font-size: 16px; font-weight: 600; color: var(--slate-800); }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
.product-cell { display: flex; align-items: center; gap: 12px; }
.product-placeholder { width: 48px; height: 48px; display: flex; align-items: center; justify-content: center; background: var(--slate-100); border-radius: 8px; }
.product-name { font-weight: 500; color: var(--slate-800); }
.price-text { font-family: var(--font-display); font-weight: 600; color: #e64340; }
.image-uploader :deep(.el-upload) { border: 1px dashed var(--slate-300); border-radius: 8px; cursor: pointer; overflow: hidden; transition: border-color 0.2s; }
.image-uploader :deep(.el-upload:hover) { border-color: var(--green-400); }
.uploaded-image { width: 148px; height: 148px; display: block; }
.upload-placeholder { width: 148px; height: 148px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; color: var(--slate-400); font-size: 13px; }
</style>
