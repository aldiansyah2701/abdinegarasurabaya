package com.abdinegara.surabaya.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.abdinegara.surabaya.entity.BuatSoal;
import com.abdinegara.surabaya.message.BaseResponse;
import com.abdinegara.surabaya.repository.BuatSoalRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SoalService {
	
	@Autowired
	private BuatSoalRepository buatSoalRepository;
	
	private static final String UPLOAD_DIR = "C:\\Users\\Dell3420\\Documents\\exelabdinegara";
	
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

}
