package com.abdinegara.surabaya.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.abdinegara.surabaya.entity.BuatSoal;
import com.abdinegara.surabaya.entity.PembelianSoal;
import com.abdinegara.surabaya.entity.QuoteSiswa;
import com.abdinegara.surabaya.entity.Siswa;
import com.abdinegara.surabaya.message.BaseResponse;
import com.abdinegara.surabaya.message.RequestQuote;
import com.abdinegara.surabaya.message.ResponsePembelianSoal;
import com.abdinegara.surabaya.repository.PembelianSoalRepository;
import com.abdinegara.surabaya.repository.QuoteSiswaRepository;
import com.abdinegara.surabaya.repository.SiswaRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SiswaService {
	
	@Autowired
	private QuoteSiswaRepository quoteSiswaRepository; 
	
	@Autowired
	private SiswaRepository siswaRepository;
	
	@Autowired
	private PembelianSoalRepository pembelianSoalRepository;
	
	@Value("${directory.quote.gambar}")
	private String directoryGambar;
	
	@Value("${directory.quote.video}")
	private String directoryVideo;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Value("${directory.base.path}")
	private String directoryBasePath;
	
	@Transactional(readOnly = false)
	public ResponseEntity<Object> createQuote(String type, RequestQuote request) {
		BaseResponse response = new BaseResponse();
		
		QuoteSiswa quote = new QuoteSiswa();
		Optional<QuoteSiswa> dataExist = quoteSiswaRepository.findByTitle(type);
		if(dataExist.isPresent()) {
			quote = dataExist.get();
			quote.setUpdateDate(new Date());
			quote.setQuote(request.getQuote());
		} else {
			
			quote.setCreatedDate(new Date());
			quote.setTitle(request.getTitle());
			quote.setQuote(request.getQuote());
		}
		
		quoteSiswaRepository.save(quote);
		
		response.setMessage("Quote updated successfully");
		response.setData(quote);
		return new ResponseEntity<>(response, HttpStatus.OK);
		
	}
	
//	private static final String UPLOAD_DIR_GAMBAR = "C:\\Users\\Dell3420\\Documents\\abdinegaragambar";
//	private static final String UPLOAD_DIR_VIDEO = "C:\\Users\\Dell3420\\Documents\\abdinegaravideo";

	@Transactional(readOnly = false)
	public ResponseEntity<Object> createQuote(String type, String title, String quote, MultipartFile fileGambar, MultipartFile fileVideo) {
		BaseResponse response = new BaseResponse();

		QuoteSiswa quoteSiswa = new QuoteSiswa();
		Optional<QuoteSiswa> dataExist = quoteSiswaRepository.findByTitle(type);
		
		try {
			
			if(dataExist.isPresent()) {
    			quoteSiswa = dataExist.get();
    			quoteSiswa.setUpdateDate(new Date());
    			quoteSiswa.setQuote(quote);
    			
    		} else {
    			
    			quoteSiswa.setCreatedDate(new Date());
    			quoteSiswa.setTitle(title);
    			quoteSiswa.setQuote(quote);
    		}
			
			String uploadGambarPath = "";
			String uploadVideoPath = "";
			try {
				Resource resource = resourceLoader.getResource("classpath:/static"+directoryGambar);
				File file2 = resource.getFile();
//				uploadGambarPath = file2.getAbsolutePath();
				uploadGambarPath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file2.getAbsolutePath()
						: (directoryBasePath + directoryGambar);
				
				Resource resourceVideo = resourceLoader.getResource("classpath:/static"+directoryVideo);
				File file3 = resourceVideo.getFile();
//				uploadVideoPath = file3.getAbsolutePath();
				uploadVideoPath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file3.getAbsolutePath()
						: (directoryBasePath + directoryVideo);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response.setMessage(e.getMessage());
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
//			String uploadGambarPath = servletContext.getRealPath("/static"+directoryGambar);
			File uploadDirGambar = new File(uploadGambarPath);
            if (!uploadDirGambar.exists()) {
            	uploadDirGambar.mkdir();
            }
            
//            String uploadVideoPath = servletContext.getRealPath("/static"+directoryVideo);
            File uploadDirVideo = new File(uploadVideoPath);
            if (!uploadDirVideo.exists()) {
            	uploadDirVideo.mkdir();
            }
            
         // Save the file to the uploads directory
            if(fileGambar != null) {
            	
            	 // Save the file to the soal folder
    	        File destFile = new File(uploadDirGambar.getAbsolutePath() + File.separator + fileGambar.getOriginalFilename());
    	        fileGambar.transferTo(destFile);
    	        
//            	File gambarFile = new File(uploadDirGambar, fileGambar.getOriginalFilename());
//            	try (FileOutputStream fos = new FileOutputStream(gambarFile)) {
//            		fos.write(fileGambar.getBytes());
            		
            		String pathGambar = directoryGambar;      
            		pathGambar = pathGambar +"/"+ fileGambar.getOriginalFilename();
            		quoteSiswa.setFilePathGambar(pathGambar);
//            	}            	
            }
            
            if(fileVideo != null) {
            	 // Save the file to the soal folder
    	        File destFile = new File(uploadDirVideo.getAbsolutePath() + File.separator + fileVideo.getOriginalFilename());
    	        fileVideo.transferTo(destFile);
    	        
//            	File videoFile = new File(uploadDirVideo, fileVideo.getOriginalFilename());
//            	try (FileOutputStream fos = new FileOutputStream(videoFile)) {
//            		fos.write(fileVideo.getBytes());
            		
            		String pathVideo = directoryVideo;      
            		pathVideo = pathVideo +"/"+ fileVideo.getOriginalFilename();
            		quoteSiswa.setFilePathVideo(pathVideo);
//            	}            	
            }
            
            
    		
    		quoteSiswaRepository.save(quoteSiswa);
			
		} catch (Exception e) {
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		response.setMessage("Quote updated successfully");
		response.setData(quote);
		return new ResponseEntity<>(response, HttpStatus.OK);
		
	}

	public ResponseEntity<Object> getSiswa(String type, Pageable pageable) {
		BaseResponse response = new BaseResponse();

		try {
			Page<Siswa> siswaPage = siswaRepository.findByTitle(type, pageable);
			
			List<Siswa> modifiedSiswas = siswaPage.getContent().stream()
	                .map(this::modifyData) // Modify each Siswa entity
	                .toList();

	        response.setMessage("Data found successfully");
	        response.setData(new PageImpl<>(modifiedSiswas, pageable, siswaPage.getTotalElements()));
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}
	
	private Siswa modifyData(Siswa siswa) {
		List<ResponsePembelianSoal> pembelianSoals = new ArrayList<>();
		List<PembelianSoal> soals = pembelianSoalRepository.findBySiswa(siswa);
		soals.forEach(d ->{
			ResponsePembelianSoal data = ResponsePembelianSoal.builder()
					.namaSoal(d.getNamaSoal())
					.hargaSoal(d.getHargaSoal())
					.jenisSoal(d.getJenisSoal())
					.kodePromo(d.getKodePromo())
					.namaSiswa(d.getNamaSiswa()).build();
			pembelianSoals.add(data);
		});
		siswa.setPembelianSoals(pembelianSoals);
		return siswa;
	}
	
	public ResponseEntity<Object> getPembelianSoal(String uuidSiswa, Pageable pageable) {
		BaseResponse response = new BaseResponse();

		try {
			Optional<Siswa> siswaData = siswaRepository.findById(uuidSiswa);
			
			if(siswaData.isPresent()) {
				Page<PembelianSoal> soals = pembelianSoalRepository.findBySiswa(siswaData.get(), pageable);
				response.setMessage("Data found successfully");
		        response.setData(soals);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			
	        response.setMessage("Data not found");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}
	
	public ResponseEntity<Object> getDetailSiswa(String uuid) {
		BaseResponse response = new BaseResponse();

		try {
			Optional<Siswa> detailSiswa = siswaRepository.findById(uuid);
			if(detailSiswa.isPresent()) {
				Siswa getDetailSiswa = modifyData(detailSiswa.get());
				
				response.setMessage("Data found successfully");
		        response.setData(getDetailSiswa);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}

			response.setMessage("Data not found");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}
}
