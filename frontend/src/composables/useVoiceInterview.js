import { ref, onUnmounted } from 'vue'

export function useVoiceInterview() {
  const speechSupported = ref(typeof window !== 'undefined' && 'speechSynthesis' in window)
  const recognitionSupported = ref(false)
  const listening = ref(false)
  const speaking = ref(false)
  const interimText = ref('')

  let recognition = null

  if (typeof window !== 'undefined') {
    const SR = window.SpeechRecognition || window.webkitSpeechRecognition
    if (SR) {
      recognitionSupported.value = true
      recognition = new SR()
      recognition.lang = 'zh-CN'
      recognition.continuous = false
      recognition.interimResults = true
    }
  }

  const speak = (text) =>
    new Promise((resolve) => {
      if (!speechSupported.value || !text?.trim()) {
        resolve()
        return
      }
      window.speechSynthesis.cancel()
      const utter = new SpeechSynthesisUtterance(text)
      utter.lang = 'zh-CN'
      utter.rate = 1
      speaking.value = true
      utter.onend = () => {
        speaking.value = false
        resolve()
      }
      utter.onerror = () => {
        speaking.value = false
        resolve()
      }
      window.speechSynthesis.speak(utter)
    })

  const stopSpeak = () => {
    if (speechSupported.value) {
      window.speechSynthesis.cancel()
      speaking.value = false
    }
  }

  const listen = () =>
    new Promise((resolve, reject) => {
      if (!recognition) {
        reject(new Error('浏览器不支持语音识别，请改用文字输入'))
        return
      }
      interimText.value = ''
      listening.value = true
      recognition.onresult = (event) => {
        let finalText = ''
        let interim = ''
        for (let i = event.resultIndex; i < event.results.length; i++) {
          const t = event.results[i][0].transcript
          if (event.results[i].isFinal) finalText += t
          else interim += t
        }
        interimText.value = interim || finalText
        if (finalText) {
          listening.value = false
          resolve(finalText.trim())
        }
      }
      recognition.onerror = (e) => {
        listening.value = false
        if (e.error === 'no-speech') reject(new Error('未检测到语音，请重试'))
        else if (e.error === 'not-allowed') reject(new Error('请允许麦克风权限'))
        else reject(new Error('语音识别失败'))
      }
      recognition.onend = () => {
        listening.value = false
      }
      try {
        recognition.start()
      } catch {
        listening.value = false
        reject(new Error('无法启动麦克风'))
      }
    })

  const stopListen = () => {
    if (recognition && listening.value) {
      try {
        recognition.stop()
      } catch {
        /* ignore */
      }
      listening.value = false
    }
  }

  onUnmounted(() => {
    stopSpeak()
    stopListen()
  })

  return {
    speechSupported,
    recognitionSupported,
    listening,
    speaking,
    interimText,
    speak,
    stopSpeak,
    listen,
    stopListen
  }
}
