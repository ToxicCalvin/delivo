<template>
  <div class="report-container">
    <div class="page-header">
      <h2>{{ t('report.title') }}</h2>
    </div>
    <div class="filter-bar">
      <el-form :inline="true">
        <el-form-item :label="t('report.dateRange')">
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
          <el-button @click="handleExport">
            <el-icon><Download /></el-icon>
            {{ t('report.export') }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-row :gutter="20">
      
      <el-col :span="24">
        <div class="chart-container">
          <h3>{{ t('report.turnoverStatistics') }}</h3>
          <div ref="turnoverChartRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 20px">
      
      <el-col :span="12">
        <div class="chart-container">
          <h3>{{ t('report.userStatistics') }}</h3>
          <div ref="userChartRef" class="chart"></div>
        </div>
      </el-col>
      
      <el-col :span="12">
        <div class="chart-container">
          <h3>{{ t('report.orderStatistics') }}</h3>
          <div ref="orderChartRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 20px">
      
      <el-col :span="24">
        <div class="chart-container">
          <h3>{{ t('report.topSelling') }}</h3>
          <div ref="top10ChartRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>

import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  getTurnoverStatistics,
  getUserStatistics,
  getOrderStatistics,
  getSalesTop10,
  exportBusinessData
} from '@/api/report'

const { t } = useI18n()

const dateRange = ref([])
const beginDate = ref('')
const endDate = ref('')

const turnoverChartRef = ref()
const userChartRef = ref()
const orderChartRef = ref()
const top10ChartRef = ref()

let turnoverChart = null
let userChart = null
let orderChart = null
let top10Chart = null

function initDateRange() {
  const end = new Date()
  const start = new Date()
  start.setTime(start.getTime() - 3600 * 1000 * 24 * 30) // Default: last 30 days
  const startStr = formatDate(start)
  const endStr = formatDate(end)
  dateRange.value = [startStr, endStr]
  beginDate.value = startStr
  endDate.value = endStr
}

function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function initCharts() {
  turnoverChart = echarts.init(turnoverChartRef.value)
  userChart = echarts.init(userChartRef.value)
  orderChart = echarts.init(orderChartRef.value)
  top10Chart = echarts.init(top10ChartRef.value)
}

function handleDateChange(val) {
  if (val && val.length === 2) {
    beginDate.value = val[0]
    endDate.value = val[1]
  }
}

function handleSearch() {
  if (!beginDate.value || !endDate.value) {
    ElMessage.warning(t('report.selectDateRange'))
    return
  }
  fetchData()
}

function fetchData() {
  if (!beginDate.value || !endDate.value) {
    return
  }
  fetchTurnoverData()
  fetchUserData()
  fetchOrderData()
  fetchTop10Data()
}

function splitToArray(str) {
  if (!str) return []
  return str.split(',')
}

function splitToNumberArray(str) {
  if (!str) return []
  return str.split(',').map(Number)
}

function fetchTurnoverData() {
  getTurnoverStatistics(beginDate.value, endDate.value)
    .then(response => {
      const data = response.data
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          }
        },
        legend: {
          data: [t('report.turnoverStatistics')]
        },
        xAxis: {
          type: 'category',
          data: splitToArray(data.dateList)
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: '¥{value}'
          }
        },
        series: [
          {
            name: t('report.turnoverStatistics'),
            type: 'line',
            data: splitToNumberArray(data.turnoverList),
            smooth: true,
            itemStyle: {
              color: '#409EFF'
            }
          }
        ]
      }
      turnoverChart.setOption(option)
    })
}

function fetchUserData() {
  getUserStatistics(beginDate.value, endDate.value)
    .then(response => {
      const data = response.data
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: [t('report.newUser'), t('report.totalUser')]
        },
        xAxis: {
          type: 'category',
          data: splitToArray(data.dateList)
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: t('report.newUser'),
            type: 'bar',
            data: splitToNumberArray(data.newUserList),
            itemStyle: {
              color: '#67C23A'
            }
          },
          {
            name: t('report.totalUser'),
            type: 'line',
            data: splitToNumberArray(data.totalUserList),
            itemStyle: {
              color: '#E6A23C'
            }
          }
        ]
      }
      userChart.setOption(option)
    })
}

function fetchOrderData() {
  getOrderStatistics(beginDate.value, endDate.value)
    .then(response => {
      const data = response.data
      const option = {
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: t('report.orderStatistics'),
            type: 'pie',
            radius: '50%',
            data: [
              { value: data.validOrderCount || 0, name: t('order.completed') },
              { value: (data.totalOrderCount || 0) - (data.validOrderCount || 0), name: t('report.inProgress') }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
      orderChart.setOption(option)
    })
}

function fetchTop10Data() {
  getSalesTop10(beginDate.value, endDate.value)
    .then(response => {
      const data = response.data
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '20%'
        },
        xAxis: {
          type: 'value'
        },
        yAxis: {
          type: 'category',
          data: splitToArray(data.nameList).reverse()
        },
        series: [
          {
            name: t('report.sales'),
            type: 'bar',
            data: splitToNumberArray(data.numberList).reverse(),
            itemStyle: {
              color: '#F56C6C'
            }
          }
        ]
      }
      top10Chart.setOption(option)
    })
}

function handleExport() {
  exportBusinessData()
    .then(response => {
      const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `运营数据报表_${beginDate.value}_${endDate.value}.xlsx`
      link.click()
      window.URL.revokeObjectURL(url)
      ElMessage.success(t('common.success'))
    })
    .catch(() => {
      ElMessage.error(t('common.failed'))
    })
}

onMounted(() => {
  initDateRange()
  initCharts()
  fetchData()
})

onBeforeUnmount(() => {
  if (turnoverChart) turnoverChart.dispose()
  if (userChart) userChart.dispose()
  if (orderChart) orderChart.dispose()
  if (top10Chart) top10Chart.dispose()
})

</script>

<style scoped>

.report-container {
  padding: 20px;
}

.filter-bar {
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
}

.chart-container {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.chart-container h3 {
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: 500;
}

.chart {
  width: 100%;
  height: 400px;
}

</style>
