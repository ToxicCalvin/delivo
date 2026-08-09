const LANGUAGE_KEY = 'language'

export function getLanguage() {
  return localStorage.getItem(LANGUAGE_KEY) || 'en' 
}

export function setLanguage(lang) {
  localStorage.setItem(LANGUAGE_KEY, lang)
  return lang
}

