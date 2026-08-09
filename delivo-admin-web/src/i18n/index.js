import { createI18n } from 'vue-i18n'
import zh from '@/locales/zh'
import en from '@/locales/en'
import { getLanguage } from '@/utils/lang'

const i18n = createI18n({
  legacy: false,
  locale: getLanguage() || 'en',
  fallbackLocale: 'en',
  messages: { zh, en }
})

export default i18n
