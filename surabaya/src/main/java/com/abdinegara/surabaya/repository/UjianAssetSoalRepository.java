package com.abdinegara.surabaya.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.UjianAssetSoal;

@Repository
public interface UjianAssetSoalRepository extends CrudRepository<UjianAssetSoal, String>{
	
	List<UjianAssetSoal> findByUuidUjian(String uuidUjian);
	
	void deleteByUuidUjian(String uuidUjian);
	
	void deleteBySoalTypeAndUuidUjian(String soalType, String uuidUjian);

}
