package com.abdinegara.surabaya.repository;

import com.abdinegara.surabaya.entity.JawabanSiswa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JawabanSiswaRepository extends CrudRepository<JawabanSiswa, String> {

    Page<JawabanSiswa> findAll(Pageable pageable);
}
