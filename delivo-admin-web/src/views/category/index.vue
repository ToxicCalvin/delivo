<template>
  <div class="category-container">
    <div class="page-header">
      <h2>{{ t('category.title') }}</h2>
    </div>
    <div class="table-container">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          {{ t('category.add') }}
        </el-button>
      </div>
      <el-table
        v-loading="loading"
        :data="categoryList"
        border
        style="width: 100%"
        row-key="id"
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="name" :label="t('category.name')" width="200" />
        <el-table-column prop="type" :label="t('category.type')" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'success' : 'info'">
              {{ row.type === 1 ? t('category.dishCategory') : t('category.setmealCategory') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" :label="t('category.sort')" width="100" />
        <el-table-column prop="status" :label="t('category.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? t('common.enable') : t('common.disable') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('category.createTime')" width="180" />
        <el-table-column :label="t('common.operation')" width="240" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button
                type="primary"
                link
                size="small"
                @click="handleEdit(row)"
              >{{ t('common.edit') }}</el-button>
              <el-button
                type="primary"
                link
                size="small"
                @click="handleStatusChange(row)"
              >
                {{ row.status === 1 ? t('common.disable') : t('common.enable') }}
              </el-button>
              <el-button
                type="danger"
                link
                size="small"
                @click="handleDelete(row)"
              >{{ t('common.delete') }}</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="categoryRules"
        label-width="100px"
      >
        <el-form-item :label="t('category.name')" prop="name">
          <el-input v-model="categoryForm.name" />
        </el-form-item>
        <el-form-item :label="t('category.type')" prop="type">
          <el-radio-group v-model="categoryForm.type">
            <el-radio :label="1">{{ t('category.dishCategory') }}</el-radio>
            <el-radio :label="2">{{ t('category.setmealCategory') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('category.sort')" prop="sort">
          <el-input-number v-model="categoryForm.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleSubmit">{{ t('common.confirm') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCategoryList,
  addCategory,
  updateCategory,
  enableOrDisableCategory,
  deleteCategory
} from '@/api/category'

const { t } = useI18n()

const loading = ref(false)
const categoryList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const categoryFormRef = ref()

const categoryForm = ref({
  id: null,
  type: 1,
  name: '',
  sort: 0
})

const categoryRules = computed(() => ({
  name: [
    { required: true, message: t('category.namePlaceholder'), trigger: 'blur' }
  ],
  type: [
    { required: true, message: t('category.typePlaceholder'), trigger: 'change' }
  ]
}))

function fetchData() {
  loading.value = true
  getCategoryList({
    page: 1,
    pageSize: 1000
  })
    .then(response => {
      categoryList.value = response.data.records || []
      loading.value = false
    })
    .catch(() => {
      loading.value = false
    })
}

function handleAdd() {
  dialogTitle.value = t('category.add')
  isEdit.value = false
  dialogVisible.value = true
  resetForm()
}

function handleEdit(row) {
  dialogTitle.value = t('category.edit')
  isEdit.value = true
  dialogVisible.value = true
  categoryForm.value = {
    id: row.id,
    type: row.type,
    name: row.name,
    sort: row.sort
  }
}

function handleSubmit() {
  categoryFormRef.value.validate(valid => {
    if (valid) {
      if (isEdit.value) {
        updateCategory(categoryForm.value)
          .then(() => {
            ElMessage.success(t('common.success'))
            dialogVisible.value = false
            fetchData()
          })
      } else {
        addCategory(categoryForm.value)
          .then(() => {
            ElMessage.success(t('common.success'))
            dialogVisible.value = false
            fetchData()
          })
      }
    }
  })
}

function handleStatusChange(row) {
  const status = row.status === 1 ? 0 : 1
  const statusText = status === 1 ? t('common.enable') : t('common.disable')
  ElMessageBox.confirm(t('employee.confirmStatusChange', { status: statusText }), t('common.tip'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
    .then(() => {
      enableOrDisableCategory({
        id: row.id,
        status: status
      })
        .then(() => {
          ElMessage.success(t('common.success'))
          fetchData()
        })
    })
}

function handleDelete(row) {
  ElMessageBox.confirm(t('category.confirmDelete'), t('common.tip'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
    .then(() => {
      deleteCategory(row.id)
        .then(() => {
          ElMessage.success(t('common.success'))
          fetchData()
        })
    })
}

function handleDialogClose() {
  resetForm()
  categoryFormRef.value && categoryFormRef.value.resetFields()
}

function resetForm() {
  categoryForm.value = {
    id: null,
    type: 1,
    name: '',
    sort: 0
  }
}

onMounted(() => {
  fetchData()
})

</script>

<style scoped>

.category-container {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.table-actions .el-button {
  margin-left: 0 !important;
  display: flex;
  align-items: center;
}

</style>
