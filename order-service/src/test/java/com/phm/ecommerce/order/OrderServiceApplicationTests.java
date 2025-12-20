package com.phm.ecommerce.order;

import com.phm.ecommerce.order.support.TestContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"spring.main.allow-bean-definition-overriding=true"
})
class OrderServiceApplicationTests extends TestContainerSupport {

	@Test
	void contextLoads() {
	}

}
