<template>
  <div class="dashboard">
    
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1>{{ t('dashboard.title') }}</h1>
          <p class="header-desc">{{ t('dashboard.desc') }}</p>
        </div>
        <div class="header-right">
          <div class="time-info">
            <el-icon><Clock /></el-icon>
            <span>{{ currentTime }}</span>
          </div>
        </div>
      </div>
    </div>

    
    <el-row :gutter="24" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card stat-card-1">
          <div class="stat-icon">
            <el-icon><Coin /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">{{ t('dashboard.todayTurnover') }}</div>
            <div class="stat-value">¥{{ formatNumber(businessData.turnover || 0) }}</div>
            <div class="stat-trend">
              <el-icon><Top /></el-icon>
              <span>{{ t('dashboard.comparedYesterday') }}</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card stat-card-2">
          <div class="stat-icon">
            <el-icon><List /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">{{ t('dashboard.todayOrders') }}</div>
            <div class="stat-value">{{ formatNumber(businessData.validOrderCount || 0) }}</div>
            <div class="stat-trend">
              <el-icon><Top /></el-icon>
              <span>{{ t('dashboard.comparedYesterday') }}</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card stat-card-3">
          <div class="stat-icon">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">{{ t('dashboard.todayUsers') }}</div>
            <div class="stat-value">{{ formatNumber(businessData.newUsers || 0) }}</div>
            <div class="stat-trend">
              <el-icon><Top /></el-icon>
              <span>{{ t('dashboard.comparedYesterday') }}</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card stat-card-4">
          <div class="stat-icon">
            <el-icon><SuccessFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">{{ t('dashboard.completionRate') }}</div>
            <div class="stat-value">{{ businessData.orderCompletionRate || 0 }}%</div>
            <div class="stat-trend">
              <el-icon><Top /></el-icon>
              <span>{{ t('dashboard.comparedYesterday') }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    
    <el-row :gutter="24" class="overview-row">
      <el-col :xs="24" :sm="24" :md="8">
        <div class="overview-card order-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon><List /></el-icon>
              <span>{{ t('layout.order') }}</span>
            </div>
            <el-tag size="small" type="info">{{ t('dashboard.realTime') }}</el-tag>
          </div>
          <div class="card-body">
            <div class="overview-item">
              <div class="item-left">
                <div class="item-icon item-icon-warning">
                  <el-icon><Clock /></el-icon>
                </div>
                <div class="item-info">
                  <div class="item-label">{{ t('dashboard.waitingOrders') }}</div>
                  <div class="item-desc">{{ t('dashboard.waitingOrdersDesc') }}</div>
                </div>
              </div>
              <div class="item-value">
                <el-tag type="warning" size="default">{{ orderOverView.waitingOrders || 0 }}</el-tag>
              </div>
            </div>
            <div class="overview-item">
              <div class="item-left">
                <div class="item-icon item-icon-primary">
                  <el-icon><Van /></el-icon>
                </div>
                <div class="item-info">
                  <div class="item-label">{{ t('dashboard.deliveredOrders') }}</div>
                  <div class="item-desc">{{ t('dashboard.deliveredOrdersDesc') }}</div>
                </div>
              </div>
              <div class="item-value">
                <el-tag type="primary" size="default">{{ orderOverView.deliveredOrders || 0 }}</el-tag>
              </div>
            </div>
            <div class="overview-item">
              <div class="item-left">
                <div class="item-icon item-icon-success">
                  <el-icon><Check /></el-icon>
                </div>
                <div class="item-info">
                  <div class="item-label">{{ t('dashboard.completedOrders') }}</div>
                  <div class="item-desc">{{ t('dashboard.completedOrdersDesc') }}</div>
                </div>
              </div>
              <div class="item-value">
                <el-tag type="success" size="default">{{ orderOverView.completedOrders || 0 }}</el-tag>
              </div>
            </div>
            <div class="overview-item">
              <div class="item-left">
                <div class="item-icon item-icon-danger">
                  <el-icon><Close /></el-icon>
                </div>
                <div class="item-info">
                  <div class="item-label">{{ t('dashboard.cancelledOrders') }}</div>
                  <div class="item-desc">{{ t('dashboard.cancelledOrdersDesc') }}</div>
                </div>
              </div>
              <div class="item-value">
                <el-tag type="danger" size="default">{{ orderOverView.cancelledOrders || 0 }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <div class="overview-card dish-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon><Food /></el-icon>
              <span>{{ t('dashboard.dishOverview') }}</span>
            </div>
            <el-tag size="small" type="success">{{ t('dashboard.normal') }}</el-tag>
          </div>
          <div class="card-body">
            <div class="overview-item">
              <div class="item-left">
                <div class="item-icon item-icon-success">
                  <el-icon><Check /></el-icon>
                </div>
                <div class="item-info">
                  <div class="item-label">{{ t('dashboard.sold') }}</div>
                  <div class="item-desc">{{ t('dashboard.soldDishDesc') }}</div>
                </div>
              </div>
              <div class="item-value">
                <el-tag type="success" size="default">{{ dishOverView.sold || 0 }}</el-tag>
              </div>
            </div>
            <div class="overview-item">
              <div class="item-left">
                <div class="item-icon item-icon-danger">
                  <el-icon><Remove /></el-icon>
                </div>
                <div class="item-info">
                  <div class="item-label">{{ t('dashboard.discontinued') }}</div>
                  <div class="item-desc">{{ t('dashboard.discontinuedDishDesc') }}</div>
                </div>
              </div>
              <div class="item-value">
                <el-tag type="danger" size="default">{{ dishOverView.discontinued || 0 }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <div class="overview-card setmeal-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon><Goods /></el-icon>
              <span>{{ t('dashboard.setmealOverview') }}</span>
            </div>
            <el-tag size="small" type="success">{{ t('dashboard.normal') }}</el-tag>
          </div>
          <div class="card-body">
            <div class="overview-item">
              <div class="item-left">
                <div class="item-icon item-icon-success">
                  <el-icon><Check /></el-icon>
                </div>
                <div class="item-info">
                  <div class="item-label">{{ t('dashboard.sold') }}</div>
                  <div class="item-desc">{{ t('dashboard.soldSetmealDesc') }}</div>
                </div>
              </div>
              <div class="item-value">
                <el-tag type="success" size="default">{{ setmealOverView.sold || 0 }}</el-tag>
              </div>
            </div>
            <div class="overview-item">
              <div class="item-left">
                <div class="item-icon item-icon-danger">
                  <el-icon><Remove /></el-icon>
                </div>
                <div class="item-info">
                  <div class="item-label">{{ t('dashboard.discontinued') }}</div>
                  <div class="item-desc">{{ t('dashboard.discontinuedSetmealDesc') }}</div>
                </div>
              </div>
              <div class="item-value">
                <el-tag type="danger" size="default">{{ setmealOverView.discontinued || 0 }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>

import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { getBusinessData, getOrderOverView, getDishOverView, getSetmealOverView } from '@/api/workspace'

const { t } = useI18n()

const businessData = ref({})
const orderOverView = ref({})
const dishOverView = ref({})
const setmealOverView = ref({})
const currentTime = ref('')

let timer = null

function fetchData() {
  getBusinessData().then(response => {
    businessData.value = response.data || {}
  })
  getOrderOverView().then(response => {
    orderOverView.value = response.data || {}
  })
  getDishOverView().then(response => {
    dishOverView.value = response.data || {}
  })
  getSetmealOverView().then(response => {
    setmealOverView.value = response.data || {}
  })
}

function updateTime() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

function formatNumber(num) {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

onMounted(() => {
  fetchData()
  updateTime()
  timer = setInterval(() => {
    updateTime()
  }, 1000)
})

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})

</script>

<style lang="scss" scoped>

.dashboard {
  padding: 0;
}


.page-header {
  margin-bottom: 24px;

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
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

    .header-right {
      .time-info {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 10px 20px;
        background: linear-gradient(135deg, #2db87a 0%, #1a9c63 100%);
        border-radius: 25px;
        color: #fff;
        font-size: 14px;
        box-shadow: 0 4px 12px rgba(45, 184, 122, 0.3);

        .el-icon {
          font-size: 16px;
        }
      }
    }
  }
}

// 统计卡片行
.stats-row {
  margin-bottom: 24px;
}

// 统计卡片
.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #e8eaed;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #2db87a, #1a9c63);
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  .stat-icon {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: #fff;
    flex-shrink: 0;

    .el-icon {
      font-size: 28px;
    }
  }

  &.stat-card-1 .stat-icon {
    background: linear-gradient(135deg, #2db87a 0%, #1a9c63 100%);
  }

  &.stat-card-2 .stat-icon {
    background: linear-gradient(135deg, #f6ad55 0%, #ed8936 100%);
  }

  &.stat-card-3 .stat-icon {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }

  &.stat-card-4 .stat-icon {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  }

  .stat-content {
    flex: 1;

    .stat-label {
      font-size: 14px;
      color: #718096;
      margin-bottom: 8px;
      font-weight: 500;
    }

    .stat-value {
      font-size: 32px;
      font-weight: 700;
      color: #2d3748;
      margin-bottom: 8px;
      line-height: 1.2;
    }

    .stat-trend {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: #48bb78;

      .el-icon {
        font-size: 12px;
      }
    }
  }
}


.overview-row {
  margin-bottom: 24px;
}


.overview-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #e8eaed;
  transition: all 0.3s ease;
  height: 100%;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 2px solid #f7fafc;

    .card-title {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 18px;
      font-weight: 600;
      color: #2d3748;

      .el-icon {
        font-size: 20px;
        color: #2db87a;
      }
    }
  }

  .card-body {
    .overview-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 0;
      border-bottom: 1px solid #f7fafc;

      &:last-child {
        border-bottom: none;
      }

      .item-left {
        display: flex;
        align-items: center;
        gap: 12px;
        flex: 1;

        .item-icon {
          width: 40px;
          height: 40px;
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 18px;
          color: #fff;

          .el-icon {
            font-size: 18px;
          }

          &.item-icon-warning {
            background: linear-gradient(135deg, #f6ad55, #ed8936);
          }

          &.item-icon-primary {
            background: linear-gradient(135deg, #2db87a, #1a9c63);
          }

          &.item-icon-success {
            background: linear-gradient(135deg, #48bb78, #38a169);
          }

          &.item-icon-danger {
            background: linear-gradient(135deg, #f56565, #e53e3e);
          }
        }

        .item-info {
          .item-label {
            font-size: 15px;
            font-weight: 600;
            color: #2d3748;
            margin-bottom: 4px;
          }

          .item-desc {
            font-size: 12px;
            color: #a0aec0;
          }
        }
      }

      .item-value {
        flex-shrink: 0;
      }
    }
  }
}


@media (max-width: 768px) {
  .page-header .header-content {
    flex-direction: column;
    gap: 16px;
  }

  .stat-card {
    flex-direction: column;
    text-align: center;

    .stat-icon {
      margin-bottom: 12px;
    }
  }
}

</style>
