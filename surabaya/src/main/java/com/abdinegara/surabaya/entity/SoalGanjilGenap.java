package com.abdinegara.surabaya.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Setter
@Getter
@Entity(name= "soal_ganjil_genap")
@Table(name= "soal_ganjil_genap")
public class SoalGanjilGenap extends BaseEntity{

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    @Column(name = "nama_soal")
    private String namaSoal;

    @Column(name = "durasi")
    private String durasi;

    @Column(name = "soal")
    private String soal;

    @Column(name = "jawaban")
    private String jawaban;

    @Column(name = "deskripsi")
    private String deskripsi;

    @Column(name = "jenis")
    private String jenis;

    @Transient
    private String nilai;
}
