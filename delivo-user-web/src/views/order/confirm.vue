<template>
  <div class="order-confirm">
    
    <van-nav-bar
      :title="t('order.confirmTitle')"
      left-arrow
      @click-left="router.back()"
      fixed
      placeholder
    />

    
    <div v-if="selectedAddress" class="address-card" @click="handleSelectAddress">
      <div class="address-icon">
        <van-icon name="location" color="#1989fa" size="24" />
      </div>
      <div class="address-info">
        <div class="user-info">
          <span class="name">{{ selectedAddress.consignee }}</span>
          <span class="phone">{{ selectedAddress.phone }}</span>
        </div>
        <div class="detail-address">
          {{ formatAddress(selectedAddress) }}
        </div>
      </div>
      <div class="address-arrow">
        <van-icon name="arrow" color="#969799" />
      </div>
    </div>
    
    
    <div v-else class="no-address-card" @click="handleSelectAddress">
      <div class="no-address-icon">
        <van-icon name="add-square" size="24" color="#1989fa" />
      </div>
      <span class="no-address-text">{{ addressStore.addresses.length > 0 ? '请选择收货地址' : '请添加收货地址' }}</span>
      <div class="address-arrow">
        <van-icon name="arrow" color="#969799" />
      </div>
    </div>

    
    <div class="goods-section">
      <div class="section-title">{{ t('order.items') }}</div>
      <van-card
        v-for="item in cartStore.cartItems"
        :key="item.id"
        :title="item.item.name"
        :desc="item.item.description"
        :thumb="item.item.image"
        :price="item.item.price.toFixed(2)"
        :num="item.quantity"
      >
        <template #tag>
          <van-tag v-if="item.type === 'combo'" type="primary" size="medium">{{ t('menu.title').includes('Delivo') ? 'Combo' : '套餐' }}</van-tag>
        </template>
        <template #tags>
          <van-tag
            plain
            type="primary"
            v-if="item.dishFlavor"
            class="flavor-tag"
          >
            {{ formatFlavor(item.dishFlavor) }}
          </van-tag>
        </template>
      </van-card>
    </div>

    
    <div class="remark-section">
      <van-field
        v-model="remark"
        rows="2"
        autosize
        type="textarea"
        maxlength="50"
        :placeholder="t('order.remarkPlaceholder')"
        show-word-limit
      />
    </div>

    
    <van-submit-bar
      :price="cartStore.totalAmount * 100"
      :button-text="t('order.submit')"
      :loading="submitting"
      :disabled="!selectedAddress"
      @submit="handleSubmit"
    >
      <template #tip>
        <div v-if="!selectedAddress" class="submit-tip">
          {{ t('order.selectAddressFirst') }}
        </div>
      </template>
    </van-submit-bar>

    
    <van-action-sheet v-model:show="showCashier" :title="t('order.cashierTitle')">
      <div class="cashier-content">
        <div class="cashier-amount">
          <span class="currency">¥</span>
          <span class="price">{{ createdOrderAmount.toFixed(2) }}</span>
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

<script setup lang="ts">

import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useCartStore } from '@/stores/cart';
import { useAddressStore } from '@/stores/address';
import { orderService } from '@/services/order.service';
import type { Address } from '@/types';
import { showToast } from 'vant';
import { useI18n } from 'vue-i18n';

const router = useRouter();
const cartStore = useCartStore();
const addressStore = useAddressStore();
const { t } = useI18n();


const selectedAddress = ref<Address | null>(null);
const remark = ref<string>('');
const submitting = ref<boolean>(false);

// Mock Cashier State
const showCashier = ref(false);
const createdOrderNumber = ref('');
const createdOrderAmount = ref(0);
const paying = ref(false);

/**
 * 格式化地址显示
 */
function formatAddress(address: Address): string {
  return `${address.provinceName || ''}${address.cityName || ''}${address.districtName || ''}${address.detail || ''}`;
}

