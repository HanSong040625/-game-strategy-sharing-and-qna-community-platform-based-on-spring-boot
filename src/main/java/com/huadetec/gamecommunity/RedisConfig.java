 //redis配置类
package com.huadetec.gamecommunity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
@Configuration//标记一个类作为Spring的配置类，用于定义Bean和配置信息
public class RedisConfig {
    @Bean//标记一个方法，改方法返回的对象将被注册为Spring容器中的Bean，在配置类中定义Bean
    public RedisTemplate<String , Object> redisTemplate(RedisConnectionFactory connectionFactory){
        //RedisTemplate<String , Object> 是redis的核心模版类，用于操作redis数据库
        //string：redis键的类型；object：redis值的类型
        //RedisConnectionFactory负责创建和管理redis连接
        RedisTemplate<String , Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);//设置redis连接工厂，参数是RedisConnectionFactory类型的连接工厂实例，告诉redistemplate使用哪个连接工厂创建redis连接
        template.setKeySerializer(new StringRedisSerializer());//设置redis键的序列化器，指定如何将java对象序列化为redis键的字节数组
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer()) ;
        //向redis缓存中存储数据时要把java对象转换成字符串，从redis读取缓存时要把json字符串转换成java对象，而GenericJackson2JsonRedisSerializer()能在json字符串转换成java对象时保留java对象的原始类型，所以不会报错
        return template ;
    }
}
//这个配置类的作用是创建并配置一个RedisTemplate Bean，使其能够正确地序列化和反序列化Redis的键值对，从而方便地操作Redis数据库。
