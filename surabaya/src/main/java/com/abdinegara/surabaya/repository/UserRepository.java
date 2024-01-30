package com.abdinegara.surabaya.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.User;


@Repository
public interface UserRepository extends CrudRepository<User, String>{
	User findByName(String name);

}
