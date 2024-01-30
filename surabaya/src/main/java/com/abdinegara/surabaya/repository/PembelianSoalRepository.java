package com.abdinegara.surabaya.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.PembelianSoal;

@Repository
public interface PembelianSoalRepository extends CrudRepository<PembelianSoal, String>{

}
