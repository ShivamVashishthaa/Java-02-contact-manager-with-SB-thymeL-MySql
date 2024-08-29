package com.contactManager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.contactManager.dao.ContactRepository;
import com.contactManager.dao.UserRepository;
import com.contactManager.entity.Contact;
import com.contactManager.entity.User;
import com.contactManager.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;


@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired 
	UserRepository userRepository;

	@Autowired
	ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
//	run everytime when we hit any url to add common data
	@ModelAttribute
	public void addCommonData(ModelMap model, Principal principal) {
		String username = principal.getName();

//		get the user details using username
		User user = userRepository.getUserByUserName(username);
		model.put("user", user);
	}

//	
	@GetMapping("/index")
	public String dashboard(ModelMap model) {
		model.put("title", "User - Home");
		return "normal/user_dashboard";
	}

//	open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(ModelMap model) {
		model.put("title", "User - Add Contact");
		model.put("contact", new Contact());
		return "normal/user_addContact_form";
	}

	@PostMapping("/process-contact")
	public String addContactHandler(@Valid @ModelAttribute("contact") Contact contact
			,BindingResult result
			,@RequestParam("profileImage") MultipartFile file
			,ModelMap model
			,Principal principal
			,HttpSession session) {

		try {
			if (result.hasErrors())return "normal/user_addContact_form";
			else {
				
				String username = principal.getName();
				User user = userRepository.getUserByUserName(username);
				
//				Processing and uploading file
				
				if(file.isEmpty()) {
					contact.setImage("contact.png");
				}
				else {
//					Update the file name to contact
					contact.setImage(file.getOriginalFilename());
					
//					Save the file to the folder and 
					File saveFile = new ClassPathResource("static/img").getFile();
					
					Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
					
					System.out.println("image is uploaded");
				}
				 
				contact.setUser(user);
				user.getContacts().add(contact);
				System.out.println(contact.getUser());
				this.userRepository.save(user);
				session.setAttribute("message", new Message("Your contact is added", "alert-success"));
				return "normal/user_addContact_form";
			}
			

		} catch (Exception e) {
			System.out.println("Error : " + e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Try again!!", "alert-danger"));
			return "normal/user_addContact_form";
		}
	}
	
	@GetMapping("/view-contact/{page}")
	public String showContacts(@PathVariable("page") Integer page,ModelMap model
			, Principal principal) {
		
		model.put("title", "User - View contacts");
		
		try {
			String username = principal.getName();
			User user = userRepository.getUserByUserName(username);
			
//			creating pageable object and give it to repo
			Pageable pageable = PageRequest.of(page, 5);
			
//			System.out.println(pageable.getPageNumber());
			
			Page<Contact> contacts = contactRepository.findContactByUser(user.getId(),pageable);
			
			model.put("contacts", contacts);
			model.put("currentPage", page);
			model.put("totalPages", contacts.getTotalPages());
//			System.out.println(contacts.getTotalPages());
			
		} catch (Exception e) {
			System.out.println("Error : " + e.getMessage());
			e.printStackTrace(); 
		}
		
		return "normal/user_show_contact";
	}
	
	@GetMapping("/{cid}/contact")
	public String contactDetail(@PathVariable int cid 
			, ModelMap model
			,Principal principal) {
		try {
			Optional<Contact> contactOptionl = this.contactRepository.findById(cid);
			Contact contact = contactOptionl.get();
			String username = principal.getName();
			User user = userRepository.getUserByUserName(username);
			if (user.getId() == contact.getUser().getId())model.put("contact", contact);
			
		} catch (Exception e) {
			System.out.println("No such contact found");
			model.put("exception", "No such contact is available");
		}
		
		return "normal/contact_detail";
	}

	@RequestMapping("delete/{cid}/contact")
	public String deleteById(@PathVariable int cid 
			, ModelMap model
			,Principal principal) {
//		we have to unlink the contact from user before delete because we use cascade all while mappling
		try {
			Contact contact = this.contactRepository.findById(cid).get();
			contact.setUser(null);
			String username = principal.getName();
			System.out.println(username);
//			User user = this.userRepository.getUserByUserName(username);
//			System.out.println(user);
			this.contactRepository.delete(contact);
			System.out.println("deleted");
//			if (user.getId() == contact.getUser().getId())model.put("contact", contact);
			
		} catch (Exception e) {
			System.out.println("No such contact found");
			model.put("exception", "No such contact is available");
		}
		return "redirect:/user/view-contact/0";
	}
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, ModelMap model) {	
		model.put("title", "User - update Contact");
		Contact contact = contactRepository.findById(cid).get();
		model.put("contact", contact);
		return "normal/user_update_form";
	}
	
	@PostMapping("/process-update")
	public String postMethodName(@ModelAttribute("contact") Contact contact
				,ModelMap model
				,Principal principal
				,@RequestParam("profileImage") MultipartFile file) {
		try {
			Contact oldContact = contactRepository.findById(contact.getCid()).get();
			System.out.println(oldContact.getImage());
			if(!file.isEmpty()) {
//				delete old image
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file2 = new File(deleteFile,oldContact.getImage());
				file2.delete();
				
//				update new image
//				Save the file to the folder and 
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());	
			}else {
	
				contact.setImage(oldContact.getImage());
			}
				
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
//	Open Setting Handler
	
	@GetMapping("/settings")
	public String openSetting(ModelMap model) {
		model.put("title", "User - Settings");
		return "normal/user_settings";
	}
	
	@PostMapping("/change-password")
	public String postMethodName(@RequestParam("oldPassword") String oldPassword
			,@RequestParam("newPassword") String newPassword
			,Principal principal) {
		
		System.out.println("oldPassword "+oldPassword);
		System.out.println("newPassword "+newPassword);
		
		User user = userRepository.getUserByUserName(principal.getName());
		
		if (this.bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
			//change the password
			user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(user);
		} else {
			//error.....
			return "redirect:/user/settings";
		}
		
		
		return "redirect:/user/index";
	}

	

}


//UsernamePasswordAuthenticationToken 
//[Principal=com.contactManager.config.CustomUserDetails@198a6109, 
//	Credentials=[PROTECTED], 
//	Authenticated=true, 
//	Details=WebAuthenticationDetails 
//		[RemoteIpAddress=0:0:0:0:0:0:0:1,SessionId=F66C4B92049139E1526A96342B8BBA36], 
//	Granted Authorities=[ROLE_ADMIN]
//]

// ------------------  pagination------------------

//important
//
//backend work: we send data inside list in number like if database have 10 contact then we only send 5 contact i.e we show only 5 contact per page.
//
//example per page = [n] per page n contacts 
//current page is 0 [page] we take page number via @PathVariable
// we have to get contact in bundle of 5 via contact repo to do that we have a interface page.
//page is a sublist of a list of objects. It allows gain information about the position of it in the containing entire list.
// go to contact repo

//Now go to front end to manage pagination

