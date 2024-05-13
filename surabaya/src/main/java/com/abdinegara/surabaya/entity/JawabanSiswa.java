package com.abdinegara.surabaya.entity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Setter
@Getter
@Entity(name= "jawaban_siswa")
@Table(name= "jawaban_siswa")
public class JawabanSiswa extends BaseEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    @Column(name = "ujian_uuid")
    private String ujianUuid;

    @Column(name = "soal_uuid")
    private String soalUuid;

    @Column(name = "user_uuid")
    private String userUuid;

    @Column(name = "jawaban")
    private String jawaban;

    @Column(name = "nilai")
    private String nilai;

    @Column(name = "soalType")
    private String soalType;

    @Column(name = "jawaban_soal")
    private String jawabanSoal;

    @Transient
    private String soalName;

    @Transient
    private String ujianName;

}
