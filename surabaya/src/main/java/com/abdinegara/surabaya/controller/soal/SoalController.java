package com.abdinegara.surabaya.controller.soal;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import com.abdinegara.surabaya.message.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
			@RequestParam("isCanRevisi") Boolean isCanRevisi,
			@RequestParam(name = "file", required = true) MultipartFile files,
			@RequestParam(name = "image", required = false) MultipartFile[] images) {
		return soalService.createSoalWithUpload(namaSoal, durasi, jawaban, deskripsi, files, images, directoryPilihanGanda, SOALTYPE.PILIHANGANDA, jenis, isCanRevisi);
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
		return soalService.createSoalWithUpload(namaSoal, durasi, jawaban, deskripsi, files, images, directoryEssay, SOALTYPE.ESSAY, jenis, false);
	}
	
	@PostMapping(path = "/create/TKD", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE } )
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> createSoalTkd(
			@RequestParam("namaSoal") String namaSoal,
			@RequestParam("durasi") String durasi,
			@RequestParam("deskripsi") String deskripsi,
			@RequestParam("jenis") String jenis,
			@RequestParam("jawabanTwk") String jawabanTwk,
			@RequestParam(name = "fileTwk", required = true) MultipartFile filesTwk,
			@RequestParam(name = "imagesTwk", required = false) MultipartFile[] imagesTwk,
			@RequestParam("jawabanTiu") String jawabanTiu,
			@RequestParam(name = "fileTiu", required = true) MultipartFile filesTiu,
			@RequestParam(name = "imagesTiu", required = false) MultipartFile[] imagesTiu,
			@RequestParam("jawabanTkp") String jawabanTkp,
			@RequestParam(name = "fileTkp", required = true) MultipartFile filesTkp,
			@RequestParam(name = "imagesTkp", required = false) MultipartFile[] imagesTkp) {
		return soalService.createSoalTKDWithUpload(namaSoal, durasi, deskripsi, jenis, jawabanTwk,
				filesTwk, imagesTwk, jawabanTiu, filesTiu, imagesTiu, jawabanTkp, filesTkp, imagesTkp);
	}

	@PostMapping(path = "/create/pauli", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> createSoalPauli(@RequestBody RequestCreateSoalPauli request) {
		return soalService.createSoalPauli("", request);
	}

	@PostMapping(path = "/create/ganjil-genap", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> createSoalGanjilGenap(@RequestBody RequestCreateSoalPauli request) {
		return soalService.createSoalGanjilGenap("", request);
	}

	@PostMapping(path = "/create/soal-hilang", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> createSoalHilang(@RequestBody RequestCreateSoalHilang request) {
		return soalService.createSoalHilang("", request);
	}

	@PostMapping(path = "/upload/image", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE } )
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> uploadImage(
			@RequestParam(name = "image", required = false) MultipartFile[] images,
			HttpServletRequest request) {
		Principal user = request.getUserPrincipal();

		return soalService.uploadImagePreview(user.getName(), images);
	}
	
	@PostMapping(path = "/upload/video", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE } )
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> uploadVideo(
			@RequestParam("namaVideo") String namaVideo,
			@RequestParam("deskripsi") String deskripsi,
			@RequestParam("jenis") String jenis,
			@RequestParam(name = "video", required = false) MultipartFile video,
			HttpServletRequest request) {
		Principal user = request.getUserPrincipal();

		return soalService.uploadPembelajaranVideo(namaVideo, deskripsi, jenis, video);
	}
	
	@PostMapping(path = "/upload/update/video", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE } )
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> uploadEditVideo(
			@RequestParam("namaVideo") String namaVideo,
			@RequestParam("deskripsi") String deskripsi,
			@RequestParam("jenis") String jenis,
			@RequestParam("uuid") String uuid,
			@RequestParam(name = "video", required = false) MultipartFile video,
			HttpServletRequest request) {
		Principal user = request.getUserPrincipal();

		return soalService.uploadUpdatePembelajaranVideo(uuid, namaVideo, deskripsi, jenis, video);
	}
	
	@GetMapping(value = "/list/video")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> getListVideo(Pageable pageable) {
		return soalService.getVideos(pageable);
	}
	
	@GetMapping(value = "/detail/video/{uuid}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> getDetailVideo(@PathVariable("uuid") String uuid) {
		return soalService.getVideo(uuid);
	}
	
	@DeleteMapping(value = "/delete/video/{uuid}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> deleteVideo(@PathVariable("uuid") String uuid) {
		return soalService.deleteVideo(uuid);
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
			@RequestParam("isCanRevisi") Boolean isCanRevisi,
			@RequestParam(name = "file", required = false) MultipartFile files,
			@RequestParam(name = "image", required = false) MultipartFile[] images) {
		return soalService.updateSoalWithUpload(uuid, namaSoal, durasi, jawaban, deskripsi, files, images, directoryPilihanGanda, SOALTYPE.PILIHANGANDA, jenis, isCanRevisi);
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
		return soalService.updateSoalWithUpload(uuid, namaSoal, durasi, jawaban, deskripsi, files, images, directoryEssay, SOALTYPE.ESSAY, jenis, false);
	}

	
	@PostMapping(path = "/update/pauli", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> updateSoalPauli(
			@RequestParam("uuid") String uuid,
			@RequestBody RequestCreateSoalPauli request) {
		return soalService.createSoalPauli(uuid, request);
	}

	@PostMapping(path = "/update/soal-hilang", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> updateSoalHilang(
			@RequestParam("uuid") String uuid,
			@RequestBody RequestCreateSoalHilang request) {

		return soalService.createSoalHilang(uuid, request);
	}

	@PostMapping(path = "/update/ganjil-genap", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> updateSoalGanjilGenap(
			@RequestParam("uuid") String uuid,
			@RequestBody RequestCreateSoalPauli request) {
		return soalService.createSoalGanjilGenap(uuid, request);
	}
	
	@PostMapping(path = "/update/TKD", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE } )
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> updateSoalTkd(
			@RequestParam("uuid") String uuid,
			@RequestParam("namaSoal") String namaSoal,
			@RequestParam("durasi") String durasi,
			@RequestParam("deskripsi") String deskripsi,
			@RequestParam("jenis") String jenis,
			@RequestParam("jawabanTwk") String jawabanTwk,
			@RequestParam(name = "filesTwk", required = false) MultipartFile filesTwk,
			@RequestParam(name = "imagesTwk", required = false) MultipartFile[] imagesTwk,
			@RequestParam("jawabanTiu") String jawabanTiu,
			@RequestParam(name = "filesTiu", required = false) MultipartFile filesTiu,
			@RequestParam(name = "imagesTiu", required = false) MultipartFile[] imagesTiu,
			@RequestParam("jawabanTkp") String jawabanTkp,
			@RequestParam(name = "filesTkp", required = false) MultipartFile filesTkp,
			@RequestParam(name = "imagesTkp", required = false) MultipartFile[] imagesTkp) {
		return soalService.updateSoalTKDWithUpload(uuid, namaSoal, durasi, deskripsi, jenis, jawabanTwk,
				filesTwk, imagesTwk, jawabanTiu, filesTiu, imagesTiu, jawabanTkp, filesTkp, imagesTkp);
	}
	
	@GetMapping(value = "/list/{type}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SISWA')")
	public ResponseEntity<Object> getListSoal(@PathVariable("type") SOALTYPE type, Pageable pageable) {
		return soalService.getSoal(type, pageable);
	}
	
	@GetMapping(value = "/download")
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SISWA')")
	public ResponseEntity<Object> downloadSoal(@RequestParam("path") String path) {
		return soalService.downloadSoal(path);
	}
	
	@GetMapping(value = "/detail/{type}/{uuid}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SISWA')")
	public ResponseEntity<Object> getDetailSiswa(@PathVariable("type") SOALTYPE type, @PathVariable("uuid") String uuid) {
		return soalService.getSoalDetail(type, uuid);
	}
	
	@DeleteMapping(value = "/delete/{type}/{uuid}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> deleteSoal(@PathVariable("type") SOALTYPE type, @PathVariable("uuid") String uuid) {
		return soalService.deleteSoal(type, uuid);
	}
	
	@PostMapping(path = "/create/ujian", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> createUjian(@RequestBody RequestCreateUjian request) {
		return soalService.createUjian(request);
	}
	
	@GetMapping(value = "/list/ujian-old")
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SISWA')")
	public ResponseEntity<Object> getListUjian(@RequestParam(name = "jenis", required = false) String jenis, Pageable pageable) {
		return soalService.getListUjian(jenis, pageable);
	}

	@GetMapping(value = "/list/ujian")
	public ResponseEntity<Object> getListUjianNew(@RequestParam(name = "jenis", required = false) String jenis, Pageable pageable) {
		// Get the current authentication
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// Check if the user has the role "ROLE_SISWA"
		boolean isSiswa = authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_SISWA"));

		// Check if the user has the role "ROLE_ADMIN"
		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

		// Check if the user is a Siswa and the jenis parameter is not provided
		if (isSiswa && jenis == null) {
			return ResponseEntity.badRequest().body("Jenis parameter is required for Siswa");
		}

		// If the user is an admin, jenis parameter is optional, otherwise, it's required
		if (isAdmin || (isSiswa && jenis != null)) {
			// Call your service method and return the response
			return soalService.getListUjian(jenis, pageable);
		} else {
			// If the user is neither admin nor Siswa with jenis provided, return access denied
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
		}
	}
	
	@GetMapping(value = "/detail/ujian/{uuid}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SISWA')")
	public ResponseEntity<Object> getDetailUjian(@PathVariable("uuid") String uuid) {
		return soalService.getDetailUjian(uuid);
	}
	
	@DeleteMapping(value = "/delete/ujian/{uuid}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> deleteUjian(@PathVariable("uuid") String uuid) {
		return soalService.deleteUjian(uuid);
	}
	
	@PostMapping(path = "/update/ujian/{uuid}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> createUjian(@RequestBody RequestCreateUjian request, @PathVariable("uuid") String uuid) {
		return soalService.updateUjian(request, uuid);
	}

	@GetMapping(value = "/test-mail")
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SISWA')")
	public ResponseEntity<Object> testMail() {
		soalService.testMail();
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(value = "/beli/ujian")
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SISWA')")
	public ResponseEntity<Object> belilUjian(@RequestBody RequestBeliUjian request) {
		return soalService.beliUjian(request);
	}

	@GetMapping(value = "/history/approval/ujian")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> listApprovalBeliUjian(@RequestParam(name = "approval", required = false) SoalService.APPROVAL approval, Pageable pageable) {
		return soalService.listApprovalBeliUjian(approval, pageable);
	}

	@GetMapping(value = "/history-siswa/beli/ujian")
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SISWA')")
	public ResponseEntity<Object> historyBeliUjian(@RequestParam(name = "userUuid", required = false) String userUuid, Pageable pageable) {
		return soalService.historyBeliUjian(userUuid, pageable);
	}

	@PostMapping(value = "/approval/beli/ujian")
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SISWA')")
	public ResponseEntity<Object> approvalbelilUjian(@RequestParam(name = "approval", required = true) SoalService.APPROVAL approval,
											 @RequestParam(name = "userUuid", required = true) String userUuid,
											 @RequestParam(name = "ujianUuid", required = true) String ujianUuid,
											 @RequestParam(name = "adminName", required = true) String adminName,
													 @RequestParam(name = "remark", required = false) String remark) {
		return soalService.approvalsBeliUjian(approval, userUuid, ujianUuid, adminName, remark);
	}

	@DeleteMapping(value = "/delete/beli/ujian/{uuid}")
	@PreAuthorize("hasAuthority('ROLE_SISWA')")
	public ResponseEntity<Object> deleteBeliUjian(@PathVariable("uuid") String uuid) {
		return soalService.deleteBeliUjian(uuid);
	}

	@PostMapping(path = "/upload-transfer/beli/ujian", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE } )
	@PreAuthorize("hasAuthority('ROLE_SISWA')")
	public ResponseEntity<Object> uploadTransferBeliUjian(
			@RequestParam("ujianUuid") String ujianUuid,
			@RequestParam("userUuid") String userUuid,
			@RequestParam("rekening") String rekening,
			@RequestParam(name = "file", required = true) MultipartFile files) {
		return soalService.uploadTransferBeliUjian(ujianUuid, userUuid, rekening, files);
	}

	@PostMapping(value = "/jawaban/ujian")
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SISWA')")
	public ResponseEntity<Object> jawabanUjian(@RequestBody RequestJawabanSiswaTKD request) {
		return soalService.jawabanUjian(request);
	}

}
