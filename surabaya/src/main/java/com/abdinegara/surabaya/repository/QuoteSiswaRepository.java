package com.abdinegara.surabaya.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.QuoteSiswa;

@Repository
public interface QuoteSiswaRepository extends CrudRepository<QuoteSiswa, String>{

}
