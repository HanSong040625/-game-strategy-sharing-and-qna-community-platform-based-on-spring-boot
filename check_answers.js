const mysql = require('mysql2/promise');

async function checkAnswers() {
    try {
        // 创建数据库连接
        const connection = await mysql.createConnection({
            host: 'localhost',
            user: 'root',
            password: 'root',
            database: 'game_community'
        });

        console.log('连接到数据库成功\n');

        // 查询所有回答
        const [answers] = await connection.execute('SELECT id, content, question_id FROM answer');
        
        console.log(`数据库中共有 ${answers.length} 个回答:`);
        console.log('========================================');
        
        answers.forEach(answer => {
            console.log(`ID: ${answer.id}`);
            console.log(`内容: ${answer.content ? answer.content.substring(0, 50) + '...' : '空'}`);
            console.log(`问题ID: ${answer.question_id}`);
            console.log('----------------------------------------');
        });

        await connection.end();
        
    } catch (error) {
        console.error('数据库连接失败:', error.message);
    }
}

checkAnswers();