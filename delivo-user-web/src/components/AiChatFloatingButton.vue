<script setup lang="ts">

import { ref, nextTick, onMounted, watch, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useCartStore } from '@/stores/cart';
import { aiApi } from '@/api/ai';
import { showToast, showConfirmDialog } from 'vant';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import ProductCarousel from '@/components/ProductCarousel.vue';
import ComboList from '@/components/ComboList.vue';
import { orderService } from '@/services/order.service';

const showChat = ref(false);
const inputMessage = ref('');
const loading = ref(false);
const chatListRef = ref<HTMLElement | null>(null);
const { t } = useI18n();
const router = useRouter();
const cartStore = useCartStore();

// ── Rich Message Types ──
interface CardItem {
  id: string;
  name: string;
  price: number;
  image: string;
  description: string;
}

interface ActionInfo {
  type: string;
  summary: string;
}

interface Message {
  id: number;
  role: 'user' | 'assistant';
  content: string;
  uiType?: string;
  cards?: CardItem[];
  quickReplies?: string[];
  action?: ActionInfo | null;
}


const defaultMessage = computed<Message>(() => ({
  id: Date.now(),
  role: 'assistant',
  content: t('ai.greeting'),
  uiType: 'carousel',
  cards: [],
  quickReplies: [],
  action: null
}));

const messages = ref<Message[]>([]);

const authStore = useAuthStore();
const storageKey = computed(() => `delivo_ai_chat_history_${authStore.currentUser?.id || 'guest'}`);

const loadMessages = () => {
  const savedMessages = localStorage.getItem(storageKey.value);
  if (savedMessages) {
    try {
      messages.value = JSON.parse(savedMessages);
    } catch (e) {
      messages.value = [defaultMessage.value];
    }
  } else {
    messages.value = [defaultMessage.value];
  }
};

onMounted(() => {
  loadMessages();
});

watch(storageKey, () => {
  loadMessages();
});

watch(messages, (newVal) => {
  localStorage.setItem(storageKey.value, JSON.stringify(newVal));
}, { deep: true });


const scrollToBottom = async () => {
  await nextTick();
  if (chatListRef.value) {
    chatListRef.value.scrollTop = chatListRef.value.scrollHeight;
  }
};


const showCashier = ref(false);
const createdOrderNumber = ref('');
const createdOrderAmount = ref(0);
const paying = ref(false);

const handleActionClick = (action: ActionInfo) => {
  if (action.type === 'pending_payment') {
      const anyAction = action as any;
      if (anyAction.orderNumber && anyAction.orderAmount) {
          createdOrderNumber.value = anyAction.orderNumber;
          createdOrderAmount.value = anyAction.orderAmount;
          showCashier.value = true;
      }
  }
};

const submitMockPayment = async () => {
  if (!createdOrderNumber.value) return;
  paying.value = true;
  try {
      await orderService.mockPayment({
          orderNumber: createdOrderNumber.value,
          payMethod: 1 
      });
      showToast({ type: 'success', message: 'Payment successful' });
      showCashier.value = false;
      
      
      inputMessage.value = `Payment successful for order #${createdOrderNumber.value}`;
      sendMessage();
  } catch (e: any) {
      showToast(e.message || 'Payment simulation failed');
  } finally {
      paying.value = false;
  }
};


const parseAiResponse = (raw: string): { text: string; uiType: string; cards: CardItem[]; quickReplies: string[]; action: ActionInfo | null } => {
  try {
    
    let jsonStr = raw.trim();
    if (jsonStr.startsWith('```')) {
      jsonStr = jsonStr.replace(/^```(?:json)?\s*/, '').replace(/```\s*$/, '').trim();
    }
    const parsed = JSON.parse(jsonStr);
    return {
      text: parsed.text || '',
      uiType: parsed.uiType || 'carousel',
      cards: Array.isArray(parsed.cards) ? parsed.cards : [],
      quickReplies: Array.isArray(parsed.quickReplies) ? parsed.quickReplies : [],
      action: parsed.action || null
    };
  } catch {
    
    return { text: raw, uiType: 'carousel', cards: [], quickReplies: [], action: null };
  }
};


