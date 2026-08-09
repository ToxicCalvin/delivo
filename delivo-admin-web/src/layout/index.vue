<template>
  <el-container class="layout-container">
    <el-aside width="240px" class="sidebar">
      <div class="logo">
        <div class="logo-icon">
          <el-icon><Food /></el-icon>
        </div>
        <div class="logo-text">
          <h2>{{ t('login.title') }}</h2>
          <p>{{ t('layout.systemName') }}</p>
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
        background-color="transparent"
        text-color="#a0aec0"
        active-text-color="#2db87a"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>{{ t('layout.dashboard') }}</span>
        </el-menu-item>
        <el-menu-item index="/employee">
          <el-icon><User /></el-icon>
          <span>{{ t('layout.employee') }}</span>
        </el-menu-item>
        <el-menu-item index="/category">
          <el-icon><Menu /></el-icon>
          <span>{{ t('layout.category') }}</span>
        </el-menu-item>
        <el-menu-item index="/dish">
          <el-icon><Food /></el-icon>
          <span>{{ t('layout.dish') }}</span>
        </el-menu-item>
        <el-menu-item index="/setmeal">
          <el-icon><Goods /></el-icon>
          <span>{{ t('layout.setmeal') }}</span>
        </el-menu-item>
        <el-menu-item index="/order">
          <el-icon><List /></el-icon>
          <span>{{ t('layout.order') }}</span>
        </el-menu-item>
        <el-menu-item index="/report">
          <el-icon><DataLine /></el-icon>
          <span>{{ t('layout.report') }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="header-menu-icon"><Grid /></el-icon>
          <span class="header-title">{{ t('layout.systemName') }}</span>
        </div>
        <div class="header-right">
          <LanguageSwitcher />
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-info">
              <div class="user-avatar">
                <el-icon><User /></el-icon>
              </div>
              <span class="user-name">{{ userStore.userInfo?.name || t('layout.admin') }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown">
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  {{ t('layout.logout') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>

import { computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElNotification } from 'element-plus'
import { useUserStore } from '@/store/user'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

let ws = null
let audioCtx = null

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout().then(() => {
      closeWebSocket()
      router.push('/login')
    })
  }
}

function initAudioContext() {
  try {
    const AudioCtx = window.AudioContext || window.webkitAudioContext
    if (!AudioCtx) return
    audioCtx = new AudioCtx()
    
    document.addEventListener('click', resumeAudioOnce, { once: true })
  } catch (e) {  }
}

function resumeAudioOnce() {
  if (audioCtx && audioCtx.state === 'suspended') {
    audioCtx.resume().catch(() => {})
  }
}

function initWebSocket() {
  const sid = (userStore.userInfo && userStore.userInfo.id) ? userStore.userInfo.id : 'admin'
  const wsUrl = `ws://localhost:8080/ws/${sid}`
  ws = new WebSocket(wsUrl)

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      handleWsMessage(data)
    } catch (e) {
      console.error('WebSocket message parse error:', e)
    }
  }

  ws.onerror = (err) => {
    console.error('WebSocket error:', err)
  }
}

function closeWebSocket() {
  if (ws) {
    ws.close()
    ws = null
  }
}

function handleWsMessage(data) {
  const { type, content } = data
  
  const orderNum = content ? content.replace(/^.*?[:：]/, '').trim() : ''

  let title, message, notifyType

  if (type === 1) {
    title = t('notify.newOrder.title')
    message = t('notify.newOrder.message', { orderNum })
    notifyType = 'success'
  } else if (type === 2) {
    title = t('notify.reminder.title')
    message = t('notify.reminder.message', { orderNum })
    notifyType = 'warning'
  } else {
    return
  }

  
  ElNotification({
    title,
    message,
    type: notifyType,
    duration: 0,
    position: 'top-right'
  })

  playChime()
}

