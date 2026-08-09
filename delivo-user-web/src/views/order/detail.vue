
<script setup lang="ts">

import { ref, onMounted, computed, onUnmounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast, showLoadingToast, closeToast } from 'vant';
import { orderService } from '@/services/order.service';
import type { Order } from '@/types';
import { useI18n } from 'vue-i18n';

const route = useRoute();
const router = useRouter();
const { t } = useI18n();
const orderId = route.params.id as string;

const order = ref<Order | null>(null);

const statusText = computed(() => {
  if (!order.value) return '';
  switch (order.value.status) {
    case 1:
      return t('order.status.unpaid');
    case 2:
      return t('order.status.pending');
    case 3:
      return t('order.status.accepted');
    case 4:
      return t('order.status.delivering');
    case 5:
      return t('order.status.completed');
    case 6:
      return t('order.status.cancelled');
    default:
      return 'Unknown';
  }
});

const orderDetails = computed(() => order.value?.orderDetailList ?? []);
const hasOrderDetails = computed(() => orderDetails.value.length > 0);

const formatFlavor = (flavorStr: string) => {
    try {
        const arr = JSON.parse(flavorStr);
        return Array.isArray(arr) ? arr.join(', ') : flavorStr;
    } catch {
        return flavorStr;
    }
};


const ACTIVE_STATUSES = [2, 3, 4];
let pollTimer: ReturnType<typeof setInterval> | null = null;

const startPolling = () => {
  if (pollTimer) return;
  pollTimer = setInterval(async () => {
    try {
      const res = await orderService.getOrderDetail(orderId);
      order.value = res;
    } catch {}
  }, 5000);
};

const stopPolling = () => {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null; }
};


watch(() => order.value?.status, (status) => {
  if (status != null && !ACTIVE_STATUSES.includes(status)) stopPolling();
});

onMounted(async () => {
  if (!orderId) return;
  showLoadingToast({ message: t('common.loading'), forbidClick: true });
  try {
    const res = await orderService.getOrderDetail(orderId);
    order.value = res;
    if (ACTIVE_STATUSES.includes(res.status)) startPolling();
  } catch (e: any) {
    showToast(t('common.fail'));
  } finally {
    closeToast();
  }
});

const handleCancel = async () => {
  try {
    await orderService.cancelOrder(orderId);
    showToast(t('common.success'));
    const res = await orderService.getOrderDetail(orderId);
    order.value = res;
  } catch (e: any) {
    showToast(t('common.fail'));
  }
};


const REMINDER_COOLDOWN = 60;
const reminderStorageKey = `reminder_expiry_${orderId}`;
const reminderCooldown = ref(0);
let reminderTimer: ReturnType<typeof setInterval> | null = null;

const startCooldownTimer = (seconds: number) => {
  reminderCooldown.value = seconds;
  reminderTimer = setInterval(() => {
    reminderCooldown.value--;
    if (reminderCooldown.value <= 0 && reminderTimer) {
      clearInterval(reminderTimer);
      reminderTimer = null;
      sessionStorage.removeItem(reminderStorageKey);
    }
  }, 1000);
};


const storedExpiry = sessionStorage.getItem(reminderStorageKey);
if (storedExpiry) {
  const remaining = Math.ceil((parseInt(storedExpiry) - Date.now()) / 1000);
  if (remaining > 0) {
    startCooldownTimer(remaining);
  } else {
    sessionStorage.removeItem(reminderStorageKey);
  }
}

const handleReminder = async () => {
  if (reminderCooldown.value > 0) return;
  try {
    await orderService.reminderOrder(orderId);
    showToast({ type: 'success', message: t('order.urge') + ' ✓' });
    const expiry = Date.now() + REMINDER_COOLDOWN * 1000;
    sessionStorage.setItem(reminderStorageKey, expiry.toString());
    startCooldownTimer(REMINDER_COOLDOWN);
  } catch {
    showToast(t('common.fail'));
  }
};

onUnmounted(() => {
  if (reminderTimer) clearInterval(reminderTimer);
  stopPolling();
});

const handleReorder = async () => {
  try {
    await orderService.repetitionOrder(orderId);
    showToast(t('cart.added'));
    router.push('/cart');
  } catch (e: any) {
    showToast(t('common.fail'));
  }
};


const showCashier = ref(false);
const paying = ref(false);

