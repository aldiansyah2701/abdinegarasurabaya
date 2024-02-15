package com.abdinegara.surabaya.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	
	private static final String UPLOAD_DIR = "C:\\Users\\Dell3420\\Documents\\abdinegaraexel";
	
	public enum SOALTYPE{
		PILIHANGANDA, ESSAY
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
                path = path +"\\"+ file.getOriginalFilename();
                
                Optional<BuatSoal> soalExist = buatSoalRepository.findByNamaSoalAndJenisSoalAndJenisSiswa(namaSoal, jenisSoal, jenisSiswa);
                if(soalExist.isPresent()) {
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
	
		try {
			// Create the uploads directory if it doesn't exist
            File uploadDir = new File(directory);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            // Save the file to the uploads directory
            File excelFile = new File(uploadDir, file.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                fos.write(file.getBytes());
                
                String path = directory;      
                path = path +"\\"+ file.getOriginalFilename();
                
                if(SOALTYPE.PILIHANGANDA.equals(type)) {
                	updatePilihanGanda(namaSoal, durasi, jawaban, deskripsi, file, path);
                } else if(SOALTYPE.ESSAY.equals(type)) {
                	updateEssay(namaSoal, durasi, jawaban, deskripsi, file, path);
                }

            }
            
            
            response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
	}
	
	private void updateEssay(String namaSoal, String durasi, String jawaban, String deskripsi,
			MultipartFile file, String path) {
		SoalEssay soal = new SoalEssay();
        Optional<SoalEssay> soalExist = soalEssayRepository.findByNamaSoal(namaSoal);
        if(soalExist.isPresent()) {
        	soal = soalExist.get();
            soal.setUpdateDate(new Date());
            
        } else {
        	
        	soal.setCreatedDate(new Date());            	
        	soal.setNamaSoal(namaSoal);
        }
        soal.setFilePath(path);
        soal.setDeskripsi(deskripsi);
        soal.setDurasi(durasi);
        soal.setJawaban(jawaban);
        
        soalEssayRepository.save(soal);
	}
	
	private void updatePilihanGanda(String namaSoal, String durasi, String jawaban, String deskripsi,
			MultipartFile file, String path) {
        SoalPilihanGanda soal = new SoalPilihanGanda();
        Optional<SoalPilihanGanda> soalExist = soalPilihanGandaRepository.findByNamaSoal(namaSoal);
        if(soalExist.isPresent()) {
        	soal = soalExist.get();
            soal.setUpdateDate(new Date());
            
        } else {
        	
        	soal.setCreatedDate(new Date());            	
        	soal.setNamaSoal(namaSoal);
        }
        soal.setFilePath(path);
        soal.setDeskripsi(deskripsi);
        soal.setDurasi(durasi);
        soal.setJawaban(jawaban);
        
        soalPilihanGandaRepository.save(soal);
	}
	
	@Transactional(readOnly = false)
	public ResponseEntity<Object> createSoalPauli(RequestCreateSoalPauli request) {
		BaseResponse response = new BaseResponse();

		Optional<SoalPauli> soalExist = soalPauliRepository.findByNamaSoal(request.getNamaSoal());
		SoalPauli soal = new SoalPauli();
		if (soalExist.isPresent()) {
			soal = soalExist.get();
			soal.setUpdateDate(new Date());

		} else {

			soal.setCreatedDate(new Date());
			soal.setNamaSoal(request.getNamaSoal());
		}
		soal.setDeskripsi(request.getDeskripsi());
		soal.setDurasi(request.getDurasi());
		soal.setJawaban(request.getJawaban());
		soal.setSoal(request.getSoal());

		soalPauliRepository.save(soal);
		response.setMessage(BaseResponse.SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

}
