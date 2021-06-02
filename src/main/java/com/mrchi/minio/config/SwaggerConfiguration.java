package com.mrchi.minio.config;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Hope
 * @className: SwaggerConfiguration
 * @Author mrchi
 * @Date 2021-6-1 14:10:12
 * @description: SwaggerConfiguration
 */

@Configuration
@EnableSwagger2
@EnableKnife4j
public class SwaggerConfiguration {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("MinIO 1.0")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.hope.minio.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("MinIO 对象存储服务")
                .description("文件上传系统")
                .termsOfServiceUrl("https://docs.min.io/cn")
                .contact(new Contact("Mrchi","http://www.mrchi.cn","mrchis@126.com"))
                .version("1.0")
                .build();
    }
}