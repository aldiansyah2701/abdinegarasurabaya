package com.abdinegara.surabaya.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.SoalAssetImage;

@Repository
public interface SoalAssetImageRepository extends CrudRepository<SoalAssetImage, String>{
	
    void deleteByUuidSoal(String uuidSoal);
    List<SoalAssetImage> findByUuidSoal(String uuidSoal);
    List<SoalAssetImage> findByUuidSoalAndSoalType(String uuidSoal, String soalType);
    void deleteByUuidSoalAndSoalType(String uuidSoal, String soalType);

}