const systemChips = computed<string[]>(() => {
  const chips: string[] = [];
  if (cartStore.isEmpty) return chips;

  
  const lastMsg = messages.value[messages.value.length - 1];
  if (lastMsg && lastMsg.role === 'assistant' && lastMsg.action) {
    const activeCheckoutActions = ['query_address', 'create_address', 'order_submit', 'pending_payment'];
    if (activeCheckoutActions.includes(lastMsg.action.type)) {
      
      return chips;
    }
  }

  
  chips.push(`${t('ai.goCheckout')} (¥${cartStore.totalAmount.toFixed(0)})`);
  chips.push(t('ai.viewCart'));
  
  return chips;
});


const handleChipClick = (chipText: string) => {
  
  if (chipText === t('ai.viewCart') || chipText.includes(t('ai.viewCart'))) {
    router.push('/cart');
    showChat.value = false;
    return;
  }
  
  inputMessage.value = chipText;
  sendMessage();
};


const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return;
  
  const userText = inputMessage.value.trim();
  messages.value.push({ id: Date.now(), role: 'user', content: userText });
  inputMessage.value = '';
  loading.value = true;
  await scrollToBottom();

  try {
    const aiReply = await aiApi.chat(userText);
    loading.value = false;
    
    const parsed = parseAiResponse(aiReply || '');

    // Build the rich message
    const richMessage: Message = {
      id: Date.now(),
      role: 'assistant',
      content: '',
      uiType: parsed.uiType,
      cards: parsed.cards,
      quickReplies: parsed.quickReplies,
      action: parsed.action
    };
    messages.value.push(richMessage);

    // Typewriter effect for the text portion
    let i = 0;
    const aiText = parsed.text;
    const typingInterval = setInterval(() => {
      if (i < aiText.length) {
        const lastMsg = messages.value[messages.value.length - 1];
        if (lastMsg) {
          lastMsg.content += aiText.charAt(i);
        }
        i++;
        scrollToBottom();
      } else {
        clearInterval(typingInterval);
        scrollToBottom();
      }
    }, 20);
    
    // Refresh cart unconditionally to avoid sync bugs
    await cartStore.fetchCartList();
    
  } catch (error: any) {
    loading.value = false;
    messages.value.push({
      id: Date.now(),
      role: 'assistant',
      content: t('common.fail')
    });
    showToast(t('common.fail'));
    await scrollToBottom();
  }
};


const startNewChat = () => {
  showConfirmDialog({
    title: t('ai.newChatTitle'),
    message: t('ai.newChatMsg'),
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel')
  }).then(async () => {
    try {
      loading.value = true;
      await aiApi.clearMemory();
      messages.value = [defaultMessage.value];
      localStorage.removeItem(storageKey.value);
      showToast(t('ai.newChatSuccess'));
    } catch (e: any) {
      showToast(t('ai.newChatFail'));
    } finally {
      loading.value = false;
    }
  }).catch(() => {});
};

</script>

