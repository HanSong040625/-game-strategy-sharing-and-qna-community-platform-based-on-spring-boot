<template>
  <div class="ai-chat-container">
    <div class="chat-header">
      <h3>游戏AI助手</h3>
      <p class="subtitle">有什么游戏问题可以问我哦！</p>
    </div>
    <div class="chat-messages" ref="messagesContainer">
      <div 
        v-for="(message, index) in messages" 
        :key="index" 
        :class="['message', message.role]"
      >
        <div class="message-content">{{ message.content }}</div>
      </div>
      <div v-if="loading" class="message ai">
        <div class="message-content">
          <div class="loading">AI思考中...</div>
        </div>
      </div>
    </div>
    <div class="chat-input">
      <textarea 
        v-model="inputQuestion" 
        placeholder="输入游戏相关问题..."
        @keyup.enter.exact="sendMessage"
        :disabled="loading"
      ></textarea>
      <button @click="sendMessage" :disabled="loading || !inputQuestion.trim()">
        {{ loading ? '发送中...' : '发送' }}
      </button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      messages: [
        {
          role: 'ai',
          content: '你好！我是游戏AI助手，有什么游戏相关的问题可以问我哦！'
        }
      ],
      inputQuestion: '',
      loading: false,
      context: []
    };
  },
  methods: {
    sendMessage() {
      if (!this.inputQuestion.trim() || this.loading) return;
      
      const question = this.inputQuestion.trim();
      // 添加用户消息
      this.messages.push({ role: 'user', content: question });
      this.inputQuestion = '';
      this.loading = true;
      
      // 调用后端API
      axios.post('/api/ai/ask', {
        question,
        context: this.context
      })
      .then(response => {
        if (response.data.success) {
          // 添加AI回答
          this.messages.push({ role: 'ai', content: response.data.data.answer });
          // 更新上下文（保留最近3条对话）
          this.context = [
            ...this.context.slice(-2),
            `用户: ${question}`,
            `AI: ${response.data.data.answer}`
          ];
        } else {
          this.messages.push({ role: 'ai', content: response.data.message });
        }
      })
      .catch(error => {
        console.error('API调用失败:', error);
        this.messages.push({ role: 'ai', content: '抱歉，服务暂时不可用，请稍后再试。' });
      })
      .finally(() => {
        this.loading = false;
        // 滚动到底部
        this.$nextTick(() => {
          const container = this.$refs.messagesContainer;
          container.scrollTop = container.scrollHeight;
        });
      });
    }
  },
  mounted() {
    // 初始滚动到底部
    this.$nextTick(() => {
      const container = this.$refs.messagesContainer;
      container.scrollTop = container.scrollHeight;
    });
  }
};
</script>

<style scoped>
.ai-chat-container {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.chat-header {
  background-color: #1976d2;
  color: white;
  padding: 15px;
  text-align: center;
}

.chat-header h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
}

.chat-header .subtitle {
  margin: 0;
  font-size: 12px;
  opacity: 0.9;
}

.chat-messages {
  height: 400px;
  overflow-y: auto;
  padding: 15px;
  background-color: #f9f9f9;
}

.message {
  margin-bottom: 15px;
  max-width: 80%;
}

.message.user {
  margin-left: auto;
  text-align: right;
}

.message.ai {
  margin-right: auto;
  text-align: left;
}

.message-content {
  padding: 10px 15px;
  border-radius: 18px;
  word-wrap: break-word;
  font-size: 14px;
  line-height: 1.4;
}

.message.user .message-content {
  background-color: #e3f2fd;
  color: #1976d2;
  border-bottom-right-radius: 4px;
}

.message.ai .message-content {
  background-color: white;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.loading {
  display: inline-block;
  animation: pulse 1.5s infinite;
  font-style: italic;
  color: #666;
}

@keyframes pulse {
  0% { opacity: 0.6; }
  50% { opacity: 1; }
  100% { opacity: 0.6; }
}

.chat-input {
  display: flex;
  padding: 15px;
  border-top: 1px solid #ddd;
  background-color: white;
}

.chat-input textarea {
  flex: 1;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 20px;
  resize: none;
  height: 60px;
  margin-right: 10px;
  font-size: 14px;
  font-family: inherit;
}

.chat-input textarea:focus {
  outline: none;
  border-color: #1976d2;
  box-shadow: 0 0 0 2px rgba(25, 118, 210, 0.2);
}

.chat-input textarea:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.chat-input button {
  padding: 0 24px;
  border: none;
  border-radius: 20px;
  background-color: #1976d2;
  color: white;
  cursor: pointer;
  font-weight: bold;
  font-size: 14px;
  transition: background-color 0.3s;
}

.chat-input button:hover:not(:disabled) {
  background-color: #1565c0;
}

.chat-input button:disabled {
  background-color: #bdbdbd;
  cursor: not-allowed;
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>
.ai-chat-container {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.chat-header {
  background-color: #1976d2;
  color: white;
  padding: 15px;
  text-align: center;
}

.chat-header h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
}

.chat-header .subtitle {
  margin: 0;
  font-size: 12px;
  opacity: 0.9;
}

.chat-messages {
  height: 400px;
  overflow-y: auto;
  padding: 15px;
  background-color: #f9f9f9;
}

.message {
  margin-bottom: 15px;
  max-width: 80%;
}

.message.user {
  margin-left: auto;
  text-align: right;
}

.message.ai {
  margin-right: auto;
  text-align: left;
}

.message-content {
  padding: 10px 15px;
  border-radius: 18px;
  word-wrap: break-word;
  font-size: 14px;
  line-height: 1.4;
}

.message.user .message-content {
  background-color: #e3f2fd;
  color: #1976d2;
  border-bottom-right-radius: 4px;
}

.message.ai .message-content {
  background-color: white;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.loading {
  display: inline-block;
  animation: pulse 1.5s infinite;
  font-style: italic;
  color: #666;
}

@keyframes pulse {
  0% { opacity: 0.6; }
  50% { opacity: 1; }
  100% { opacity: 0.6; }
}

.chat-input {
  display: flex;
  padding: 15px;
  border-top: 1px solid #ddd;
  background-color: white;
}

.chat-input textarea {
  flex: 1;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 20px;
  resize: none;
  height: 60px;
  margin-right: 10px;
  font-size: 14px;
  font-family: inherit;
}

.chat-input textarea:focus {
  outline: none;
  border-color: #1976d2;
  box-shadow: 0 0 0 2px rgba(25, 118, 210, 0.2);
}

.chat-input textarea:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.chat-input button {
  padding: 0 24px;
  border: none;
  border-radius: 20px;
  background-color: #1976d2;
  color: white;
  cursor: pointer;
  font-weight: bold;
  font-size: 14px;
  transition: background-color 0.3s;
}

.chat-input button:hover:not(:disabled) {
  background-color: #1565c0;
}

.chat-input button:disabled {
  background-color: #bdbdbd;
  cursor: not-allowed;
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>
