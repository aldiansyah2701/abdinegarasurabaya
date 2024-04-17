package com.abdinegara.surabaya.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Setter
@Getter
@Entity(name= "pembelian_ujian")
@Table(name= "pembelian_ujian")
public class PembelianUjian extends BaseEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    @Column(name = "ujian_uuid")
    private String ujianUuid;

    @Column(name = "user_uuid")
    private String userUuid;

    @Column(name = "status")
    private String status;

    @Column(name = "approval")
    private String approval;

    @Column(name = "approve_by")
    private String approveBy;

    @Column(name = "remark")
    private String remark;

    @Column(name = "file_path")
    private String filePath;


}
