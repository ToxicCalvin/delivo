<template>
  <div class="login-container">
    
    <div class="background-animation">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
    </div>

    
    <div class="login-box">
      <LanguageSwitcher />
      <div class="title">
        <div class="logo-icon">
          <el-icon><Food /></el-icon>
        </div>
        <h2>{{ t('login.title') }}</h2>
        <p class="subtitle">{{ t('login.subtitle') }}</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        auto-complete="on"
        label-position="left"
      >
        <el-form-item prop="username">
          <div class="input-wrapper">
            <span class="svg-container">
              <el-icon><User /></el-icon>
            </span>
            <el-input
              v-model="loginForm.username"
              :placeholder="t('login.usernamePlaceholder')"
              name="username"
              type="text"
              tabindex="1"
              auto-complete="on"
              class="custom-input"
            />
          </div>
        </el-form-item>

        <el-form-item prop="password">
          <div class="input-wrapper">
            <span class="svg-container">
              <el-icon><Lock /></el-icon>
            </span>
            <el-input
              v-model="loginForm.password"
              :type="passwordType"
              :placeholder="t('login.passwordPlaceholder')"
              name="password"
              tabindex="2"
              auto-complete="on"
              class="custom-input"
              @keyup.enter="handleLogin"
            />
            <span class="show-pwd" @click="showPwd">
              <el-icon><View v-if="passwordType === 'password'" /><Hide v-else /></el-icon>
            </span>
          </div>
        </el-form-item>

        <el-button
          :loading="loading"
          type="primary"
          class="login-button"
          @click.prevent="handleLogin"
        >
          <span v-if="!loading">{{ t('login.login') }}</span>
          <span v-else>{{ t('login.logging') }}</span>
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>

import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { nextTick } from 'vue'
import { useUserStore } from '@/store/user'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)
const passwordType = ref('password')
const redirect = ref(undefined)

const loginForm = ref({
  username: 'admin',
  password: '123456'
})

const loginRules = computed(() => ({
  username: [{ required: true, trigger: 'blur', validator: validateUsername }],
  password: [{ required: true, trigger: 'blur', validator: validatePassword }]
}))

watch(
  () => route.query,
  (query) => {
    redirect.value = query && query.redirect
  },
  { immediate: true }
)

function validateUsername(rule, value, callback) {
  if (!value) {
    callback(new Error(t('login.usernameRequired')))
  } else {
    callback()
  }
}

function validatePassword(rule, value, callback) {
  if (!value) {
    callback(new Error(t('login.passwordRequired')))
  } else {
    callback()
  }
}

function showPwd() {
  if (passwordType.value === 'password') {
    passwordType.value = ''
  } else {
    passwordType.value = 'password'
  }
}

function handleLogin() {
  loginFormRef.value.validate(valid => {
    if (valid) {
      loading.value = true
      userStore
        .login(loginForm.value)
        .then(() => {
          router.push({ path: redirect.value || '/' })
          loading.value = false
        })
        .catch(() => {
          loading.value = false
        })
    } else {
      console.log('error submit!!')
      return false
    }
  })
}

</script>

<style lang="scss" scoped>

.login-container {
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #2db87a 0%, #1a9c63 50%, #85e4b3 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}


.background-animation {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.3;
  animation: float 20s infinite ease-in-out;
}

.shape-1 {
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.2);
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.shape-2 {
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.15);
  bottom: -50px;
  right: -50px;
  animation-delay: 5s;
}

.shape-3 {
  width: 350px;
  height: 350px;
  background: rgba(255, 255, 255, 0.1);
  top: 50%;
  right: -150px;
  animation-delay: 10s;
}

.shape-4 {
  width: 250px;
  height: 250px;
  background: rgba(255, 255, 255, 0.2);
  bottom: 20%;
  left: -100px;
  animation-delay: 15s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(50px, -50px) scale(1.1); }
  50% { transform: translate(-30px, 30px) scale(0.9); }
  75% { transform: translate(30px, 50px) scale(1.05); }
}

.login-box {
  width: 420px;
  padding: 50px 45px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3),
              0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  position: relative;
  z-index: 1;
  animation: slideUp 0.6s ease-out;

  :deep(.language-switcher) {
    position: absolute;
    top: 16px;
    right: 16px;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.title {
    text-align: center;
    margin-bottom: 40px;
    position: relative;

  .logo-icon {
    width: 70px;
    height: 70px;
    margin: 0 auto 20px;
    background: linear-gradient(135deg, #2db87a 0%, #1a9c63 100%);
    border-radius: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 10px 30px rgba(45, 184, 122, 0.4);
    animation: pulse 2s ease-in-out infinite;

    .el-icon {
      font-size: 36px;
      color: #fff;
    }
  }

  @keyframes pulse {
    0%, 100% { transform: scale(1); }
    50% { transform: scale(1.05); }
  }

  h2 {
    color: #2c3e50;
    font-size: 28px;
    font-weight: 600;
    margin: 0 0 8px 0;
    letter-spacing: 1px;
  }

  .subtitle {
    color: #7f8c8d;
    font-size: 14px;
    margin: 0;
    font-weight: 400;
  }

}

.login-form {
  .el-form-item {
    margin-bottom: 25px;

    :deep(.el-form-item__content) {
      position: relative;
    }
  }

  .input-wrapper {
    position: relative;
    display: flex;
    align-items: center;
    background: #f8f9fa;
    border-radius: 12px;
    border: 2px solid transparent;
    transition: all 0.3s ease;

    &:hover {
      background: #f0f2f5;
      border-color: rgba(45, 184, 122, 0.3);
    }

    &:focus-within {
      background: #fff;
      border-color: #2db87a;
      box-shadow: 0 0 0 4px rgba(45, 184, 122, 0.1);
    }
  }

  .svg-container {
    padding: 0 15px;
    color: #2db87a;
    font-size: 18px;
    display: flex;
    align-items: center;
    z-index: 1;
  }

  .custom-input {
    flex: 1;

    :deep(.el-input__wrapper) {
      border: none;
      background: transparent;
      box-shadow: none;
      padding: 0;
    }

    :deep(.el-input__inner) {
      border: none;
      background: transparent;
      padding: 14px 15px 14px 0;
      font-size: 15px;
      color: #2c3e50;

      &::placeholder {
        color: #95a5a6;
      }

      &:focus {
        border: none;
      }
    }
  }

  .show-pwd {
    position: absolute;
    right: 15px;
    top: 50%;
    transform: translateY(-50%);
    font-size: 18px;
    color: #95a5a6;
    cursor: pointer;
    user-select: none;
    z-index: 2;
    transition: color 0.3s;

    &:hover {
      color: #2db87a;
    }
  }

  .login-button {
    width: 100%;
    height: 50px;
    margin-top: 10px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, #2db87a 0%, #1a9c63 100%);
    border: none;
    box-shadow: 0 8px 20px rgba(45, 184, 122, 0.4);
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 12px 30px rgba(45, 184, 122, 0.5);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

</style>
