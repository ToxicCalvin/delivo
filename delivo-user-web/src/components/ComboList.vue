<script setup lang="ts">

import { useCartStore } from '@/stores/cart';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { useI18n } from 'vue-i18n';

interface CardItem {
  id: string;
  name: string;
  price: number;
  image: string;
  description: string;
}

const props = defineProps<{
  cards: CardItem[];
}>();

const emit = defineEmits(['added']);
const cartStore = useCartStore();
const router = useRouter();
const { t } = useI18n();

const addToCart = async (item: CardItem) => {
  try {
    await cartStore.addItem(item as any, 'dish');
    showToast('已加入购物车');
    emit('added');
  } catch (error) {
    console.error('Failed to add item to cart', error);
    showToast('添加失败');
  }
};

const goDetail = (id: string) => {
  router.push(`/dish/${id}`);
};


const comboTotal = props.cards.reduce((sum, item) => sum + item.price, 0);

const addAllToCart = async () => {
  try {
    for (const item of props.cards) {
      await cartStore.addItem(item as any, 'dish');
    }
    showToast('套餐已加入购物车');
    emit('added');
  } catch (error) {
    console.error('Failed to add combo to cart', error);
    showToast('套餐添加失败');
  }
}

</script>

<template>
  <div class="combo-list-container">
    <div class="combo-header">
      <span class="combo-title">{{ t('ai.recommendedCombo') }}</span>
      <span class="combo-price">¥{{ comboTotal.toFixed(2) }}</span>
    </div>
    
    <div class="combo-items">
      <div 
        v-for="(item, index) in cards" 
        :key="item.id + index" 
        class="combo-item"
        @click="goDetail(item.id)"
      >
        <div class="item-img-wrapper">
          <img :src="item.image" :alt="item.name" class="item-img" />
        </div>
        
        <div class="item-info">
          <div class="item-name">{{ item.name }}</div>
          <div class="item-desc van-multi-ellipsis--l2">{{ item.description }}</div>
          <div class="item-bottom">
            <span class="item-price">¥{{ item.price }}</span>
            <van-icon 
              name="add" 
              class="add-icon" 
              @click.stop="addToCart(item)" 
            />
          </div>
        </div>
      </div>
    </div>
    
    <div class="combo-action">
      <van-button 
        type="primary" 
        block 
        round 
        size="small"
        class="add-all-btn"
        @click="addAllToCart"
      >
        {{ t('ai.addAllToCart') }}
      </van-button>
    </div>
  </div>
</template>

<style scoped>

.combo-list-container {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  margin-top: 8px;
  border: 1px solid #f0f0f0;
  width: 260px;
}

.combo-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(90deg, #fffaf0, #fff);
  border-bottom: 1px dashed #ebedf0;
}

.combo-title {
  font-size: 14px;
  font-weight: bold;
  color: #d48806;
}

.combo-price {
  font-size: 16px;
  font-weight: bold;
  color: #ee0a24;
}

.combo-items {
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.combo-item {
  display: flex;
  gap: 12px;
  align-items: stretch;
  cursor: pointer;
}

.item-img-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f7f8fa;
}

.item-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0; 
}

.item-name {
  font-size: 14px;
  font-weight: bold;
  color: #323233;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-desc {
  font-size: 12px;
  color: #969799;
  line-height: 1.4;
  margin-top: 2px;
}

.item-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
}

.item-price {
  font-size: 14px;
  color: #ee0a24;
  font-weight: 500;
}

.add-icon {
  font-size: 20px;
  color: #ffc200;
  padding: 4px;
}

.combo-action {
  padding: 12px;
  padding-top: 4px;
}

.add-all-btn {
  background: linear-gradient(to right, #ff6034, #ee0a24);
  border: none;
}

</style>
