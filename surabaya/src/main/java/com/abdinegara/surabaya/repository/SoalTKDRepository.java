package com.abdinegara.surabaya.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.SoalTKD;

@Repository
public interface SoalTKDRepository extends CrudRepository<SoalTKD, String>{
	
	Optional<SoalTKD> findByNamaSoal(String namaSoal);
	Page<SoalTKD> findAll(Pageable pageable);

}
