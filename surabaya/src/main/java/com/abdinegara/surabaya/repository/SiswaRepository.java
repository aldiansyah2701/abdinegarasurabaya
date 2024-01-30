package com.abdinegara.surabaya.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.abdinegara.surabaya.entity.Siswa;

@Repository
public interface SiswaRepository extends CrudRepository<Siswa, String>{

}
