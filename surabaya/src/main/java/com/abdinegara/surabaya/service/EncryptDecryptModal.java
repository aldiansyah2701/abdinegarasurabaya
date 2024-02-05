package com.abdinegara.surabaya.service;

import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.stereotype.Service;

@Service
public class EncryptDecryptModal {
	final String mysecretkey = "axasecretmobile";

	public String setSensitiveData(String sensitiveData) {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(mysecretkey);

		return textEncryptor.encrypt(sensitiveData);
	}

	public String getSensitiveData(String sensitiveData) {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(mysecretkey);

		return textEncryptor.decrypt(sensitiveData);
	}

}
