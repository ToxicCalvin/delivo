<script setup lang="ts">

import { ref, onMounted, onUnmounted, onActivated } from 'vue';
import dayjs from 'dayjs';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { orderService } from '@/services/order.service';
import type { Order } from '@/types';
import { useI18n } from 'vue-i18n';

const router = useRouter();
const { t } = useI18n();

const loading = ref(false);
const finished = ref(false);
const orders = ref<Order[]>([]);
const page = ref(1);
const pageSize = ref(10);

const onLoad = async () => {
    if (finished.value) return;

    loading.value = true;
    try {
        const res = await orderService.getOrderHistory(page.value, pageSize.value);
        const records = res?.records || [];

        if (records.length < pageSize.value) {
            finished.value = true;
        }

        if (records.length === 0 && page.value === 1) {
            
            finished.value = true;
            return;
        }

        
        orders.value = [...orders.value, ...records].sort((a, b) =>
            dayjs(b.orderTime).valueOf() - dayjs(a.orderTime).valueOf()
        );

        page.value += 1;
    } catch (e: any) {
        showToast(t('common.fail'));
        finished.value = true; 
    } finally {
        loading.value = false;
    }
};

const goToDetail = (id: string) => {
    router.push(`/order/detail/${id}`);
};

const refreshList = () => {
    page.value = 1;
    orders.value = [];
    finished.value = false;
    onLoad();
};


const onVisibilityChange = () => {
    if (!document.hidden) refreshList();
};

onMounted(() => {
    document.addEventListener('visibilitychange', onVisibilityChange);
});

onUnmounted(() => {
    document.removeEventListener('visibilitychange', onVisibilityChange);
});


onActivated(() => {
    refreshList();
});


const showCashier = ref(false);
const payingOrderNumber = ref('');
const payingOrderAmount = ref(0);
const paying = ref(false);

const showPayment = (order: any) => {
  payingOrderNumber.value = order.number;
  payingOrderAmount.value = order.amount;
  showCashier.value = true;
};

const handleMockPayment = async () => {
  if (!payingOrderNumber.value) return;
  paying.value = true;
  try {
    await orderService.mockPayment({
      orderNumber: payingOrderNumber.value,
      payMethod: 1
    });
    showToast({ type: 'success', message: t('order.paySuccess') });
    showCashier.value = false;

    
    page.value = 1;
    orders.value = [];
    finished.value = false;
    loading.value = true;
    onLoad();
  } catch (e: any) {
    showToast(e.message || t('order.payFail'));
  } finally {
    paying.value = false;
  }
};

const getStatusText = (status: number) => {
    const map: Record<number, string> = {
        1: t('order.status.unpaid'),
        2: t('order.status.pending'),
        3: t('order.status.accepted'),
        4: t('order.status.delivering'),
        5: t('order.status.completed'),
        6: t('order.status.cancelled')
    };
    return map[status] || 'Unknown';
};

</script>

<template>
  <div class="order-list-page">
    <van-nav-bar :title="t('order.listTitle')" />
    
    <van-list
      v-model:loading="loading"
      :finished="finished"
      :finished-text="t('order.noMore')"
      @load="onLoad"
    >
      <div
        v-for="order in orders"
        :key="order.id"
        class="order-card"
        @click="goToDetail(order.id)"
      >
        <div class="header">
          <span class="time">{{ order.orderTime }}</span>
          <span class="status">{{ getStatusText(order.status) }}</span>
        </div>
        <div class="body">
          <p>{{ t('order.orderNumber') }}：{{ order.number }}</p>
          <p>{{ t('cart.total') }} ¥{{ order.amount.toFixed(2) }}</p>
          <p>{{ order.consignee }}</p>
        </div>
        <div class="footer">
          <van-button size="small" type="primary" v-if="order.status === 1" @click.stop="showPayment(order)">{{ t('order.pay') }}</van-button>
          <van-button size="small" v-if="order.status === 2">{{ t('order.urge') }}</van-button>
          <van-button size="small" v-if="[3, 4, 5, 6].includes(order.status)">{{ t('order.repurchase') }}</van-button>
        </div>
      </div>
    </van-list>

    
    <van-action-sheet v-model:show="showCashier" :title="t('order.cashierTitle')">
      <div class="cashier-content">
        <div class="cashier-amount">
          <span class="currency">¥</span>
          <span class="price">{{ payingOrderAmount.toFixed(2) }}</span>
        </div>
        <p class="cashier-tip">{{ t('order.cashierTip') }}</p>
        <van-button
          type="primary"
          block
          round
          :loading="paying"
          @click="handleMockPayment"
          class="pay-btn"
        >
          {{ t('order.confirmPay') }}
        </van-button>
        <div class="mock-tip">{{ t('order.mockPayTip') }}</div>
      </div>
    </van-action-sheet>

    
    <van-tabbar route>
      <van-tabbar-item replace to="/" icon="home-o">{{ t('tabbar.menu') }}</van-tabbar-item>
      <van-tabbar-item replace to="/cart" icon="cart-o">{{ t('tabbar.cart') }}</van-tabbar-item>
      <van-tabbar-item replace to="/order" icon="orders-o">{{ t('tabbar.order') }}</van-tabbar-item>
      <van-tabbar-item replace to="/user" icon="user-o">{{ t('tabbar.user') }}</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style scoped>

.order-list-page {
    background-color: var(--page-bg-color);
    min-height: 100vh;
    padding-bottom: 50px;
}
.order-card {
    background: #fff;
    margin: 10px;
    padding: 15px;
    border-radius: 8px;
}
.header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
    border-bottom: 1px solid #f0f0f0;
    padding-bottom: 10px;
}
.time {
    color: #999;
    font-size: 12px;
}
.status {
    color: #ffc200;
    font-weight: bold;
}
.body {
    margin-bottom: 10px;
    font-size: 14px;
    color: #333;
}
.footer {
    display: flex;
    justify-content: flex-end;
}

.cashier-content {
  padding: 30px 20px 40px;
  text-align: center;
}

.cashier-amount {
  display: flex;
  justify-content: center;
  align-items: baseline;
  margin-bottom: 8px;
  color: #323233;
}

.cashier-amount .currency {
  font-size: 24px;
  margin-right: 4px;
  font-weight: 500;
}

.cashier-amount .price {
  font-size: 40px;
  font-weight: bold;
}

.cashier-tip {
  font-size: 14px;
  color: #969799;
  margin-bottom: 30px;
}

.pay-btn {
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 500;
  height: 44px;
}

.mock-tip {
  font-size: 12px;
  color: #c8c9cc;
}

</style>
