package com.abdinegara.surabaya.repository;

import com.abdinegara.surabaya.entity.JawabanSiswa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JawabanSiswaRepository extends CrudRepository<JawabanSiswa, String> {

    Page<JawabanSiswa> findAll(Pageable pageable);

    Optional<JawabanSiswa> findByUjianUuidAndSoalUuidAndUserUuidAndSoalType(String ujianUuid, String soalUuid, String userUuid, String soalType);

    List<JawabanSiswa> findByUjianUuidAndUserUuid(String ujianUuid, String userUuid);
}
