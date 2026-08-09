<script setup lang="ts">

import { useCartStore } from '@/stores/cart';
import { showSuccessToast } from 'vant';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';

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

const emit = defineEmits<{
  (e: 'added'): void;
}>();

const cartStore = useCartStore();
const { t } = useI18n();
const router = useRouter();

const handleAddToCart = async (card: CardItem) => {
  const dish = {
    id: card.id,
    name: card.name,
    price: card.price,
    image: card.image,
    categoryId: '',
    description: card.description,
    status: 'available' as const
  };
  await cartStore.addItem(dish, 'dish');
  showSuccessToast(t('cart.added'));
  emit('added');
};

const handleCardClick = (card: CardItem) => {
  router.push(`/dish/${card.id}`);
};

</script>

<template>
  <div class="product-carousel">
    <div class="carousel-scroll">
      <div 
        v-for="card in props.cards" 
        :key="card.id" 
        class="product-card"
      >
        <img
          :src="card.image || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
          class="product-img"
          @click="handleCardClick(card)"
        />
        <div class="product-info">
          <div class="product-name">{{ card.name }}</div>
          <div class="product-desc">{{ card.description }}</div>
          <div class="product-bottom">
            <span class="product-price">¥{{ card.price }}</span>
            <van-button
              size="mini"
              round
              type="primary"
              icon="plus"
              class="add-btn"
              @click.stop="handleAddToCart(card)"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

.product-carousel {
  width: 100%;
  overflow: hidden;
  margin-top: 8px;
}

.carousel-scroll {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding: 4px 0 8px 0;
  scroll-snap-type: x mandatory;
  -webkit-overflow-scrolling: touch;
}

.carousel-scroll::-webkit-scrollbar {
  display: none;
}

.product-card {
  flex-shrink: 0;
  width: 140px;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  scroll-snap-align: start;
}

.product-img {
  width: 100%;
  height: 100px;
  object-fit: cover;
  cursor: pointer;
}

.product-info {
  padding: 8px;
}

.product-name {
  font-size: 13px;
  font-weight: bold;
  color: #323233;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 11px;
  color: #969799;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6px;
}

.product-price {
  font-size: 14px;
  font-weight: bold;
  color: #ee0a24;
}

.add-btn {
  width: 24px;
  height: 24px;
  padding: 0;
}

</style>
