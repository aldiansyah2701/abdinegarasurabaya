package com.abdinegara.surabaya.entity;
import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity(name="roles")
@Table(name="roles")
public class Role extends BaseEntity  {
	
	public enum ROLE{
		ADMIN, SUPERVISOR, CUSTOMERSERVICE, SISWA
	}
	
	@Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid",
            strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
	
	@Column
	private String name;
	
	@JoinColumn(name= "user_uuid")
	@ManyToOne
	private User user;

}
