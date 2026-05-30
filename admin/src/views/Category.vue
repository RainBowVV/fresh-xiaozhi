<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">分类列表</span>
          <el-button type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon>新增分类
          </el-button>
        </div>
      </template>

      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="sort" label="排序权重" width="120" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="420px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序权重">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
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
import { Plus } from '@element-plus/icons-vue'
import { getCategoryList, saveCategory, deleteCategory } from '../api/category'

const list = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const form = ref({ id: null, name: '', sort: 0 })

function loadList() {
  loading.value = true
  getCategoryList({ pageNum: pageNum.value, pageSize: pageSize.value })
    .then(data => {
      list.value = data.records
      total.value = data.total
    })
    .finally(() => { loading.value = false })
}

function handleSizeChange() {
  pageNum.value = 1
  loadList()
}

function openDialog(row) {
  form.value = row ? { id: row.id, name: row.name, sort: row.sort ?? 0 } : { id: null, name: '', sort: 0 }
  dialogVisible.value = true
}

function handleSave() {
  if (!form.value.name.trim()) { ElMessage.warning('请输入分类名称'); return }
  saveCategory(form.value).then(() => {
    ElMessage.success(form.value.id ? '修改成功' : '新增成功')
    dialogVisible.value = false
    loadList()
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除分类「${row.name}」？`, '提示', { type: 'warning' }).then(() => {
    deleteCategory(row.id).then(() => { ElMessage.success('删除成功'); loadList() })
  }).catch(() => {})
}

onMounted(() => { loadList() })
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 24px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-family: var(--font-display); font-size: 16px; font-weight: 600; color: var(--slate-800); }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
