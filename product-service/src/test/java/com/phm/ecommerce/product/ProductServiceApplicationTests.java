package com.phm.ecommerce.product;

import com.phm.ecommerce.product.support.TestContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"spring.main.allow-bean-definition-overriding=true"
})
class ProductServiceApplicationTests extends TestContainerSupport {

	@Test
	void contextLoads() {
	}

}
