package com.abdinegara.surabaya.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.SoalEssay;

@Repository
public interface SoalEssayRepository extends CrudRepository<SoalEssay, String>{
	Optional<SoalEssay> findByNamaSoal(String namaSoal);
	Page<SoalEssay> findAll(Pageable pageable);
}
