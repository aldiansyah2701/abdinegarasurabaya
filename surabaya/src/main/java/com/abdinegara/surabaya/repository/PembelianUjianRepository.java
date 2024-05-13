package com.abdinegara.surabaya.repository;

import com.abdinegara.surabaya.entity.PembelianSoal;
import com.abdinegara.surabaya.entity.PembelianUjian;
import com.abdinegara.surabaya.entity.Siswa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PembelianUjianRepository extends CrudRepository<PembelianUjian, String> {

    List<PembelianUjian> findByUserUuid(String userUuid);
    Page<PembelianUjian> findByUserUuid(String userUuid, Pageable pageable);
    Page<PembelianUjian> findAll(Pageable pageable);
    Optional<PembelianUjian> findByUjianUuidAndUserUuid(String ujianUuid, String userUuid);

    Optional<PembelianUjian> findByUjianUuid(String ujianUuid);
    Page<PembelianUjian> findByApproval(String approval, Pageable pageable);

    List<PembelianUjian> findAll();

}
