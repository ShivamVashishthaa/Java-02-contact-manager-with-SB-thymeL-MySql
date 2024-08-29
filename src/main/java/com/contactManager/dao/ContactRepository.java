package com.contactManager.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
  
import com.contactManager.entity.Contact;
import com.contactManager.entity.User;

public interface ContactRepository extends JpaRepository<Contact, Integer>{
	@Query("from Contact c where c.user.id = :userId")
	public  List<Contact> findContactByUserId(@Param("userId") int userId);
	
// pagination
	
	@Query("from Contact c where c.user.id = :userId")
//	currentPage-page 
//	contact per page - 5
	public  Page<Contact> findContactByUser(@Param("userId") int userId, Pageable pe);
	
//	Pageable need only two info 1 contact per page 2 current page
	
	
//	search bar
	
	public List<Contact> findByNameContainingAndUser(String keywords, User user);

}
