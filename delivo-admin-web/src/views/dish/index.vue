<template>
  <div class="dish-container">
    <div class="page-header">
      <h2>{{ t('dish.title') }}</h2>
    </div>
    <div class="table-container">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          {{ t('dish.add') }}
        </el-button>
        <el-button
          type="danger"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          {{ t('dish.batchDelete') }}
        </el-button>
      </div>
      <div class="search-bar">
        <el-form :inline="true" class="search-form">
          <el-form-item :label="t('dish.name')">
            <el-input v-model="searchForm.name" :placeholder="t('dish.namePlaceholder')" clearable />
          </el-form-item>
          <el-form-item :label="t('dish.category')">
            <el-select v-model="searchForm.categoryId" :placeholder="t('dish.categoryPlaceholder')" clearable>
              <el-option
                v-for="item in categoryList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('dish.status')">
            <el-select v-model="searchForm.status" :placeholder="t('dish.statusPlaceholder')" clearable>
              <el-option :label="t('dish.onSale')" :value="1" />
              <el-option :label="t('dish.discontinued')" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              {{ t('common.search') }}
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              {{ t('common.reset') }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table
        v-loading="loading"
        :data="dishList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" :label="t('dish.name')" width="150" />
        <el-table-column prop="categoryName" :label="t('dish.category')" width="120" />
        <el-table-column prop="price" :label="t('dish.price')" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="image" :label="t('dish.image')" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.image"
              :src="row.image"
              style="width: 60px; height: 60px"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="t('dish.description')" show-overflow-tooltip />
        <el-table-column prop="status" :label="t('dish.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? t('dish.onSale') : t('dish.discontinued') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('common.createTime')" width="180" />
        <el-table-column :label="t('common.operation')" width="280" fixed="right">
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
                {{ row.status === 1 ? t('dish.discontinued') : t('dish.onSale') }}
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
      <div class="pagination">
        <el-pagination
          :current-page="page"
          :page-sizes="[10, 20, 30, 50]"
          :page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form
        ref="dishFormRef"
        :model="dishForm"
        :rules="dishRules"
        label-width="100px"
      >
        <el-form-item :label="t('dish.name')" prop="name">
          <el-input v-model="dishForm.name" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item :label="t('dish.category')" prop="categoryId">
              <el-select v-model="dishForm.categoryId" :placeholder="t('dish.categoryPlaceholder')">
                <el-option
                  v-for="item in dishCategoryList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('dish.price')" prop="price">
              <el-input-number v-model="dishForm.price" :min="0" :precision="2" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="t('dish.image')" prop="image">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
          >
            <img v-if="dishForm.image" :src="dishForm.image" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item :label="t('dish.description')" prop="description">
          <el-input
            v-model="dishForm.description"
            type="textarea"
            :rows="3"
            :placeholder="t('dish.descriptionPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('dish.flavor')">
          <el-button type="primary" link @click="handleAddFlavor">
            <el-icon><Plus /></el-icon>
            {{ t('dish.addFlavor') }}
          </el-button>
          <el-table :data="dishForm.flavors" border style="width: 100%; margin-top: 10px">
            <el-table-column :label="t('dish.flavorName')" width="150">
              <template #default="{ row }">
                <el-input v-model="row.name" :placeholder="t('dish.flavorName')" />
              </template>
            </el-table-column>
            <el-table-column :label="t('dish.flavorValue')" width="200">
              <template #default="{ row }">
                <el-input v-model="row.value" :placeholder="t('dish.flavorValue')" />
              </template>
            </el-table-column>
            <el-table-column :label="t('common.operation')" width="100">
              <template #default="{ $index }">
                <el-button
                  type="danger"
                  link
                  @click="handleRemoveFlavor($index)"
                >{{ t('common.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
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
import { getDishList, addDish, updateDish, getDishById, enableOrDisableDish, deleteDishBatch } from '@/api/dish'
import { getCategoryByType } from '@/api/category'
import { uploadFile } from '@/api/common'

const { t } = useI18n()

const loading = ref(false)
const dishList = ref([])
const categoryList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedIds = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const dishFormRef = ref()

const searchForm = ref({
  name: '',
  categoryId: null,
  status: null
})

const dishForm = ref({
  id: null,
  name: '',
  categoryId: null,
  price: 0,
  image: '',
  description: '',
  status: 1,
  flavors: []
})

const dishCategoryList = computed(() => categoryList.value.filter(item => item.type === 1))

const dishRules = computed(() => ({
  name: [
    { required: true, message: t('dish.namePlaceholder'), trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: t('dish.categoryPlaceholder'), trigger: 'change' }
  ],
  price: [
    { required: true, message: t('dish.price'), trigger: 'blur' }
  ]
}))

function fetchCategoryList() {
  getCategoryByType(1).then(response => {
    categoryList.value = response.data || []
  })
}

function fetchData() {
  loading.value = true
  const params = {
    page: page.value,
    pageSize: pageSize.value,
    ...searchForm.value
  }
  getDishList(params)
    .then(response => {
      dishList.value = response.data.records || []
      total.value = response.data.total || 0
      loading.value = false
    })
    .catch(() => {
      loading.value = false
    })
}

function handleAdd() {
  dialogTitle.value = t('dish.add')
  isEdit.value = false
  dialogVisible.value = true
  resetForm()
}

function handleEdit(row) {
  dialogTitle.value = t('dish.edit')
  isEdit.value = true
  dialogVisible.value = true
  getDishById(row.id)
    .then(response => {
      const data = response.data
      dishForm.value = {
        id: data.id,
        name: data.name,
        categoryId: data.categoryId,
        price: data.price,
        image: data.image,
        description: data.description,
        status: data.status,
        flavors: data.flavors || []
      }
    })
}

function handleSubmit() {
  dishFormRef.value.validate(valid => {
    if (valid) {
      if (isEdit.value) {
        updateDish(dishForm.value)
          .then(() => {
            ElMessage.success(t('common.success'))
            dialogVisible.value = false
            fetchData()
          })
      } else {
        addDish(dishForm.value)
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
  const statusText = status === 1 ? t('dish.onSale') : t('dish.discontinued')
  ElMessageBox.confirm(t('employee.confirmStatusChange', { status: statusText }), t('common.tip'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
    .then(() => {
      enableOrDisableDish({
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
      deleteDishBatch([row.id])
        .then(() => {
          ElMessage.success(t('common.success'))
          fetchData()
        })
    })
}

function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning(t('dish.selectToBatchDelete'))
    return
  }
  ElMessageBox.confirm(t('dish.confirmBatchDelete'), t('common.tip'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
    .then(() => {
      deleteDishBatch(selectedIds.value)
        .then(() => {
          ElMessage.success(t('common.success'))
          selectedIds.value = []
          fetchData()
        })
    })
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function handleReset() {
  searchForm.value = {
    name: '',
    categoryId: null,
    status: null
  }
  page.value = 1
  fetchData()
}

function handleSizeChange(val) {
  pageSize.value = val
  page.value = 1
  fetchData()
}

function handleCurrentChange(val) {
  page.value = val
  fetchData()
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error(t('common.uploadImageOnly'))
    return false
  }
  if (!isLt2M) {
    ElMessage.error(t('common.uploadSizeLimit'))
    return false
  }
  return true
}

function handleUpload(options) {
  uploadFile(options.file)
    .then(response => {
      dishForm.value.image = response.data
      ElMessage.success(t('common.uploadSuccess'))
    })
    .catch(() => {
      ElMessage.error(t('common.uploadFailed'))
    })
}

function handleAddFlavor() {
  dishForm.value.flavors.push({
    name: '',
    value: ''
  })
}

function handleRemoveFlavor(index) {
  dishForm.value.flavors.splice(index, 1)
}

function handleDialogClose() {
  resetForm()
  dishFormRef.value && dishFormRef.value.resetFields()
}

function resetForm() {
  dishForm.value = {
    id: null,
    name: '',
    categoryId: null,
    price: 0,
    image: '',
    description: '',
    status: 1,
    flavors: []
  }
}

onMounted(() => {
  fetchCategoryList()
  fetchData()
})

</script>

<style scoped>

.dish-container {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
}

.search-form {
  margin: 0;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
}

.avatar-uploader:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
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
