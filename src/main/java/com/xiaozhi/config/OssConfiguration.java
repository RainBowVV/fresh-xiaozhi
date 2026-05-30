package com.xiaozhi.config;

import com.xiaozhi.properties.OssProperties;
import com.xiaozhi.util.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OssConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil(OssProperties ossProperties) {
        log.info("创建阿里云OSS工具: endpoint={}, bucket={}", ossProperties.getEndpoint(), ossProperties.getBucketName());
        return new AliOssUtil(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret(),
                ossProperties.getBucketName(),
                ossProperties.getUrlPrefix());
    }
}
