package com.phm.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("E-commerce API")
                .description(
                    """
                                이커머스 플랫폼 API 명세서

                                ## 주요 기능
                                - 상품 조회 및 인기 상품 통계
                                - 장바구니 관리
                                - 주문 생성 및 결제 (포인트, 쿠폰)
                                - 선착순 쿠폰 발급
                                - 포인트 충전 및 거래 내역
                                """)
                .version("1.0.0")
                .contact(new Contact().name("API Support").email("phm543@gmail.com")))
        .servers(List.of(new Server().url("http://localhost:8085").description("로컬 개발 서버")));
  }
}