const formatFlavor = (flavorStr: string) => {
    try {
        const arr = JSON.parse(flavorStr);
        return Array.isArray(arr) ? arr.join(', ') : flavorStr;
    } catch {
        return flavorStr;
    }
};

/**
 * 选择地址
 */
function handleSelectAddress() {
  router.push({
    path: '/address',
    query: { select: 'true' }
  });
}

async function handleSubmit() {
  if (!selectedAddress.value) {
    showToast(t('common.empty'));
    return;
  }

  if (cartStore.isEmpty) {
    showToast(t('common.empty'));
    return;
  }

  submitting.value = true;

  try {
    
    const now = new Date();
    now.setHours(now.getHours() + 1);
    const pad = (n: number) => n.toString().padStart(2, '0');
    const estimatedDeliveryTime = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;

    
    const orderResult = await orderService.createOrder({
      addressBookId: selectedAddress.value.id,
      payMethod: 1, 
      remark: remark.value,
      amount: cartStore.totalAmount, 
      estimatedDeliveryTime, 
      deliveryStatus: 1, 
      packAmount: 0, 
      tablewareNumber: 1, 
      tablewareStatus: 1 
    });

    
    const orderData = orderResult.data || orderResult;
    createdOrderNumber.value = orderData.orderNumber;
    createdOrderAmount.value = orderData.orderAmount || cartStore.totalAmount;

    
    cartStore.clearCart();

    
    showCashier.value = true;
  } catch (error: any) {
    showToast(error.message || t('common.fail'));
  } finally {
    submitting.value = false;
  }
}

async function handleMockPayment() {
  if (!createdOrderNumber.value) return;
  
  paying.value = true;
  try {
    await orderService.mockPayment({
      orderNumber: createdOrderNumber.value,
      payMethod: 1
    });
    
    showToast({ type: 'success', message: t('order.paySuccess') });
    showCashier.value = false;

    
    router.replace('/order');
  } catch (e: any) {
    showToast(e.message || t('order.payFail'));
  } finally {
    paying.value = false;
  }
}

onMounted(async () => {
  
  await cartStore.fetchCartList();

  
  if (cartStore.isEmpty) {
    showToast(t('common.empty'));
    router.replace('/menu');
    return;
  }

  
  try {
    await addressStore.fetchAddresses();
    
    
    if (addressStore.defaultAddress) {
      selectedAddress.value = addressStore.defaultAddress || null;
    } else if (addressStore.addresses && addressStore.addresses.length > 0) {
      selectedAddress.value = addressStore.addresses[0] || null;
    }
  } catch (error) {
    console.error('Failed to load addresses:', error);
  }
});

</script>

<style scoped>

.order-confirm {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 50px;
}

.address-card, .no-address-card {
  background-color: #fff;
  margin: 12px;
  padding: 16px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
}

.address-icon, .no-address-icon {
  margin-right: 12px;
  display: flex;
  align-items: center;
}

.address-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.user-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.user-info .name {
  font-size: 16px;
  font-weight: bold;
  color: #323233;
}

.user-info .phone {
  font-size: 14px;
  color: #969799;
}

.detail-address {
  font-size: 13px;
  color: #323233;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
    -webkit-box-direction: normal;
  overflow: hidden;
}

.no-address-text {
  flex: 1;
  font-size: 15px;
  color: #323233;
  font-weight: 500;
}

.address-arrow {
  margin-left: 8px;
  display: flex;
  align-items: center;
}

.goods-section {
  background-color: #fff;
  margin: 12px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.section-title {
  padding: 12px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #323233;
  border-bottom: 1px solid #ebedf0;
}

.goods-section :deep(.van-card) {
  background-color: #fff;
  padding: 12px 16px;
}

.flavor-tag {
  margin-top: 4px;
}

.remark-section {
  background-color: #fff;
  margin: 12px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.submit-tip {
  color: #ee0a24;
  font-size: 12px;
  padding: 4px 0;
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
