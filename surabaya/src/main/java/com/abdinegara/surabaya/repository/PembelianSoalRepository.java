package com.abdinegara.surabaya.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.PembelianSoal;
import com.abdinegara.surabaya.entity.Siswa;

@Repository
public interface PembelianSoalRepository extends CrudRepository<PembelianSoal, String>{

	List<PembelianSoal> findBySiswa(Siswa siswa);
	Page<PembelianSoal> findBySiswa(Siswa siswa, Pageable pageable);
}
