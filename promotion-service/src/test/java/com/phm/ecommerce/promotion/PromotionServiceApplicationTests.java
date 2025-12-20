package com.phm.ecommerce.promotion;

import com.phm.ecommerce.promotion.support.TestContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"spring.main.allow-bean-definition-overriding=true"
})
class PromotionServiceApplicationTests extends TestContainerSupport {

	@Test
	void contextLoads() {
	}

}
