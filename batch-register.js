const axios = require('axios');

// 配置
const config = {
  baseURL: 'http://localhost:8080', // 后端服务地址
  userCount: 10, // 要注册的用户数量
  password: '123456', // 所有用户的默认密码
  emailSuffix: '@example.com' // 邮箱后缀
};

// 创建axios实例
const api = axios.create({
  baseURL: config.baseURL
});

// 生成随机用户名
function generateUsername(index) {
  return `user${index}`;
}

// 生成邮箱
function generateEmail(username) {
  return `${username}${config.emailSuffix}`;
}

// 注册单个用户
async function registerUser(username, password, email) {
  try {
    const response = await api.post('/api/auth/register', 
      new URLSearchParams({
        username,
        password,
        email
      }),
      {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      }
    );
    return { success: true, message: '注册成功', username };
  } catch (error) {
    return { 
      success: false, 
      message: error.response?.data?.message || '注册失败', 
      username 
    };
  }
}

// 批量注册用户
async function batchRegister() {
  console.log('开始批量注册用户...');
  console.log(`配置: 注册 ${config.userCount} 个用户`);
  console.log('====================================');
  
  let successCount = 0;
  let failureCount = 0;
  const results = [];
  
  for (let i = 1; i <= config.userCount; i++) {
    const username = generateUsername(i);
    const email = generateEmail(username);
    
    console.log(`正在注册用户: ${username} (${email})`);
    
    const result = await registerUser(username, config.password, email);
    results.push(result);
    
    if (result.success) {
      successCount++;
      console.log(`✓ ${username} 注册成功`);
    } else {
      failureCount++;
      console.log(`✗ ${username} 注册失败: ${result.message}`);
    }
    
    // 避免请求过于频繁，添加小延迟
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  
  console.log('====================================');
  console.log('批量注册完成!');
  console.log(`成功: ${successCount} 个`);
  console.log(`失败: ${failureCount} 个`);
  
  if (failureCount > 0) {
    console.log('失败详情:');
    results.filter(r => !r.success).forEach(r => {
      console.log(`- ${r.username}: ${r.message}`);
    });
  }
  
  console.log('====================================');
  console.log('批量注册脚本执行完毕');
}

// 检查是否安装了axios
try {
  require('axios');
} catch (e) {
  console.error('错误: 请先安装axios库');
  console.error('执行命令: npm install axios');
  process.exit(1);
}

// 执行批量注册
batchRegister().catch(err => {
  console.error('执行过程中发生错误:', err);
  process.exit(1);
});