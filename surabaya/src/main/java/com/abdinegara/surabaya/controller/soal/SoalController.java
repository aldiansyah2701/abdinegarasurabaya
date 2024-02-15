package com.abdinegara.surabaya.controller.soal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import com.abdinegara.surabaya.message.RequestCreateSoalPauli;
import com.abdinegara.surabaya.message.RequestLogin;
import com.abdinegara.surabaya.message.RequestQuote;
import com.abdinegara.surabaya.service.SoalService;
import com.abdinegara.surabaya.service.SoalService.SOALTYPE;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/soal")
public class SoalController {
	
	@Value("${directory.soal.pilihan.ganda}")
	private String directoryPilihanGanda;
	
	@Value("${directory.soal.essay}")
	private String directoryEssay;
	
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
	
	@PostMapping(path = "/pilihan/ganda", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE } )
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> createSoalPilihanGanda(
			@RequestParam("namaSoal") String namaSoal,
			@RequestParam("durasi") String durasi,
			@RequestParam("jawaban") String jawaban,
			@RequestParam("deskripsi") String deskripsi,
			@RequestParam(name = "file", required = true) MultipartFile files) {
		return soalService.createSoalWithUpload(namaSoal, durasi, jawaban, deskripsi, files, directoryPilihanGanda, SOALTYPE.PILIHANGANDA);
	}
	
	@PostMapping(path = "/essay", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE } )
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> createSoalEssay(
			@RequestParam("namaSoal") String namaSoal,
			@RequestParam("durasi") String durasi,
			@RequestParam("jawaban") String jawaban,
			@RequestParam("deskripsi") String deskripsi,
			@RequestParam(name = "file", required = true) MultipartFile files) {
		return soalService.createSoalWithUpload(namaSoal, durasi, jawaban, deskripsi, files, directoryEssay, SOALTYPE.ESSAY);
	}

	
	@PostMapping(path = "/pauli", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> loginUser(@RequestBody RequestCreateSoalPauli request) {
		return soalService.createSoalPauli(request);
	}
}
