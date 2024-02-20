package com.abdinegara.surabaya.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.SoalPilihanGanda;

@Repository
public interface SoalPilihanGandaRepository extends CrudRepository<SoalPilihanGanda, String>{
	
	Optional<SoalPilihanGanda> findByNamaSoal(String namaSoal);
	Page<SoalPilihanGanda> findAll(Pageable pageable);
}
