package com.falcon.booking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {"MAIL_USERNAME=test", "MAIL_PASSWORD=test"})
@ActiveProfiles("tests")
class FalconBookingSystemApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
