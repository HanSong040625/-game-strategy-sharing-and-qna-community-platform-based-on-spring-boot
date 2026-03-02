<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <h1 class="not-found-title">404</h1>
      <p class="not-found-message">抱歉，您访问的页面不存在</p>
      <router-link to="/home" class="back-home-button">返回首页</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotFound',
  mounted() {
    // 更改页面标题
    document.title = '页面不存在 - 游戏社区'
  }
}
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.not-found-content {
  text-align: center;
  padding: 40px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.not-found-title {
  font-size: 72px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}

.not-found-message {
  font-size: 18px;
  color: #666;
  margin-bottom: 30px;
}

.back-home-button {
  display: inline-block;
  padding: 10px 20px;
  background-color: #42b983;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.back-home-button:hover {
  background-color: #3aa876;
}
</style>