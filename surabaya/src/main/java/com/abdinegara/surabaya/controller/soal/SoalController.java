package com.abdinegara.surabaya.controller.soal;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

@CrossOrigin
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
	
	@PostMapping(path = "/create/pilihan/ganda", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE } )
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> createSoalPilihanGanda(
			@RequestParam("namaSoal") String namaSoal,
			@RequestParam("durasi") String durasi,
			@RequestParam("jawaban") String jawaban,
			@RequestParam("deskripsi") String deskripsi,
			@RequestParam("jenis") String jenis,
			@RequestParam(name = "file", required = true) MultipartFile files,
			@RequestParam(name = "image", required = false) MultipartFile[] images) {
		return soalService.createSoalWithUpload(namaSoal, durasi, jawaban, deskripsi, files, images, directoryPilihanGanda, SOALTYPE.PILIHANGANDA, jenis);
	}
	
	@PostMapping(path = "/create/essay", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE } )
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> createSoalEssay(
			@RequestParam("namaSoal") String namaSoal,
			@RequestParam("durasi") String durasi,
			@RequestParam("jawaban") String jawaban,
			@RequestParam("deskripsi") String deskripsi,
			@RequestParam("jenis") String jenis,
			@RequestParam(name = "file", required = true) MultipartFile files,
			@RequestParam(name = "image", required = false) MultipartFile[] images) {
		return soalService.createSoalWithUpload(namaSoal, durasi, jawaban, deskripsi, files, images, directoryEssay, SOALTYPE.ESSAY, jenis);
	}

	@PostMapping(path = "/upload/image", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE } )
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> uploadImage(
			@RequestParam(name = "image", required = false) MultipartFile[] images,
			HttpServletRequest request) {
		Principal user = request.getUserPrincipal();

		return soalService.uploadImagePreview(user.getName(), images);
	}
	
	@PostMapping(path = "/create/pauli", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> createSoalPauli(@RequestBody RequestCreateSoalPauli request) {
		return soalService.createSoalPauli("", request);
	}
	
	@PostMapping(path = "/update/pilihan/ganda", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE } )
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> updateSoalPilihanGanda(
			@RequestParam("uuid") String uuid,
			@RequestParam("namaSoal") String namaSoal,
			@RequestParam("durasi") String durasi,
			@RequestParam("jawaban") String jawaban,
			@RequestParam("deskripsi") String deskripsi,
			@RequestParam("jenis") String jenis,
			@RequestParam(name = "file", required = false) MultipartFile files,
			@RequestParam(name = "image", required = false) MultipartFile[] images) {
		return soalService.updateSoalWithUpload(uuid, namaSoal, durasi, jawaban, deskripsi, files, images, directoryPilihanGanda, SOALTYPE.PILIHANGANDA, jenis);
	}
	
	@PostMapping(path = "/update/essay", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE } )
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> updateSoalEssay(
			@RequestParam("uuid") String uuid,
			@RequestParam("namaSoal") String namaSoal,
			@RequestParam("durasi") String durasi,
			@RequestParam("jawaban") String jawaban,
			@RequestParam("deskripsi") String deskripsi,
			@RequestParam("jenis") String jenis,
			@RequestParam(name = "file", required = false) MultipartFile files,
			@RequestParam(name = "image", required = false) MultipartFile[] images) {
		return soalService.updateSoalWithUpload(uuid, namaSoal, durasi, jawaban, deskripsi, files, images, directoryEssay, SOALTYPE.ESSAY, jenis);
	}

	
	@PostMapping(path = "/update/pauli", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> updateSoalPauli(
			@RequestParam("uuid") String uuid,
			@RequestBody RequestCreateSoalPauli request) {
		return soalService.createSoalPauli(uuid, request);
	}
	
	@GetMapping(value = "/list/{type}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> getListSiswa(@PathVariable("type") SOALTYPE type, Pageable pageable) {
		return soalService.getSoal(type, pageable);
	}
	
	@GetMapping(value = "/detail/{type}/{uuid}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> getDetailSiswa(@PathVariable("type") SOALTYPE type, @PathVariable("uuid") String uuid) {
		return soalService.getSoalDetail(type, uuid);
	}
	
	@DeleteMapping(value = "/delete/{type}/{uuid}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> deleteUser(@PathVariable("type") SOALTYPE type, @PathVariable("uuid") String uuid) {
		return soalService.deleteSoal(type, uuid);
	}
}
