<script setup lang="ts">

import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue';
import { useI18n } from 'vue-i18n';
import { useAuthStore } from '@/stores/auth';

type Orientation = 'portrait' | 'landscape';

const orientation = ref<Orientation>('portrait');
const { t } = useI18n();
const authStore = useAuthStore();


type NoticeKind = 'reject' | 'confirmed' | 'delivering' | 'done';
const orderNotice = ref<{ visible: boolean; kind: NoticeKind; reason: string }>({
  visible: false, kind: 'reject', reason: ''
});
let noticeTimer: ReturnType<typeof setTimeout> | null = null;
function showOrderNotice(kind: NoticeKind, reason = '') {
  if (noticeTimer) { clearTimeout(noticeTimer); noticeTimer = null; }
  orderNotice.value = { visible: true, kind, reason };
  // Auto-dismiss non-rejection notices after 5 seconds
  if (kind !== 'reject') {
    noticeTimer = setTimeout(() => { orderNotice.value.visible = false; }, 5000);
  }
}
function dismissOrderNotice() {
  if (noticeTimer) { clearTimeout(noticeTimer); noticeTimer = null; }
  orderNotice.value.visible = false;
}


let ws: WebSocket | null = null;
let audioCtx: AudioContext | null = null;

function initAudioContext() {
  const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
  if (!AudioCtx) return;
  audioCtx = new AudioCtx();
  const resume = () => { audioCtx?.state === 'suspended' && audioCtx.resume().catch(() => {}); };
  document.addEventListener('click', resume, { once: true });
}

function playChime() {
  if (!audioCtx) return;
  const doPlay = () => {
    const ctx = audioCtx!;
    const playNote = (freq: number, start: number, dur: number) => {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.connect(gain); gain.connect(ctx.destination);
      osc.type = 'sine'; osc.frequency.value = freq;
      gain.gain.setValueAtTime(0, start);
      gain.gain.linearRampToValueAtTime(0.35, start + 0.015);
      gain.gain.exponentialRampToValueAtTime(0.001, start + dur);
      osc.start(start); osc.stop(start + dur);
    };
    const now = ctx.currentTime;
    playNote(880, now, 0.4);
    playNote(660, now + 0.25, 0.5);
  };
  audioCtx.state === 'suspended'
    ? audioCtx.resume().then(doPlay).catch(() => {})
    : doPlay();
}

function handleWsMessage(data: { type: number; orderId: number; reason?: string; content?: string }) {
  if (data.type === 3) {
    playChime();
    showOrderNotice('reject', data.reason || '');
  } else if (data.type === 4) {
    playChime();
    showOrderNotice('confirmed');
  } else if (data.type === 5) {
    playChime();
    showOrderNotice('delivering');
  } else if (data.type === 6) {
    playChime();
    showOrderNotice('done');
  }
}

function connectWebSocket(userId: string | number) {
  closeWebSocket();
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const wsUrl = `${protocol}//${window.location.host}/ws/${userId}`;
  console.log('[WS] connecting sid=', userId, 'url=', wsUrl);
  ws = new WebSocket(wsUrl);
  ws.onopen = () => console.log('[WS] connected sid=', userId);
  ws.onclose = (e) => console.log('[WS] closed', e.code, e.reason);
  ws.onerror = (e) => console.error('[WS] error', e);
  ws.onmessage = (event) => {
    console.log('[WS] message:', event.data);
    try { handleWsMessage(JSON.parse(event.data)); } catch (err) {
      console.error('[WS] parse error', err);
    }
  };
}

function closeWebSocket() {
  ws?.close(); ws = null;
}

watch(
  () => authStore.user?.id,
  (userId) => {
    if (userId) connectWebSocket(userId);
    else closeWebSocket();
  },
  { immediate: true }
);


const updateOrientation = () => {
  if (typeof window === 'undefined') return;
  const { innerWidth, innerHeight } = window;
  orientation.value = innerWidth > innerHeight ? 'landscape' : 'portrait';
};

