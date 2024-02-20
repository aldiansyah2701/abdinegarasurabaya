package com.abdinegara.surabaya.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.abdinegara.surabaya.entity.BuatSoal;
import com.abdinegara.surabaya.entity.SoalEssay;
import com.abdinegara.surabaya.entity.SoalPauli;
import com.abdinegara.surabaya.entity.SoalPilihanGanda;
import com.abdinegara.surabaya.message.BaseResponse;
import com.abdinegara.surabaya.message.RequestCreateSoalPauli;
import com.abdinegara.surabaya.repository.BuatSoalRepository;
import com.abdinegara.surabaya.repository.SoalEssayRepository;
import com.abdinegara.surabaya.repository.SoalPauliRepository;
import com.abdinegara.surabaya.repository.SoalPilihanGandaRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SoalService {

	@Autowired
	private BuatSoalRepository buatSoalRepository;

	@Autowired
	private SoalPilihanGandaRepository soalPilihanGandaRepository;

	@Autowired
	private SoalEssayRepository soalEssayRepository;

	@Autowired
	private SoalPauliRepository soalPauliRepository;
	
	@Autowired
	private ResourceLoader resourceLoader;

	private static final String UPLOAD_DIR = "C:\\Users\\Dell3420\\Documents\\abdinegaraexel";

	public enum SOALTYPE {
		PILIHANGANDA, ESSAY, PAULI
	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> createSoal(String namaSoal, String jenisSoal, String jenisSiswa, MultipartFile file) {
		BaseResponse response = new BaseResponse();

		try {
			// Create the uploads directory if it doesn't exist
			File uploadDir = new File(UPLOAD_DIR);
			if (!uploadDir.exists()) {
				uploadDir.mkdir();
			}

			// Save the file to the uploads directory
			File excelFile = new File(uploadDir, file.getOriginalFilename());
			try (FileOutputStream fos = new FileOutputStream(excelFile)) {
				fos.write(file.getBytes());

				BuatSoal soal = new BuatSoal();
				String path = UPLOAD_DIR;
				path = path + "\\" + file.getOriginalFilename();

				Optional<BuatSoal> soalExist = buatSoalRepository.findByNamaSoalAndJenisSoalAndJenisSiswa(namaSoal,
						jenisSoal, jenisSiswa);
				if (soalExist.isPresent()) {
					soal = soalExist.get();
					soal.setFilePath(path);
					soal.setUpdateDate(new Date());
				} else {

					soal.setCreatedDate(new Date());
					soal.setFilePath(path);
					soal.setJenisSiswa(jenisSiswa);
					soal.setJenisSoal(jenisSoal);
					soal.setNamaSoal(namaSoal);
				}

				buatSoalRepository.save(soal);
			}

			response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> createSoalWithUpload(String namaSoal, String durasi, String jawaban, String deskripsi,
			MultipartFile file, String directory, SOALTYPE type) {
		BaseResponse response = new BaseResponse();
		
		if (SOALTYPE.PILIHANGANDA.equals(type)) {
			Optional<SoalPilihanGanda> soalExist = soalPilihanGandaRepository.findByNamaSoal(namaSoal);
			if(soalExist.isPresent()) {
				response.setMessage("nama soal sudah tersedia");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}	
		} else if (SOALTYPE.ESSAY.equals(type)) {
			Optional<SoalEssay> soalExist = soalEssayRepository.findByNamaSoal(namaSoal);
			if(soalExist.isPresent()) {
				response.setMessage("nama soal sudah tersedia");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}			
		}
		
//		String uploadPath = servletContext.getRealPath("/static"+directory);
		String uploadPath = "";
		try {
			Resource resource = resourceLoader.getResource("classpath:/static"+directory);
			File file2 = resource.getFile();
			uploadPath = file2.getAbsolutePath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		try {
			// Create the uploads directory if it doesn't exist
			File uploadDir = new File(uploadPath);
			if (!uploadDir.exists()) {
				uploadDir.mkdir();
			}

			 // Save the file to the soal folder
	        File destFile = new File(uploadDir.getAbsolutePath() + File.separator + file.getOriginalFilename());
	        file.transferTo(destFile);
	        
			// Save the file to the uploads directory
//			File excelFile = new File(uploadDir, file.getOriginalFilename());
//			try (FileOutputStream fos = new FileOutputStream(excelFile)) {
//				fos.write(file.getBytes());

				String path = directory;
				path = path + "/" + file.getOriginalFilename();

				if (SOALTYPE.PILIHANGANDA.equals(type)) {
					updatePilihanGanda("", namaSoal, durasi, jawaban, deskripsi, file, path);
				} else if (SOALTYPE.ESSAY.equals(type)) {
					updateEssay("", namaSoal, durasi, jawaban, deskripsi, file, path);
				}

//			}

			response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

	}

	private void updateEssay(String uuid, String namaSoal, String durasi, String jawaban, String deskripsi, MultipartFile file,
			String path) {
		SoalEssay soal = new SoalEssay();
		Optional<SoalEssay> soalExist = soalEssayRepository.findById(uuid);
		if (soalExist.isPresent()) {
			soal = soalExist.get();
			soal.setUpdateDate(new Date());
			soal.setNamaSoal(namaSoal == null ? soal.getNamaSoal() : namaSoal);
			soal.setFilePath(file == null ? soal.getFilePath(): path);
			soal.setDeskripsi(deskripsi == null ? soal.getDeskripsi() : deskripsi);
			soal.setDurasi(durasi == null ? soal.getDurasi() : durasi);
			soal.setJawaban(jawaban == null ? soal.getJawaban() : jawaban);


		} else {

			soal.setCreatedDate(new Date());
			soal.setNamaSoal(namaSoal);
			soal.setFilePath(path);
			soal.setDeskripsi(deskripsi);
			soal.setDurasi(durasi);
			soal.setJawaban(jawaban);
		}

		soalEssayRepository.save(soal);
	}

	private void updatePilihanGanda(String uuid, String namaSoal, String durasi, String jawaban, String deskripsi,
			MultipartFile file, String path) {
		SoalPilihanGanda soal = new SoalPilihanGanda();
		Optional<SoalPilihanGanda> soalExist = soalPilihanGandaRepository.findById(uuid);
		if (soalExist.isPresent()) {
			soal = soalExist.get();
			soal.setUpdateDate(new Date());
			soal.setNamaSoal(namaSoal == null ? soal.getNamaSoal() : namaSoal);
			soal.setFilePath(file == null ? soal.getFilePath(): path);
			soal.setDeskripsi(deskripsi == null ? soal.getDeskripsi() : deskripsi);
			soal.setDurasi(durasi == null ? soal.getDurasi() : durasi);
			soal.setJawaban(jawaban == null ? soal.getJawaban() : jawaban);

		} else {

			soal.setCreatedDate(new Date());
			soal.setNamaSoal(namaSoal);
			soal.setFilePath(path);
			soal.setDeskripsi(deskripsi);
			soal.setDurasi(durasi);
			soal.setJawaban(jawaban);
		}

		soalPilihanGandaRepository.save(soal);
	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> createSoalPauli(String uuid, RequestCreateSoalPauli request) {
		BaseResponse response = new BaseResponse();
		Optional<SoalPauli> soalExist = soalPauliRepository.findByNamaSoal(request.getNamaSoal());
		if(soalExist.isPresent() && "".equals(uuid)) {
			response.setMessage("nama soal sudah tersedia");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		Optional<SoalPauli> soalUpdateExist = soalPauliRepository.findById(uuid);
		if(!soalUpdateExist.isPresent() && !"".equals(uuid)) {
			response.setMessage("soal tidak tersedia");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		SoalPauli soal = new SoalPauli();
		if (soalUpdateExist.isPresent()) {
			soal = soalUpdateExist.get();
			soal.setUpdateDate(new Date());
			
			soal.setNamaSoal(request.getNamaSoal() == null ? soal.getNamaSoal() : request.getNamaSoal());
			soal.setDeskripsi(request.getDeskripsi() == null ? soal.getDeskripsi() : request.getDeskripsi());
			soal.setDurasi(request.getDurasi() == null ? soal.getDurasi() : request.getDurasi());
			soal.setJawaban(request.getJawaban() == null ? soal.getJawaban() : request.getJawaban());
			soal.setSoal(request.getSoal() == null ? soal.getSoal() : request.getSoal());
		} else {

			soal.setCreatedDate(new Date());
			soal.setNamaSoal(request.getNamaSoal());
			soal.setDeskripsi(request.getDeskripsi());
			soal.setDurasi(request.getDurasi());
			soal.setJawaban(request.getJawaban());
			soal.setSoal(request.getSoal());
		}

		soalPauliRepository.save(soal);
		response.setMessage(BaseResponse.SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	public ResponseEntity<Object> getSoal(SOALTYPE type, Pageable pageable) {
		BaseResponse response = new BaseResponse();
		response.setMessage("Data found successfully");
		if (SOALTYPE.PILIHANGANDA.equals(type)) {
			Page<SoalPilihanGanda> data = soalPilihanGandaRepository.findAll(pageable);
			response.setData(data);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else if (SOALTYPE.ESSAY.equals(type)) {
			Page<SoalEssay> data = soalEssayRepository.findAll(pageable);
			response.setData(data);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else if (SOALTYPE.PAULI.equals(type)) {
			Page<SoalPauli> data = soalPauliRepository.findAll(pageable);
			response.setData(data);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}

		response.setMessage("Data not found");
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

	}

	public ResponseEntity<Object> getSoalDetail(SOALTYPE type, String uuid) {
		BaseResponse response = new BaseResponse();
		response.setMessage("Data found successfully");
		if (SOALTYPE.PILIHANGANDA.equals(type)) {
			Optional<SoalPilihanGanda> data = soalPilihanGandaRepository.findById(uuid);
			if (data.isPresent()) {

				response.setData(data);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
		} else if (SOALTYPE.ESSAY.equals(type)) {
			Optional<SoalEssay> data = soalEssayRepository.findById(uuid);
			if (data.isPresent()) {

				response.setData(data);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
		} else if (SOALTYPE.PAULI.equals(type)) {
			Optional<SoalPauli> data = soalPauliRepository.findById(uuid);
			if (data.isPresent()) {

				response.setData(data);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
		}

		response.setMessage("Data not found");
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

	}

	public ResponseEntity<Object> deleteSoal(SOALTYPE type, String uuid) {
		BaseResponse response = new BaseResponse();
		response.setMessage("Delete data successfully");
		if (SOALTYPE.PILIHANGANDA.equals(type)) {
			Optional<SoalPilihanGanda> data = soalPilihanGandaRepository.findById(uuid);
			if (data.isPresent()) {
				soalPilihanGandaRepository.deleteById(uuid);
				return new ResponseEntity<>(response, HttpStatus.OK);

			}
		} else if (SOALTYPE.ESSAY.equals(type)) {
			Optional<SoalEssay> data = soalEssayRepository.findById(uuid);
			if (data.isPresent()) {
				soalEssayRepository.deleteById(uuid);
				return new ResponseEntity<>(response, HttpStatus.OK);

			}
		} else if (SOALTYPE.PAULI.equals(type)) {
			Optional<SoalPauli> data = soalPauliRepository.findById(uuid);
			if (data.isPresent()) {
				soalPauliRepository.deleteById(uuid);
				return new ResponseEntity<>(response, HttpStatus.OK);

			}
		}

		response.setMessage("Data not found");
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

	}
	
	@Transactional(readOnly = false)
	public ResponseEntity<Object> updateSoalWithUpload(String uuid, String namaSoal, String durasi, String jawaban, String deskripsi,
			MultipartFile file, String directory, SOALTYPE type) {
		BaseResponse response = new BaseResponse();
		
		if (SOALTYPE.PILIHANGANDA.equals(type)) {
			Optional<SoalPilihanGanda> soalExist = soalPilihanGandaRepository.findById(uuid);
			if(!soalExist.isPresent()) {
				response.setMessage("soal tidak tersedia");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}	
		} else if (SOALTYPE.ESSAY.equals(type)) {
			Optional<SoalEssay> soalExist = soalEssayRepository.findById(uuid);
			if(!soalExist.isPresent()) {
				response.setMessage("soal tidak tersedia");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}			
		}
		
		String uploadPath = "";
		
		try {
			// Create the uploads directory if it doesn't exist
			String path = "";
			if(file != null) {
				File uploadDir = new File(uploadPath);
				if (!uploadDir.exists()) {
					uploadDir.mkdir();
				}
				
				// Save the file to the soal folder
				File destFile = new File(uploadDir.getAbsolutePath() + File.separator + file.getOriginalFilename());
				file.transferTo(destFile);
				
				path = directory;
				path = path + "/" + file.getOriginalFilename();				
			}

			if (SOALTYPE.PILIHANGANDA.equals(type)) {
				updatePilihanGanda(uuid, namaSoal, durasi, jawaban, deskripsi, file, path);
			} else if (SOALTYPE.ESSAY.equals(type)) {
				updateEssay(uuid, namaSoal, durasi, jawaban, deskripsi, file, path);
			}

			response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}

}
