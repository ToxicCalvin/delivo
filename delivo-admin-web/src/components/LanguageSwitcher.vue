<template>
  <div class="language-switcher">
    <el-button
      size="small"
      :type="currentLang === 'en' ? 'primary' : 'default'"
      @click="switchLanguage('en')"
    >
      English
    </el-button>
    <el-button
      size="small"
      :type="currentLang === 'zh' ? 'primary' : 'default'"
      @click="switchLanguage('zh')"
    >
      中文
    </el-button>
  </div>
</template>

<script setup>

import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { getLanguage, setLanguage } from '@/utils/lang'

const { locale } = useI18n()
const currentLang = ref(getLanguage())

watch(locale, (newLang) => {
  currentLang.value = newLang
})

function switchLanguage(lang) {
  currentLang.value = lang
  setLanguage(lang)
  locale.value = lang
}

</script>

<style scoped>

.language-switcher {
  display: flex;
  gap: 8px;
  margin-right: 20px;
}

</style>
