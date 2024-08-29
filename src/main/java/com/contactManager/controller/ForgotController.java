package com.contactManager.controller;

import java.util.Random;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

//@Controller
public class ForgotController {

	Random random = new Random(1000);
	
	@RequestMapping("/forgot")
	public String fun() {
		
		return "forgot_email_form";
	}
	@PostMapping("/send-otp")
	public String postMethodName(@RequestParam("email") String email) {
		System.out.println("Email" + email);
		
		//generate otp of 4 digit
		
		int otp = random.nextInt(9999);
		System.out.println("otp "+otp);
		
		//write code for sending otp to email...
		
		
		
		return "verify_otp";
	}
	
}
