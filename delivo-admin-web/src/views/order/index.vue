<template>
  <div class="order-container">
    <div class="page-header">
      <h2>{{ t('order.title') }}</h2>
    </div>
    <div class="table-container">
      <div class="search-bar">
        <el-form :inline="true" class="search-form">
          <el-form-item :label="t('order.orderNumber')">
            <el-input v-model="searchForm.number" :placeholder="t('order.orderNumberPlaceholder')" clearable />
          </el-form-item>
          <el-form-item :label="t('order.phone')">
            <el-input v-model="searchForm.phone" :placeholder="t('order.phonePlaceholder')" clearable />
          </el-form-item>
          <el-form-item :label="t('order.status')">
            <el-select v-model="searchForm.status" :placeholder="t('order.statusPlaceholder')" clearable>
              <el-option :label="t('order.unpaid')" :value="1" />
              <el-option :label="t('order.waiting')" :value="2" />
              <el-option :label="t('order.accepted')" :value="3" />
              <el-option :label="t('order.delivering')" :value="4" />
              <el-option :label="t('order.completed')" :value="5" />
              <el-option :label="t('order.cancelled')" :value="6" />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('order.dateRange')">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              :range-separator="t('order.to')"
              :start-placeholder="t('order.startPlaceholder')"
              :end-placeholder="t('order.endPlaceholder')"
              value-format="YYYY-MM-DD"
              @change="handleDateChange"
            />
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
        :data="orderList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" :label="t('order.orderNumber')" width="180" />
        <el-table-column prop="phone" :label="t('order.phone')" width="120" />
        <el-table-column prop="address" :label="t('order.address')" show-overflow-tooltip />
        <el-table-column prop="amount" :label="t('order.amount')" width="100">
          <template #default="{ row }">
            ¥{{ row.amount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="t('common.status')" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderTime" :label="t('order.orderTime')" width="180" />
        <el-table-column :label="t('common.operation')" min-width="260" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button
                type="primary"
                plain
                size="small"
                @click="handleView(row)"
              >{{ t('order.viewDetails') }}</el-button>
              <el-button
                v-if="row.status === 2"
                type="success"
                plain
                size="small"
                @click="handleConfirm(row)"
              >{{ t('order.confirm') }}</el-button>
              <el-button
                v-if="row.status === 2"
                type="warning"
                plain
                size="small"
                @click="handleRejection(row)"
              >{{ t('order.rejection') }}</el-button>
              <el-button
                v-if="row.status === 3"
                type="primary"
                plain
                size="small"
                @click="handleDelivery(row)"
              >{{ t('order.delivery') }}</el-button>
              <el-button
                v-if="row.status === 4"
                type="success"
                plain
                size="small"
                @click="handleComplete(row)"
              >{{ t('order.complete') }}</el-button>
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
      :title="t('order.details')"
      v-model="detailVisible"
      width="800px"
    >
      <div v-if="orderDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item :label="t('order.orderNumber')">{{ orderDetail.id }}</el-descriptions-item>
          <el-descriptions-item :label="t('order.status')">
            <el-tag :type="getStatusType(orderDetail.status)">
              {{ getStatusText(orderDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('order.userName')">{{ orderDetail.consignee }}</el-descriptions-item>
          <el-descriptions-item :label="t('order.phone')">{{ orderDetail.phone }}</el-descriptions-item>
          <el-descriptions-item :label="t('order.address')" :span="2">{{ orderDetail.address }}</el-descriptions-item>
          <el-descriptions-item :label="t('order.amount')">¥{{ orderDetail.amount }}</el-descriptions-item>
          <el-descriptions-item :label="t('order.orderTime')">{{ orderDetail.orderTime }}</el-descriptions-item>
        </el-descriptions>
        <el-divider>{{ t('order.orderDishes') }}</el-divider>
        <el-table :data="orderDetail.orderDetailList" border>
          <el-table-column prop="name" :label="t('dish.name')" />
          <el-table-column prop="dishFlavor" :label="t('order.flavor')" />
          <el-table-column prop="number" :label="t('order.number')" width="80" />
          <el-table-column prop="amount" :label="t('order.amount')" width="100">
            <template #default="{ row }">
              ¥{{ row.amount }}
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailVisible = false">{{ t('common.close') }}</el-button>
        </div>
      </template>
    </el-dialog>

    
    <el-dialog
      :title="rejectionTitle"
      v-model="rejectionVisible"
      width="500px"
    >
      <el-form :model="rejectionForm" label-width="100px">
        <el-form-item :label="t('order.reason')">
          <el-input
            v-model="rejectionForm.rejectionReason"
            type="textarea"
            :rows="4"
            :placeholder="t('order.rejectReasonPlaceholder')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="rejectionVisible = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleRejectionSubmit">{{ t('common.confirm') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

import { ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOrderList,
  getOrderDetail,
  confirmOrder,
  rejectionOrder,
  deliveryOrder,
  completeOrder
} from '@/api/order'

const { t } = useI18n()

const loading = ref(false)
const orderList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dateRange = ref(null)
const detailVisible = ref(false)
const orderDetail = ref(null)
const rejectionVisible = ref(false)
const rejectionTitle = ref('')

const searchForm = ref({
  number: '',
  phone: '',
  status: null,
  beginTime: '',
  endTime: ''
})

const rejectionForm = ref({
  id: null,
  rejectionReason: ''
})

function fetchData(silent = false) {
  if (!silent) loading.value = true
  const params = {
    page: page.value,
    pageSize: pageSize.value,
    ...searchForm.value
  }
  getOrderList(params)
    .then(response => {
      orderList.value = response.data.records || []
      total.value = response.data.total || 0
      if (!silent) loading.value = false
    })
    .catch(() => {
      if (!silent) loading.value = false
    })
}

// 自动轮询：弹窗关闭且页面可见时每 10 秒静默刷新
let pollTimer = null

const isDialogOpen = () => detailVisible.value || rejectionVisible.value

function startPolling() {
  if (pollTimer) return
  pollTimer = setInterval(() => {
    if (!document.hidden && !isDialogOpen()) fetchData(true)
  }, 10000)
}

function stopPolling() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}

const onVisibilityChange = () => {
  if (document.hidden) stopPolling()
  else startPolling()
}

function handleView(row) {
  getOrderDetail(row.id)
    .then(response => {
      orderDetail.value = response.data
      detailVisible.value = true
    })
}

function handleConfirm(row) {
  ElMessageBox.confirm(t('order.confirmAccept'), t('common.tip'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
    .then(() => {
      confirmOrder({ id: row.id })
        .then(() => {
          ElMessage.success(t('common.success'))
          fetchData()
        })
    })
}

function handleRejection(row) {
  rejectionTitle.value = t('order.rejection')
  rejectionForm.value.id = row.id
  rejectionForm.value.rejectionReason = ''
  rejectionVisible.value = true
}

function handleRejectionSubmit() {
  if (!rejectionForm.value.rejectionReason) {
    ElMessage.warning(t('order.rejectReasonPlaceholder'))
    return
  }
  rejectionOrder(rejectionForm.value)
    .then(() => {
      ElMessage.success(t('common.success'))
      rejectionVisible.value = false
      fetchData()
    })
}

function handleDelivery(row) {
  ElMessageBox.confirm(t('order.confirmDelivery'), t('common.tip'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
    .then(() => {
      deliveryOrder(row.id)
        .then(() => {
          ElMessage.success(t('common.success'))
          fetchData()
        })
    })
}

function handleComplete(row) {
  ElMessageBox.confirm(t('order.confirmComplete'), t('common.tip'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
    .then(() => {
      completeOrder(row.id)
        .then(() => {
          ElMessage.success(t('common.success'))
          fetchData()
        })
    })
}

function handleCancel(row) {
  rejectionTitle.value = t('order.cancel')
  rejectionForm.value.id = row.id
  rejectionForm.value.rejectionReason = ''
  rejectionVisible.value = true
}

function handleDateChange(val) {
  if (val && val.length === 2) {
    searchForm.value.beginTime = val[0] + ' 00:00:00'
    searchForm.value.endTime = val[1] + ' 23:59:59'
  } else {
    searchForm.value.beginTime = ''
    searchForm.value.endTime = ''
  }
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function handleReset() {
  searchForm.value = {
    number: '',
    phone: '',
    status: null,
    beginTime: '',
    endTime: ''
  }
  dateRange.value = null
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

function getStatusText(status) {
  const statusMap = {
    1: t('order.unpaid'),
    2: t('order.waiting'),
    3: t('order.accepted'),
    4: t('order.delivering'),
    5: t('order.completed'),
    6: t('order.cancelled')
  }
  return statusMap[status] || 'Unknown'
}

function getStatusType(status) {
  const typeMap = {
    1: 'warning',
    2: 'info',
    3: 'primary',
    4: '',
    5: 'success',
    6: 'danger'
  }
  return typeMap[status] || ''
}

onMounted(() => {
  fetchData()
  startPolling()
  document.addEventListener('visibilitychange', onVisibilityChange)
})

onUnmounted(() => {
  stopPolling()
  document.removeEventListener('visibilitychange', onVisibilityChange)
})

</script>

<style scoped>

.order-container {
  padding: 20px;
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

.table-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.table-actions .el-button {
  margin-left: 0 !important;
  margin-right: 0 !important;
}

</style>
