package com.contactManager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.contactManager.dao.UserRepository;
import com.contactManager.entity.User;
import com.contactManager.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncode;
	
	@Autowired
	private UserRepository userRepository;

	@GetMapping("/")
	public String home(ModelMap model) {
		model.put("title", "Home - Contact manager project");
		return "home";
	}

	@GetMapping("/about")
	public String about(ModelMap model) {
		model.put("title", "About - Contact manager project");
		return "about";
	}

	@GetMapping("/signup")
	public String signUp(ModelMap model) {
		model.put("title", "SignUp - Contact manager project");
		model.put("user", new User());
		return "signup";
	}

//	handler for register user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, ModelMap model,
			HttpSession session) {
		System.out.println("in handleer");
		try {
			if (!agreement) {
				System.out.println("you are not agreed with the terms and conditions");
				throw new Exception("you are not agreed with the terms and conditions");
			}
			
			if (result1.hasErrors()) {
				System.out.println("error "+ result1.toString());
				model.put("user", user);
				return "signup";
			}
			
			user.setRole("ROLE_USER"); 
			user.setEnable(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncode.encode(user.getPassword()));

			System.out.println("agreement " + agreement);
			System.out.println("user " + user);
//  user set in data base
			User result = this.userRepository.save(user);

			model.put("user", new User());

			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
 
			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			model.put("user", user);
			session.setAttribute("message", new Message("something went wrong !!" + e.getMessage(), "alert-danger"));
			return "signup";
		}

	}

//	handler for custom login
	@GetMapping("/login")
	public String customLogin(ModelMap model) {
		model.put("title", "Login - Contact manager project");
		return "login";
	}

}
