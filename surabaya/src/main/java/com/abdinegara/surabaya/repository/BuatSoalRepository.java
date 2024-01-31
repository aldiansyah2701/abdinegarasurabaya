package com.abdinegara.surabaya.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.BuatSoal;

@Repository
public interface BuatSoalRepository extends CrudRepository<BuatSoal, String>{
	
	Optional<BuatSoal> findByNamaSoalAndJenisSoalAndJenisSiswa(String value1, String value2, String value3);

}