const handleMockPayment = async () => {
  if (!order.value?.number) return;
  paying.value = true;
  try {
    await orderService.mockPayment({
      orderNumber: order.value.number,
      payMethod: 1
    });
    showToast({ type: 'success', message: t('order.paySuccess') });
    showCashier.value = false;

    
    const res = await orderService.getOrderDetail(orderId);
    order.value = res;
  } catch (e: any) {
    showToast(e.message || t('order.payFail'));
  } finally {
    paying.value = false;
  }
};

</script>

<template>
  <div class="order-detail-page">
    <van-nav-bar :title="t('order.detailTitle')" :left-text="t('common.back')" left-arrow @click-left="router.back()" />
    
    <div v-if="order" class="content">
      <div class="status-card">
        <h2>{{ statusText }}</h2>
      </div>

      
      <van-cell-group inset style="margin-top: 10px;">
        <van-cell :title="t('order.orderNumber')" :value="order.number" />
        <van-cell :title="t('order.orderTime')" :value="order.orderTime" />
        <van-cell :title="t('order.payAmount')" :value="'¥' + order.amount.toFixed(2)" />
        <van-cell :title="t('order.remark')" :value="order.remark || t('common.empty')" />
      </van-cell-group>
      
      
      <van-cell-group inset style="margin-top: 10px;">
        <van-cell :title="t('order.address')" />
        <van-cell :title="order.consignee" :value="order.phone" />
        <van-cell :label="order.address" />
      </van-cell-group>

      
      <van-cell-group inset style="margin-top: 10px; margin-bottom: 20px;">
        <van-cell :title="t('order.items')" />
        <div v-if="hasOrderDetails" class="order-items">
          <div
            v-for="item in orderDetails"
            :key="item.id"
            class="order-item"
          >
            <div class="item-main">
              <span class="name">{{ item.name }}</span>
              <span class="count">x{{ item.number }}</span>
            </div>
            <div class="item-sub">
              <span v-if="item.dishFlavor" class="flavor">{{ formatFlavor(item.dishFlavor) }}</span>
              <span class="price">¥{{ item.amount.toFixed(2) }}</span>
            </div>
          </div>
        </div>
        <van-cell v-else :label="order.orderDishes || t('common.empty')" />
      </van-cell-group>

      
      <div class="footer-actions">
        <van-button
          block
          type="primary"
          v-if="order.status === 1"
          @click="showCashier = true"
          style="margin-bottom: 15px;"
        >
          {{ t('order.pay') }} (¥{{ order.amount.toFixed(2) }})
        </van-button>
        <van-button block v-if="order.status === 1" @click="handleCancel">
          {{ t('order.cancel') }}
        </van-button>
        <br />
        <van-button
          block
          type="warning"
          v-if="[2, 3, 4].includes(order.status)"
          :disabled="reminderCooldown > 0"
          @click="handleReminder"
          style="margin-bottom: 15px;"
        >
          {{ reminderCooldown > 0 ? `${t('order.urge')} (${reminderCooldown}s)` : t('order.urge') }}
        </van-button>
        <van-button
          block
          type="primary"
          v-if="[3, 4, 5, 6].includes(order.status)"
          @click="handleReorder"
        >
          {{ t('order.repurchase') }}
        </van-button>
      </div>
    </div>

    
    <van-action-sheet v-model:show="showCashier" :title="t('order.cashierTitle')">
      <div class="cashier-content">
        <div class="cashier-amount">
          <span class="currency">¥</span>
          <span class="price">{{ order?.amount?.toFixed(2) }}</span>
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
  </div>
</template>

<style scoped>

.order-detail-page {
  background-color: var(--page-bg-color);
  min-height: 100vh;
}

.status-card {
  background: #fff;
  padding: 20px;
  text-align: center;
}

.content {
  padding-bottom: 20px;
}

.order-items {
  padding: 8px 16px 12px;
}

.order-item {
  display: flex;
  flex-direction: column;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.order-item:last-child {
  border-bottom: none;
}

.item-main {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #323233;
}

.item-sub {
  margin-top: 4px;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #969799;
}

.name {
  font-weight: 500;
}

.count {
  color: #969799;
}

.price {
  color: #ee0a24;
  font-weight: 500;
}

.footer-actions {
  padding: 20px;
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