<template>
  <div>
    
    <div class="ai-fab" @click="showChat = true">
      <van-icon name="chat" size="28" color="#fff" />
      <span class="ai-bubble">{{ t('ai.fab') }}</span>
    </div>

    
    <van-popup
      v-model:show="showChat"
      position="bottom"
      round
      :style="{ height: '80%' }"
    >
      <div class="chat-container">
        
        <div class="chat-header">
          <van-icon name="arrow-down" class="header-icon" @click="showChat = false" />
          <span class="title">{{ t('ai.title') }}</span>
          <van-icon name="plus" class="header-icon plus-icon" @click="startNewChat" />
        </div>

        
        <div class="chat-list" ref="chatListRef">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-group"
          >
            <div :class="['message-row', msg.role]">
              <div class="avatar" v-if="msg.role === 'assistant'">🤖</div>
              <div class="bubble-wrapper" v-if="msg.role === 'assistant'">
                
                <div class="bubble" v-if="msg.content">{{ msg.content }}</div>
                
                
                <div v-if="msg.action" class="action-card" :class="{'clickable': msg.action.type === 'pending_payment'}" @click="handleActionClick(msg.action)">
                  <van-icon v-if="msg.action.type === 'pending_payment'" name="gold-coin" color="#ff976a" size="18" />
                  <van-icon v-else name="checked" color="#07c160" size="18" />
                  <span class="action-text">{{ msg.action.summary }}</span>
                  <van-icon v-if="msg.action.type === 'pending_payment'" name="arrow" color="#969799" />
                </div>
                
                
                <ProductCarousel 
                  v-if="msg.cards && msg.cards.length > 0 && msg.uiType !== 'combo_list'" 
                  :cards="msg.cards"
                  @added="scrollToBottom"
                />
                
                
                <ComboList
                  v-if="msg.cards && msg.cards.length > 0 && msg.uiType === 'combo_list'"
                  :cards="msg.cards"
                  @added="scrollToBottom"
                />
              </div>
              
              
              <div class="bubble" v-if="msg.role === 'user'">{{ msg.content }}</div>
              <div class="avatar" v-if="msg.role === 'user'">👤</div>
            </div>
            
            
            <div 
              v-if="msg.role === 'assistant' && ((msg.quickReplies && msg.quickReplies.length > 0) || systemChips.length > 0)"
              class="chips-row"
            >
              <div 
                v-for="chip in (msg.quickReplies || [])" 
                :key="chip" 
                class="chip"
                @click="handleChipClick(chip)"
              >
                {{ chip }}
              </div>
              
              <template v-if="msg.id === messages[messages.length - 1]?.id">
                <div 
                  v-for="sChip in systemChips" 
                  :key="sChip" 
                  class="chip system-chip"
                  @click="handleChipClick(sChip)"
                >
                  {{ sChip }}
                </div>
              </template>
            </div>
          </div>
          
          
          <div v-if="loading" class="message-row assistant">
            <div class="avatar">🤖</div>
            <div class="bubble typing">
              <van-loading type="spinner" size="16" /> {{ t('ai.thinking') }}
            </div>
          </div>
        </div>

        
        <div class="chat-input-area">
          <van-field
            v-model="inputMessage"
            center
            clearable
            :placeholder="t('ai.placeholder')"
            @keyup.enter="sendMessage"
          >
            <template #button>
              <van-button size="small" type="primary" :loading="loading" @click="sendMessage">{{ t('ai.send') }}</van-button>
            </template>
          </van-field>
        </div>
      </div>
    </van-popup>

    
    <van-action-sheet v-model:show="showCashier" :title="t('order.cashierTitle')">
      <div class="cashier-content">
        <div class="cashier-amount">
          <span class="currency">¥</span>
          <span class="price">{{ typeof createdOrderAmount === 'number' ? createdOrderAmount.toFixed(2) : parseFloat(createdOrderAmount).toFixed(2) }}</span>
        </div>
        <p class="cashier-tip">{{ t('order.cashierTip') }}</p>
        <van-button
          type="primary"
          block
          round
          :loading="paying"
          @click="submitMockPayment"
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

.ai-fab {
  position: fixed;
  bottom: 120px;
  right: 16px;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #1989fa, #0570db);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 16px rgba(25, 137, 250, 0.4);
  z-index: 100;
  cursor: pointer;
}

.ai-bubble {
  position: absolute;
  top: -10px;
  right: -5px;
  background: #ee0a24;
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  white-space: nowrap;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #f7f8fa;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background-color: #fff;
  border-bottom: 1px solid #ebedf0;
  font-size: 16px;
  font-weight: bold;
}

.title {
  flex: 1;
  text-align: center;
}

.header-icon {
  font-size: 20px;
  color: #969799;
  cursor: pointer;
}

.plus-icon {
  color: #1989fa;
}

.chat-list {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-group {
  display: flex;
  flex-direction: column;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  max-width: 100%;
}

.message-row.user {
  justify-content: flex-end;
}

.avatar {
  font-size: 24px;
  flex-shrink: 0;
  margin-top: 4px;
}

.bubble-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 80%;
}

.bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.message-row.user .bubble {
  background-color: #1989fa;
  color: #fff;
  border-bottom-right-radius: 2px;
}

.message-row.assistant .bubble {
  background-color: #fff;
  color: #323233;
  border-bottom-left-radius: 2px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f0fff0;
  border: 1px solid #b7eb8f;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 13px;
  color: #333;
}

.action-text {
  flex: 1;
}

.chips-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 6px 0 0 36px;
}

.chip {
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 12px;
  background: #fff;
  border: 1px solid #dcdee0;
  color: #1989fa;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.chip:active {
  background: #ecf5ff;
}

.system-chip {
  border-color: #ee0a24;
  color: #ee0a24;
  font-weight: bold;
}

.system-chip:active {
  background: #fff0f0;
}

.chat-input-area {
  padding: 8px;
  background-color: #fff;
  border-top: 1px solid #ebedf0;
}

.action-card.clickable {
  cursor: pointer;
  border-color: #ffd8a8;
  background: #fff9f2;
}

.action-card.clickable:active {
  background: #fff4e5;
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
