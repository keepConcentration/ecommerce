package com.phm.ecommerce.payment;

import com.phm.ecommerce.payment.support.TestContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"spring.main.allow-bean-definition-overriding=true"
})
class PaymentServiceApplicationTests extends TestContainerSupport {

	@Test
	void contextLoads() {
	}

}
