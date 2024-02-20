package com.abdinegara.surabaya.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.QuoteSiswa;

@Repository
public interface QuoteSiswaRepository extends CrudRepository<QuoteSiswa, String>{
	
	Optional<QuoteSiswa> findByTitle(String title);
	Page<QuoteSiswa> findAll(Pageable pageable);
	

}
