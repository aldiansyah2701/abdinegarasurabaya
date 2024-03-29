package com.abdinegara.surabaya.repository;

import com.abdinegara.surabaya.entity.SoalGanjilGenap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SoalGanjilGenapRepository extends CrudRepository<SoalGanjilGenap,String> {

    Optional<SoalGanjilGenap> findByNamaSoal(String namaSoal);
    Page<SoalGanjilGenap> findAll(Pageable pageable);

}
