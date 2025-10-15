package com.aruba.customeranalysis;

import org.springframework.boot.SpringApplication;

public class TestCustomerAnalysisApplication {

	public static void main(String[] args) {
		SpringApplication.from(CustomerAnalysisApplication::main).run(args);
	}

}
