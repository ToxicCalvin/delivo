import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi } from '@/api/user'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: null
  }),
  getters: {
    isAdmin: (state) => {
      const username = (state.userInfo?.username || state.userInfo?.userName || '').toLowerCase()
      return username === 'admin'
    }
  },
  actions: {
    async login(userInfo) {
      const { username, password } = userInfo
      const response = await loginApi({ username: username.trim(), password })
      const data = response.data
      this.token = data.token
      this.userInfo = { ...data, username: data.username || data.userName || '' }
      setToken(data.token)
    },
    async logout() {
      try { await logoutApi() } catch {}
      this.token = ''
      this.userInfo = null
      removeToken()
    }
  }
})
