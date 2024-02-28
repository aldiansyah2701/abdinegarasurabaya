package com.abdinegara.surabaya.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.User;


@Repository
public interface UserRepository extends CrudRepository<User, String>{
	User findByName(String name);
	
	@Query(nativeQuery = true, value = "SELECT u.name FROM users u WHERE u.user_type = :userType")
	List<String> findByUserType(@Param("userType") String userType);

}