function playChime() {
  const ctx = audioCtx
  if (!ctx) return

  const doPlay = () => {
    const playNote = (freq, startTime, duration) => {
      const osc = ctx.createOscillator()
      const gain = ctx.createGain()
      osc.connect(gain)
      gain.connect(ctx.destination)
      osc.type = 'sine'
      osc.frequency.value = freq
      gain.gain.setValueAtTime(0, startTime)
      gain.gain.linearRampToValueAtTime(0.35, startTime + 0.015)
      gain.gain.exponentialRampToValueAtTime(0.001, startTime + duration)
      osc.start(startTime)
      osc.stop(startTime + duration)
    }
    const now = ctx.currentTime
    playNote(880, now, 0.5)
    playNote(1108, now + 0.28, 0.5)
  }

  
  if (ctx.state === 'suspended') {
    ctx.resume().then(doPlay).catch(() => {})
  } else {
    doPlay()
  }
}

onMounted(() => {
  initWebSocket()
  initAudioContext()
})

onBeforeUnmount(() => {
  closeWebSocket()
  document.removeEventListener('click', resumeAudioOnce)
  if (audioCtx) {
    audioCtx.close().catch(() => {})
    audioCtx = null
  }
})

</script>

<style lang="scss" scoped>

.layout-container {
  height: 100vh;
  background: #f5f7fa;
}

.sidebar {
  background: linear-gradient(180deg, #1a202c 0%, #2d3748 100%);
  overflow: hidden;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.1);
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  }
}

.logo {
  height: 70px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: rgba(0, 0, 0, 0.2);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);

  .logo-icon {
    width: 45px;
    height: 45px;
    background: linear-gradient(135deg, #2db87a 0%, #1a9c63 100%);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    box-shadow: 0 4px 15px rgba(45, 184, 122, 0.4);

    .el-icon {
      font-size: 22px;
      color: #fff;
    }
  }

  .logo-text {
    flex: 1;

    h2 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #fff;
      line-height: 1.2;
    }

    p {
      margin: 2px 0 0 0;
      font-size: 12px;
      color: rgba(255, 255, 255, 0.6);
    }
  }
}

.sidebar-menu {
  border: none;
  padding: 10px 0;

  :deep(.el-menu-item) {
    height: 50px;
    line-height: 50px;
    margin: 4px 12px;
    border-radius: 10px;
    transition: all 0.3s ease;

    .el-icon {
      font-size: 18px;
      margin-right: 10px;
      width: 20px;
    }

    span {
      font-size: 14px;
      font-weight: 500;
    }

    &:hover {
      background: rgba(45, 184, 122, 0.1) !important;
      color: #2db87a !important;
      transform: translateX(4px);
    }

    &.is-active {
      background: linear-gradient(90deg, rgba(45, 184, 122, 0.2), rgba(45, 184, 122, 0.1)) !important;
      color: #2db87a !important;
      font-weight: 600;
      position: relative;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 4px;
        height: 60%;
        background: linear-gradient(180deg, #2db87a, #1a9c63);
        border-radius: 0 4px 4px 0;
      }
    }
  }
}

.header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-bottom: 1px solid #e8eaed;
  position: relative;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;

  .header-menu-icon {
    font-size: 20px;
    color: #2db87a;
    cursor: pointer;
    transition: transform 0.3s;

    &:hover {
      transform: scale(1.1);
    }
  }

  .header-title {
    font-size: 18px;
    font-weight: 600;
    color: #2d3748;
    letter-spacing: 0.5px;
  }
}

.header-right {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 15px;
  border-radius: 25px;
  transition: all 0.3s ease;

  &:hover {
    background: #f7fafc;
  }

  .user-avatar {
    width: 36px;
    height: 36px;
    background: linear-gradient(135deg, #2db87a 0%, #1a9c63 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 8px rgba(45, 184, 122, 0.3);

    .el-icon {
      color: #fff;
      font-size: 18px;
    }
  }

  .user-name {
    font-size: 14px;
    font-weight: 500;
    color: #2d3748;
  }

  .el-icon:last-child {
    font-size: 12px;
    color: #a0aec0;
    transition: transform 0.3s;
  }

  &:hover .el-icon:last-child {
    transform: translateY(2px);
  }
}

:deep(.user-dropdown) {
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border: none;
  padding: 8px 0;

  .el-dropdown-menu__item {
    padding: 12px 20px;
    font-size: 14px;
    transition: all 0.3s;

    .el-icon {
      margin-right: 8px;
      color: #2db87a;
    }

    &:hover {
      background: #f7fafc;
      color: #2db87a;
    }
  }
}

.main-content {
  background: #f5f7fa;
  padding: 24px;
  overflow-y: auto;
}

</style>
