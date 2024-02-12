package com.abdinegara.surabaya.controller.soal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.abdinegara.surabaya.message.RequestQuote;
import com.abdinegara.surabaya.service.SoalService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/soal")
public class SoalController {
	
	@Autowired
	private SoalService soalService;
	
	@PostMapping(path = "/upload", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE } )
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> getQuote(
			@RequestParam("namaSoal") String namaSoal,
			@RequestParam("jenisSoal") String jenisSoal,
			@RequestParam("jenisSiswa") String jenisSiswa,
			@RequestParam(name = "file", required = true) MultipartFile files) {
		return soalService.createSoal(namaSoal, jenisSoal, jenisSiswa, files);
	}

}
