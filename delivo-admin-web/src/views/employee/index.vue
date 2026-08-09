<template>
  <div class="employee-container">
    
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1>{{ t('employee.title') }}</h1>
          <p class="header-desc">{{ t('employee.desc') }}</p>
        </div>
        <div class="header-action">
          <el-button
            v-if="userStore.isAdmin"
            type="primary"
            class="add-button"
            @click="handleAdd"
          >
            <el-icon><Plus /></el-icon>
            {{ t('employee.add') }}
          </el-button>
        </div>
      </div>
    </div>

    
    <div class="content-card">
      
      <div class="toolbar">
        <div class="toolbar-left">
          <div class="stat-info">
            <span class="stat-label">{{ t('employee.total') }}</span>
            <span class="stat-value">{{ total }}</span>
            <span class="stat-label">{{ t('employee.person') }}</span>
          </div>
        </div>
        <div class="toolbar-right">
          <el-input
            v-model="searchKeyword"
            :placeholder="t('employee.usernamePlaceholder')"
            class="search-input"
            clearable
            style="width: 300px; margin-right: 12px;"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      
      <div class="table-wrapper">
        <el-table
          v-loading="loading"
          :data="employeeList"
          style="width: 100%"
          class="modern-table"
          :header-cell-style="{ background: '#f7fafc', color: '#2d3748', fontWeight: '600' }"
        >
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="username" :label="t('employee.username')" width="140">
            <template #default="{ row }">
              <div class="cell-content">
                <div class="cell-icon">
                  <el-icon><User /></el-icon>
                </div>
                <span>{{ row.username }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="name" :label="t('employee.name')" width="120" />
          <el-table-column prop="phone" :label="t('employee.phone')" width="150">
            <template #default="{ row }">
              <div class="cell-content">
                <el-icon><Phone /></el-icon>
                <span>{{ row.phone }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="idNumber" :label="t('employee.idNumber')" width="180" show-overflow-tooltip />
          <el-table-column prop="sex" :label="t('employee.sex')" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.sex === '1' ? 'primary' : 'danger'" size="small" effect="plain">
                {{ row.sex === '1' ? t('employee.male') : t('employee.female') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="t('employee.status')" width="120" align="center">
            <template #default="{ row }">
              <el-tag
                :type="row.status === 1 ? 'success' : 'danger'"
                size="small"
                effect="dark"
              >
                {{ row.status === 1 ? t('employee.enable') : t('employee.disable') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" :label="t('employee.createTime')" width="180" />
          <el-table-column :label="t('employee.operation')" width="240" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  v-if="userStore.isAdmin"
                  size="small"
                  type="primary"
                  link
                  class="action-btn edit-btn"
                  @click="handleEdit(row)"
                >{{ t('common.edit') }}</el-button>
                <el-button
                  v-if="userStore.isAdmin"
                  size="small"
                  :type="row.status === 1 ? 'danger' : 'success'"
                  link
                  :class="['action-btn', row.status === 1 ? 'disable-btn' : 'enable-btn']"
                  @click="handleStatusChange(row)"
                >
                  {{ row.status === 1 ? t('employee.disable') : t('employee.enable') }}
                </el-button>
                <span v-if="!userStore.isAdmin" class="permission-tip">{{ t('employee.noPermissionShort') }}</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      
      <div class="pagination-wrapper">
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
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="employeeFormRef"
        :model="employeeForm"
        :rules="employeeRules"
        label-width="100px"
      >
        <el-form-item :label="t('employee.username')" prop="username">
          <el-input v-model="employeeForm.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="t('employee.name')" prop="name">
          <el-input v-model="employeeForm.name" />
        </el-form-item>
        <el-form-item :label="t('employee.phone')" prop="phone">
          <el-input v-model="employeeForm.phone" />
        </el-form-item>
        <el-form-item :label="t('employee.idNumber')" prop="idNumber">
          <el-input v-model="employeeForm.idNumber" />
        </el-form-item>
        <el-form-item :label="t('employee.sex')" prop="sex">
          <el-radio-group v-model="employeeForm.sex">
            <el-radio label="1">{{ t('employee.male') }}</el-radio>
            <el-radio label="0">{{ t('employee.female') }}</el-radio>
          </el-radio-group>
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
import { getEmployeeList, addEmployee, updateEmployee, getEmployeeById, enableOrDisableEmployee } from '@/api/employee'
import { useUserStore } from '@/store/user'

const { t } = useI18n()
const userStore = useUserStore()

const loading = ref(false)
const employeeList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const employeeFormRef = ref()

const employeeForm = ref({
  id: null,
  username: '',
  name: '',
  phone: '',
  idNumber: '',
  sex: '1'
})

const employeeRules = computed(() => ({
  username: [
    { required: true, message: t('employee.usernameRequired'), trigger: 'blur' },
    { min: 3, max: 20, message: '3-20 characters', trigger: 'blur' }
  ],
  name: [
    { required: true, message: t('employee.nameRequired'), trigger: 'blur' }
  ],
  phone: [
    { required: true, message: t('employee.phoneRequired'), trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: t('employee.phoneInvalid'), trigger: 'blur' }
  ],
  idNumber: [
    { required: true, message: t('employee.idNumberRequired'), trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: t('employee.idNumberInvalid'), trigger: 'blur' }
  ]
}))

function hasEditPermission() {
  if (!userStore.isAdmin) {
    ElMessage.warning(t('employee.noPermission'))
    return false
  }
  return true
}

function fetchData() {
  loading.value = true
  const params = {
    page: page.value,
    pageSize: pageSize.value
  }
  if (searchKeyword.value) {
    params.name = searchKeyword.value
  }
  getEmployeeList(params)
    .then(response => {
      employeeList.value = response.data.records || []
      total.value = response.data.total || 0
      loading.value = false
    })
    .catch(() => {
      loading.value = false
    })
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function handleAdd() {
  if (!hasEditPermission()) return
  dialogTitle.value = t('employee.add')
  isEdit.value = false
  dialogVisible.value = true
  resetForm()
}

function handleEdit(row) {
  if (!hasEditPermission()) return
  dialogTitle.value = t('employee.edit')
  isEdit.value = true
  dialogVisible.value = true
  getEmployeeById(row.id)
    .then(response => {
      employeeForm.value = {
        id: response.data.id,
        username: response.data.username,
        name: response.data.name,
        phone: response.data.phone,
        idNumber: response.data.idNumber,
        sex: response.data.sex ? response.data.sex.toString() : '1'
      }
    })
}

function handleSubmit() {
  if (!hasEditPermission()) return
  employeeFormRef.value.validate(valid => {
    if (valid) {
      if (isEdit.value) {
        updateEmployee(employeeForm.value)
          .then(() => {
            ElMessage.success(t('common.success'))
            dialogVisible.value = false
            fetchData()
          })
      } else {
        addEmployee(employeeForm.value)
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
  if (!hasEditPermission()) return
  const status = row.status === 1 ? 0 : 1
  const statusText = status === 1 ? t('employee.enable') : t('employee.disable')
  ElMessageBox.confirm(t('employee.confirmStatusChange', { status: statusText }), t('common.tip'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
    .then(() => {
      enableOrDisableEmployee({
        id: row.id,
        status: status
      })
        .then(() => {
          ElMessage.success(t('common.success'))
          fetchData()
        })
    })
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

function handleDialogClose() {
  resetForm()
  employeeFormRef.value && employeeFormRef.value.resetFields()
}

function resetForm() {
  employeeForm.value = {
    id: null,
    username: '',
    name: '',
    phone: '',
    idNumber: '',
    sex: '1'
  }
}

onMounted(() => {
  fetchData()
})

</script>

<style lang="scss" scoped>

.employee-container {
  padding: 0;
}


.page-header {
  margin-bottom: 24px;

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
    padding: 24px 0;

    .header-left {
      h1 {
        font-size: 28px;
        font-weight: 700;
        color: #2d3748;
        margin: 0 0 8px 0;
        display: flex;
        align-items: center;
        gap: 12px;

        &::before {
          content: '';
          width: 4px;
          height: 28px;
          background: linear-gradient(180deg, #2db87a, #1a9c63);
          border-radius: 2px;
        }
      }

      .header-desc {
        color: #718096;
        font-size: 14px;
        margin: 0;
      }
    }

    .header-action {
      .add-button {
        height: 40px;
        padding: 0 24px;
        border-radius: 10px;
        font-weight: 600;
      }
    }
  }
}


.content-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #e8eaed;
}


.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f7fafc;

  .toolbar-left {
    .stat-info {
      display: flex;
      align-items: baseline;
      gap: 6px;

      .stat-label {
        font-size: 14px;
        color: #718096;
      }

      .stat-value {
        font-size: 24px;
        font-weight: 700;
        color: #2db87a;
      }
    }
  }

  .toolbar-right {
    display: flex;
    align-items: center;

    .search-input {
      :deep(.el-input__wrapper) {
        border-radius: 10px;
        border: 2px solid #e8eaed;
        box-shadow: none;
        transition: all 0.3s;

        &:focus-within {
          border-color: #2db87a;
          box-shadow: 0 0 0 3px rgba(45, 184, 122, 0.1);
        }
      }
    }
  }
}


.table-wrapper {
  margin-bottom: 20px;
}


.modern-table {
  :deep(.el-table__row) {
    transition: all 0.3s;

    &:hover {
      background: #f7fafc;
    }
  }

  .cell-content {
    display: flex;
    align-items: center;
    gap: 8px;

    .cell-icon {
      width: 32px;
      height: 32px;
      background: linear-gradient(135deg, #2db87a 0%, #1a9c63 100%);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 14px;
    }

    .el-icon {
      color: #2db87a;
      margin-right: 4px;
    }
  }

  .action-buttons {
    display: flex;
    gap: 15px;
    justify-content: center;
    align-items: center;

    .action-btn {
      padding: 4px 8px;
      border-radius: 6px;
      font-weight: 500;
      transition: all 0.3s;

      &.edit-btn {
        color: #2db87a;

        &:hover {
          background: rgba(45, 184, 122, 0.1);
          color: #2db87a;
        }
      }

      &.disable-btn {
        color: #f56565;

        &:hover {
          background: rgba(245, 101, 101, 0.1);
          color: #f56565;
        }
      }

      &.enable-btn {
        color: #48bb78;

        &:hover {
          background: rgba(72, 187, 120, 0.1);
          color: #48bb78;
        }
      }
    }

    .permission-tip {
      font-size: 12px;
      color: #a0aec0;
    }
  }
}


.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 2px solid #f7fafc;
}

</style>
