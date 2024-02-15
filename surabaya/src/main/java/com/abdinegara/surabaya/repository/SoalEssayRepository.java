package com.abdinegara.surabaya.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.SoalEssay;

@Repository
public interface SoalEssayRepository extends CrudRepository<SoalEssay, String>{
	Optional<SoalEssay> findByNamaSoal(String namaSoal);
}
