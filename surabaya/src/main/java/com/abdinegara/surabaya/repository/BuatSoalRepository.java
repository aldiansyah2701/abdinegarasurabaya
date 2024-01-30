package com.abdinegara.surabaya.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.BuatSoal;

@Repository
public interface BuatSoalRepository extends CrudRepository<BuatSoal, String>{

}
