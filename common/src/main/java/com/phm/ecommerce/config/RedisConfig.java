package com.phm.ecommerce.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableConfigurationProperties(RedissonProperties.class)
public class RedisConfig {

  private final RedissonProperties redissonProperties;

  public RedisConfig(RedissonProperties redissonProperties) {
    this.redissonProperties = redissonProperties;
  }

  @Bean
  public RedissonClient redissonClient() {
    Config config = new Config();
    config.useSingleServer()
        .setAddress("redis://" + redissonProperties.getHost() + ":" + redissonProperties.getPort())
        .setConnectionPoolSize(redissonProperties.getConnectionPoolSize())
        .setConnectionMinimumIdleSize(redissonProperties.getConnectionMinimumIdleSize())
        .setIdleConnectionTimeout(redissonProperties.getIdleConnectionTimeout())
        .setConnectTimeout(redissonProperties.getConnectTimeout())
        .setTimeout(redissonProperties.getTimeout());
    return Redisson.create(config);
  }

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
    config.setHostName(redissonProperties.getHost());
    config.setPort(redissonProperties.getPort());
    return new LettuceConnectionFactory(config);
  }

  @Bean
  public ObjectMapper redisObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    RecordSupportingTypeResolver typeResolver = new RecordSupportingTypeResolver(
        DefaultTyping.NON_FINAL,
        objectMapper.getPolymorphicTypeValidator());

    StdTypeResolverBuilder initializedResolver = typeResolver.init(JsonTypeInfo.Id.CLASS, null);
    initializedResolver = initializedResolver.inclusion(JsonTypeInfo.As.PROPERTY);
    objectMapper.setDefaultTyping(initializedResolver);

    return objectMapper;
  }

  @Bean
  public StringRedisSerializer stringRedisSerializer() {
    return new StringRedisSerializer();
  }

  @Bean
  public GenericJackson2JsonRedisSerializer jsonRedisSerializer(
      @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
    return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      RedisConnectionFactory connectionFactory,
      StringRedisSerializer stringRedisSerializer,
      GenericJackson2JsonRedisSerializer jsonRedisSerializer) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    template.setKeySerializer(stringRedisSerializer);
    template.setHashKeySerializer(stringRedisSerializer);

    template.setValueSerializer(jsonRedisSerializer);
    template.setHashValueSerializer(jsonRedisSerializer);

    template.afterPropertiesSet();
    return template;
  }
}
