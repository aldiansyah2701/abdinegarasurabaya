package com.abdinegara.surabaya;

import net.bytebuddy.implementation.bind.MethodDelegationBinder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SurabayaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SurabayaApplication.class, args);


		System.out.println(checkdata("data"));
	}



	static boolean checkdata(String data){

		return false;
	}
}

