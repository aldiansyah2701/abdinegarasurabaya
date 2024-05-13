package com.abdinegara.surabaya.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.Siswa;

import java.util.List;

@Repository
public interface SiswaRepository extends CrudRepository<Siswa, String>{
	
	Page<Siswa> findByTitleAndUserUuidNotNull(String title, Pageable pageable);

	Siswa findByUserUuid(String userUuid);

	@Query(nativeQuery = true, value = "select count(*) from siswa")
	Integer findCountSiswa();

	@Query(nativeQuery = true, value = "SELECT COUNT(DISTINCT user_uuid) AS data FROM pembelian_ujian")
	Integer findCountPembelianUjian();

}
