<template>
  <div class="ai-assistant-page">
    <div class="page-header">
      <h2>游戏AI助手</h2>
      <p>有什么游戏问题都可以问我哦！</p>
    </div>
    <div class="chat-container">
      <AiChat />
    </div>
  </div>
</template>

<script>
import AiChat from '@/components/AiChat.vue';

export default {
  components: {
    AiChat
  }
};
</script>

<style scoped>
.ai-assistant-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  color: #1976d2;
  margin-bottom: 10px;
}

.page-header p {
  color: #666;
  font-size: 16px;
}

.chat-container {
  margin-top: 20px;
}
</style>
