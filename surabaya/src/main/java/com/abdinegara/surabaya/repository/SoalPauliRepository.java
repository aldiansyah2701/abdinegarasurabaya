package com.abdinegara.surabaya.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.SoalPauli;

@Repository
public interface SoalPauliRepository extends CrudRepository<SoalPauli, String>{

	Optional<SoalPauli> findByNamaSoal(String namaSoal);
}
