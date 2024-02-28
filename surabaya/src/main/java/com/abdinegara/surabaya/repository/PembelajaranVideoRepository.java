package com.abdinegara.surabaya.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.PembelajaranVideo;

@Repository
public interface PembelajaranVideoRepository extends CrudRepository<PembelajaranVideo, String>{
	
	Page<PembelajaranVideo> findAll(Pageable pageable);

}
