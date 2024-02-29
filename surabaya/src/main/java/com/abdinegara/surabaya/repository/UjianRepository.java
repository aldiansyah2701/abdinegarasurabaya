package com.abdinegara.surabaya.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.Ujian;

@Repository
public interface UjianRepository extends CrudRepository<Ujian, String>{

	Page<Ujian> findAll(Pageable pageable);
	Optional<Ujian> findByNamaUjian(String namaUjian);
}