onMounted(() => {
  updateOrientation();
  window.addEventListener('resize', updateOrientation);
  window.addEventListener('orientationchange', updateOrientation as any);
  initAudioContext();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateOrientation);
  window.removeEventListener('orientationchange', updateOrientation as any);
  closeWebSocket();
  audioCtx?.close().catch(() => {}); audioCtx = null;
});

const orientationClass = computed(() =>
  orientation.value === 'landscape' ? 'app-landscape' : 'app-portrait'
);

</script>

<template>
  <div id="app" :class="orientationClass">
    <van-config-provider>
      <router-view />
    </van-config-provider>

    
    <transition name="notice-slide">
      <div v-if="orderNotice.visible" class="order-notice" :class="`order-notice--${orderNotice.kind}`">
        <div class="order-notice__icon">
          <template v-if="orderNotice.kind === 'reject'">✕</template>
          <template v-else-if="orderNotice.kind === 'confirmed'">✓</template>
          <template v-else-if="orderNotice.kind === 'delivering'">🛵</template>
          <template v-else>✓</template>
        </div>
        <div class="order-notice__body">
          <template v-if="orderNotice.kind === 'reject'">
            <p class="order-notice__title">{{ t('order.rejected') }}</p>
            <p class="order-notice__msg">{{ t('order.rejectedMsg') }}</p>
            <p v-if="orderNotice.reason" class="order-notice__reason">{{ orderNotice.reason }}</p>
          </template>
          <template v-else-if="orderNotice.kind === 'confirmed'">
            <p class="order-notice__title">{{ t('order.confirmed') }}</p>
            <p class="order-notice__msg">{{ t('order.confirmedMsg') }}</p>
          </template>
          <template v-else-if="orderNotice.kind === 'delivering'">
            <p class="order-notice__title">{{ t('order.delivering') }}</p>
            <p class="order-notice__msg">{{ t('order.deliveringMsg') }}</p>
          </template>
          <template v-else>
            <p class="order-notice__title">{{ t('order.done') }}</p>
            <p class="order-notice__msg">{{ t('order.doneMsg') }}</p>
          </template>
        </div>
        <button class="order-notice__close" @click="dismissOrderNotice">✕</button>
      </div>
    </transition>
  </div>
</template>

<style>


</style>

<style scoped>

.order-notice {
  position: fixed;
  bottom: 80px;
  left: 16px;
  right: 16px;
  z-index: 9999;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  border-left: 4px solid #909399;
}

.order-notice--reject { border-left-color: #ee0a24; }
.order-notice--confirmed { border-left-color: #07c160; }
.order-notice--delivering { border-left-color: #1989fa; }
.order-notice--done { border-left-color: #07c160; }

.order-notice__icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
}

.order-notice--reject .order-notice__icon { background: #fff0f0; color: #ee0a24; }
.order-notice--confirmed .order-notice__icon { background: #e8f9ef; color: #07c160; }
.order-notice--delivering .order-notice__icon { background: #e8f3ff; color: #1989fa; }
.order-notice--done .order-notice__icon { background: #e8f9ef; color: #07c160; }

.order-notice__body {
  flex: 1;
  min-width: 0;
}

.order-notice__title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.order-notice--reject .order-notice__title { color: #ee0a24; }
.order-notice--confirmed .order-notice__title { color: #07c160; }
.order-notice--delivering .order-notice__title { color: #1989fa; }
.order-notice--done .order-notice__title { color: #07c160; }

.order-notice__msg {
  font-size: 13px;
  color: #646566;
  margin: 0 0 4px 0;
}

.order-notice__reason {
  font-size: 13px;
  color: #323233;
  background: #f7f8fa;
  border-radius: 6px;
  padding: 6px 10px;
  margin: 6px 0 0 0;
  word-break: break-all;
}

.order-notice__close {
  flex-shrink: 0;
  background: none;
  border: none;
  color: #c8c9cc;
  font-size: 16px;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.notice-slide-enter-active,
.notice-slide-leave-active {
  transition: all 0.3s ease;
}
.notice-slide-enter-from,
.notice-slide-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

</style>
