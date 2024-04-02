package com.abdinegara.surabaya.repository;

import com.abdinegara.surabaya.entity.SoalHilang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SoalHilangRepository extends CrudRepository<SoalHilang, String> {

    Optional<SoalHilang> findByNamaSoal(String namaSoal);
    Page<SoalHilang> findAll(Pageable pageable);
}
