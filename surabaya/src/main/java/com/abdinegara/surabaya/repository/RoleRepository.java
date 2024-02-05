package com.abdinegara.surabaya.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.ModelUserAndRoles;
import com.abdinegara.surabaya.entity.Role;
import com.abdinegara.surabaya.entity.User;


@Repository
public interface RoleRepository extends CrudRepository<Role, String> {
	
	void deleteByUser(User user);
	
	@Query(nativeQuery = true, value = "select u.name, u.uuid as uuid, GROUP_CONCAT(r.name separator ';') as roles from roles r join users u on r.user_uuid = u.uuid group by u.uuid")
	List<ModelUserAndRoles> getAllUserAndRoles();
	
	@Query(nativeQuery = true, value = "select u.name, u.uuid as uuid, u.password_data as passwordData, GROUP_CONCAT(r.name separator ';') as roles from roles r join users u on r.user_uuid = u.uuid where u.name = :name group by r.user_uuid")
	ModelUserAndRoles getUserAndRoles(@Param("name") String name);
}
