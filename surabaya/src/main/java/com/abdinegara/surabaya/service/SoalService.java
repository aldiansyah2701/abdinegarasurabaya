package com.abdinegara.surabaya.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.abdinegara.surabaya.entity.BuatSoal;
import com.abdinegara.surabaya.entity.PembelajaranVideo;
import com.abdinegara.surabaya.entity.SoalAssetImage;
import com.abdinegara.surabaya.entity.SoalEssay;
import com.abdinegara.surabaya.entity.SoalPauli;
import com.abdinegara.surabaya.entity.SoalPilihanGanda;
import com.abdinegara.surabaya.entity.SoalTKD;
import com.abdinegara.surabaya.entity.Ujian;
import com.abdinegara.surabaya.entity.UjianAssetSoal;
import com.abdinegara.surabaya.message.BaseResponse;
import com.abdinegara.surabaya.message.RequestCreateSoalPauli;
import com.abdinegara.surabaya.message.RequestCreateUjian;
import com.abdinegara.surabaya.message.ResponseDetailUjian;
import com.abdinegara.surabaya.repository.BuatSoalRepository;
import com.abdinegara.surabaya.repository.PembelajaranVideoRepository;
import com.abdinegara.surabaya.repository.SoalAssetImageRepository;
import com.abdinegara.surabaya.repository.SoalEssayRepository;
import com.abdinegara.surabaya.repository.SoalPauliRepository;
import com.abdinegara.surabaya.repository.SoalPilihanGandaRepository;
import com.abdinegara.surabaya.repository.SoalTKDRepository;
import com.abdinegara.surabaya.repository.UjianAssetSoalRepository;
import com.abdinegara.surabaya.repository.UjianRepository;

import lombok.extern.slf4j.Slf4j;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
	
	@Autowired
	private SoalAssetImageRepository soalAssetImageRepository;
	
	@Autowired
	private PembelajaranVideoRepository pembelajaranVideoRepository;
	
	@Autowired
	private UjianRepository ujianRepository;
	
	@Autowired
	private UjianAssetSoalRepository ujianAssetSoalRepository;
	
	@Autowired
	private SoalTKDRepository soalTKDRepository;	
	
	@Value("${directory.soal.asset.image}")
	private String directoryAssetImage;
	
	@Value("${directory.soal.preview.image}")
	private String directoryPreviewImage;
	
	@Value("${directory.soal.video}")
	private String directoryVideo;
	
	@Value("${directory.base.path}")
	private String directoryBasePath;
	
	@Value("${directory.soal.tkd}")
	private String directoryTkd;
	


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
			MultipartFile file, MultipartFile[] images, String directory, SOALTYPE type, String jenis, Boolean isCanRevisi) {
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
//			uploadPath = file2.getAbsolutePath();
			uploadPath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file2.getAbsolutePath()
					: (directoryBasePath + directory);
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
				
				List<String> uuidsSoal = new ArrayList<>();
				if (SOALTYPE.PILIHANGANDA.equals(type)) {
					String uuidSoal = updatePilihanGanda("", namaSoal, durasi, jawaban, deskripsi, file, path, jenis, isCanRevisi);
					uuidsSoal.add(uuidSoal);
				} else if (SOALTYPE.ESSAY.equals(type)) {
					String uuidSoal = updateEssay("", namaSoal, durasi, jawaban, deskripsi, file, path, jenis);
					uuidsSoal.add(uuidSoal);
				}

//			}
				if(images != null && images.length > 0) {
					IntStream.range(0, images.length).forEach(index -> {
						MultipartFile imageFile = images[index];
						
						String uploadImagePath = "";
						try {
							Resource resource = resourceLoader.getResource("classpath:/static"+directoryAssetImage);
							File file2 = resource.getFile();
//							uploadImagePath = file2.getAbsolutePath();
							uploadImagePath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file2.getAbsolutePath()
									: (directoryBasePath + directoryAssetImage);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							
						}
						File uploadImageDir = new File(uploadImagePath);
						if (!uploadImageDir.exists()) {
							uploadImageDir.mkdir();
						}
						
						String uuidSoal = uuidsSoal.isEmpty() ? null : uuidsSoal.get(0);
						// Save the file to the soal folder
						File destImageFile = new File(uploadImageDir.getAbsolutePath() + File.separator + uuidSoal+"_"+imageFile.getOriginalFilename());
						try {
							imageFile.transferTo(destImageFile);
							String pathImage = directoryAssetImage;
							pathImage = pathImage + "/" + uuidSoal+"_"+imageFile.getOriginalFilename();
							SoalAssetImage soalAssetImage = new SoalAssetImage();
							soalAssetImage.setSoalType(type.toString());
							soalAssetImage.setUuidSoal(uuidSoal);
							soalAssetImage.setFilePath(pathImage);
							
							soalAssetImageRepository.save(soalAssetImage);
						}catch (Exception e) {
							e.printStackTrace();
						}						
					});
					
				}

			response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

	}
	
	@Transactional(readOnly = false)
	public ResponseEntity<Object> createSoalTKDWithUpload(String namaSoal, String durasi,  String deskripsi , String jenis,
			String jawabanTwk, MultipartFile filesTwk, MultipartFile[] imagesTwk,
			String jawabanTiu, MultipartFile filesTiu, MultipartFile[] imagesTiu,
			String jawabanTkp, MultipartFile filesTkp, MultipartFile[] imagesTkp) {
		BaseResponse response = new BaseResponse();
		
		Optional<SoalTKD> soalExist = soalTKDRepository.findByNamaSoal(namaSoal);
		if(soalExist.isPresent()) {
			response.setMessage("nama soal sudah tersedia");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}	

		
//		String uploadPath = servletContext.getRealPath("/static"+directory);
		String uploadPath = "";
		try {
			Resource resource = resourceLoader.getResource("classpath:/static"+directoryTkd);
			File file2 = resource.getFile();
//			uploadPath = file2.getAbsolutePath();
			uploadPath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file2.getAbsolutePath()
					: (directoryBasePath + directoryTkd);
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
	        File destFileTwk = new File(uploadDir.getAbsolutePath() + File.separator + filesTwk.getOriginalFilename());
	        filesTwk.transferTo(destFileTwk);
	        
	        File destFileTiu = new File(uploadDir.getAbsolutePath() + File.separator + filesTiu.getOriginalFilename());
	        filesTiu.transferTo(destFileTiu);
	        
	        File destFileTkp = new File(uploadDir.getAbsolutePath() + File.separator + filesTkp.getOriginalFilename());
	        filesTkp.transferTo(destFileTkp);
	        

				String pathTwk = directoryTkd;
				pathTwk = pathTwk + "/" + filesTwk.getOriginalFilename();
				
				String pathTiu = directoryTkd;
				pathTiu = pathTiu + "/" + filesTiu.getOriginalFilename();
				
				String pathTkp = directoryTkd;
				pathTkp = pathTkp + "/" + filesTkp.getOriginalFilename();
				
				List<String> uuidsSoal = new ArrayList<>();
				String uuidSoal = updateTKD("", namaSoal, durasi, deskripsi, jenis, 
						filesTwk, pathTwk, jawabanTwk,
						filesTiu, pathTiu, jawabanTiu,
						filesTkp, pathTkp, jawabanTkp);
				uuidsSoal.add(uuidSoal);

				if(imagesTwk != null && imagesTwk.length > 0) {
					IntStream.range(0, imagesTwk.length).forEach(index -> {
						MultipartFile imageFile = imagesTwk[index];
						
						String uploadImagePath = "";
						try {
							Resource resource = resourceLoader.getResource("classpath:/static"+directoryAssetImage);
							File file2 = resource.getFile();

							uploadImagePath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file2.getAbsolutePath()
									: (directoryBasePath + directoryAssetImage);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							
						}
						File uploadImageDir = new File(uploadImagePath);
						if (!uploadImageDir.exists()) {
							uploadImageDir.mkdir();
						}
						
						String uuidSoalGet = uuidsSoal.isEmpty() ? null : uuidsSoal.get(0);
						// Save the file to the soal folder
						File destImageFile = new File(uploadImageDir.getAbsolutePath() + File.separator + uuidSoalGet+"_"+imageFile.getOriginalFilename());
						try {
							imageFile.transferTo(destImageFile);
							String pathImage = directoryAssetImage;
							pathImage = pathImage + "/" + uuidSoalGet+"_"+imageFile.getOriginalFilename();
							SoalAssetImage soalAssetImage = new SoalAssetImage();
							soalAssetImage.setSoalType("TKD_TWK");
							soalAssetImage.setUuidSoal(uuidSoalGet);
							soalAssetImage.setFilePath(pathImage);
							
							soalAssetImageRepository.save(soalAssetImage);
						}catch (Exception e) {
							e.printStackTrace();
						}						
					});
			
				}
				
				if(imagesTiu != null && imagesTiu.length > 0) {
					IntStream.range(0, imagesTiu.length).forEach(index -> {
						MultipartFile imageFile = imagesTiu[index];
						
						String uploadImagePath = "";
						try {
							Resource resource = resourceLoader.getResource("classpath:/static"+directoryAssetImage);
							File file2 = resource.getFile();

							uploadImagePath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file2.getAbsolutePath()
									: (directoryBasePath + directoryAssetImage);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							
						}
						File uploadImageDir = new File(uploadImagePath);
						if (!uploadImageDir.exists()) {
							uploadImageDir.mkdir();
						}
						
						String uuidSoalGet = uuidsSoal.isEmpty() ? null : uuidsSoal.get(0);
						// Save the file to the soal folder
						File destImageFile = new File(uploadImageDir.getAbsolutePath() + File.separator + uuidSoalGet+"_"+imageFile.getOriginalFilename());
						try {
							imageFile.transferTo(destImageFile);
							String pathImage = directoryAssetImage;
							pathImage = pathImage + "/" + uuidSoalGet+"_"+imageFile.getOriginalFilename();
							SoalAssetImage soalAssetImage = new SoalAssetImage();
							soalAssetImage.setSoalType("TKD_TIU");
							soalAssetImage.setUuidSoal(uuidSoalGet);
							soalAssetImage.setFilePath(pathImage);
							
							soalAssetImageRepository.save(soalAssetImage);
						}catch (Exception e) {
							e.printStackTrace();
						}						
					});
			
				}
				
				if(imagesTkp != null && imagesTkp.length > 0) {
					IntStream.range(0, imagesTkp.length).forEach(index -> {
						MultipartFile imageFile = imagesTkp[index];
						
						String uploadImagePath = "";
						try {
							Resource resource = resourceLoader.getResource("classpath:/static"+directoryAssetImage);
							File file2 = resource.getFile();

							uploadImagePath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file2.getAbsolutePath()
									: (directoryBasePath + directoryAssetImage);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							
						}
						File uploadImageDir = new File(uploadImagePath);
						if (!uploadImageDir.exists()) {
							uploadImageDir.mkdir();
						}
						
						String uuidSoalGet = uuidsSoal.isEmpty() ? null : uuidsSoal.get(0);
						// Save the file to the soal folder
						File destImageFile = new File(uploadImageDir.getAbsolutePath() + File.separator + uuidSoalGet+"_"+imageFile.getOriginalFilename());
						try {
							imageFile.transferTo(destImageFile);
							String pathImage = directoryAssetImage;
							pathImage = pathImage + "/" + uuidSoalGet+"_"+imageFile.getOriginalFilename();
							SoalAssetImage soalAssetImage = new SoalAssetImage();
							soalAssetImage.setSoalType("TKD_TKP");
							soalAssetImage.setUuidSoal(uuidSoalGet);
							soalAssetImage.setFilePath(pathImage);
							
							soalAssetImageRepository.save(soalAssetImage);
						}catch (Exception e) {
							e.printStackTrace();
						}						
					});
			
				}

			response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

	}

	private String updateEssay(String uuid, String namaSoal, String durasi, String jawaban, String deskripsi, MultipartFile file,
			String path, String jenis) {
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
			soal.setJenis(jenis == null ? soal.getJenis(): jenis);

		} else {

			soal.setCreatedDate(new Date());
			soal.setNamaSoal(namaSoal);
			soal.setFilePath(path);
			soal.setDeskripsi(deskripsi);
			soal.setDurasi(durasi);
			soal.setJawaban(jawaban);
			soal.setJenis(jenis);
		}

		soalEssayRepository.save(soal);
		return soal.getUuid();
	}

	private String updatePilihanGanda(String uuid, String namaSoal, String durasi, String jawaban, String deskripsi,
			MultipartFile file, String path, String jenis, Boolean isCanRevisi) {
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
			soal.setJenis(jenis == null ? soal.getJenis(): jenis);
			soal.setCanRevisi(isCanRevisi == null? soal.isCanRevisi(): isCanRevisi);
//			soal.(isCanRevisi == false ? soal.isCanRevisi(): isCanRevisi);

		} else {

			soal.setCreatedDate(new Date());
			soal.setNamaSoal(namaSoal);
			soal.setFilePath(path);
			soal.setDeskripsi(deskripsi);
			soal.setDurasi(durasi);
			soal.setJawaban(jawaban);
			soal.setJenis(jenis);
			soal.setCanRevisi(isCanRevisi == null? false: isCanRevisi);
		}

		soalPilihanGandaRepository.save(soal);
		return soal.getUuid();
	}
	
	private String updateTKD(String uuid, String namaSoal, String durasi, String deskripsi, String jenis,
			MultipartFile filesTwk, String pathTwk, String jawabanTwk,
			MultipartFile filesTiu, String pathTiu, String jawabanTiu,
			MultipartFile filesTkp, String pathTkp, String jawabanTkp) {
		SoalTKD soal = new SoalTKD();
		Optional<SoalTKD> soalExist = soalTKDRepository.findById(uuid);
		if (soalExist.isPresent()) {
			soal = soalExist.get();
			soal.setUpdateDate(new Date());
			soal.setNamaSoal(namaSoal == null ? soal.getNamaSoal() : namaSoal);
			soal.setFilePathTwk(filesTwk == null ? soal.getFilePathTwk(): pathTwk);
			soal.setFilePathTiu(filesTiu == null ? soal.getFilePathTiu(): pathTiu);
			soal.setFilePathTkp(filesTkp == null ? soal.getFilePathTkp(): pathTkp);
			soal.setDeskripsi(deskripsi == null ? soal.getDeskripsi() : deskripsi);
			soal.setDurasi(durasi == null ? soal.getDurasi() : durasi);
			soal.setJawabanTwk(jawabanTwk == null ? soal.getJawabanTwk() : jawabanTwk);
			soal.setJawabanTiu(jawabanTiu == null ? soal.getJawabanTiu() : jawabanTiu);
			soal.setJawabanTkp(jawabanTkp == null ? soal.getJawabanTkp() : jawabanTkp);
			soal.setJenis(jenis == null ? soal.getJenis(): jenis);

		} else {

			soal.setCreatedDate(new Date());
			soal.setNamaSoal(namaSoal );
			soal.setFilePathTwk(pathTwk);
			soal.setFilePathTiu(pathTiu);
			soal.setFilePathTkp(pathTkp);
			soal.setDeskripsi(deskripsi);
			soal.setDurasi(durasi);
			soal.setJawabanTwk(jawabanTwk);
			soal.setJawabanTiu(jawabanTiu);
			soal.setJawabanTkp(jawabanTkp);
			soal.setJenis(jenis);
		}

		soalTKDRepository.save(soal);
		return soal.getUuid();
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
			soal.setJenis(request.getJenis() == null ? soal.getJenis() : request.getJenis());
		} else {

			soal.setCreatedDate(new Date());
			soal.setNamaSoal(request.getNamaSoal());
			soal.setDeskripsi(request.getDeskripsi());
			soal.setDurasi(request.getDurasi());
			soal.setJawaban(request.getJawaban());
			soal.setSoal(request.getSoal());
			soal.setJenis(request.getJenis());
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
				List<SoalAssetImage> assetImages = soalAssetImageRepository.findByUuidSoal(uuid);
				SoalPilihanGanda dataResp = data.get();
				dataResp.setAssetImage(assetImages);
				
				response.setData(data);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
		} else if (SOALTYPE.ESSAY.equals(type)) {
			Optional<SoalEssay> data = soalEssayRepository.findById(uuid);
			if (data.isPresent()) {
				List<SoalAssetImage> assetImages = soalAssetImageRepository.findByUuidSoal(uuid);
				SoalEssay dataResp = data.get();
				dataResp.setAssetImage(assetImages);
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
			MultipartFile file, MultipartFile[] images, String directory, SOALTYPE type, String jenis, boolean isCanRevisi) {
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
			Resource resource = resourceLoader.getResource("classpath:/static"+directory);
			File file2 = resource.getFile();
//			uploadPath = file2.getAbsolutePath();
			uploadPath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file2.getAbsolutePath()
					: (directoryBasePath + directory);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
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

			List<String> uuidsSoal = new ArrayList<>();
			if (SOALTYPE.PILIHANGANDA.equals(type)) {
				String uuidSoal = updatePilihanGanda(uuid, namaSoal, durasi, jawaban, deskripsi, file, path, jenis, isCanRevisi);
				uuidsSoal.add(uuidSoal);
			} else if (SOALTYPE.ESSAY.equals(type)) {
				String uuidSoal = updateEssay(uuid, namaSoal, durasi, jawaban, deskripsi, file, path, jenis);
				uuidsSoal.add(uuidSoal);
			}
			
			if(images != null && images.length > 0) {
				soalAssetImageRepository.deleteByUuidSoal(uuid);
				IntStream.range(0, images.length).forEach(index -> {
					MultipartFile imageFile = images[index];

					String uploadImagePath = "";
					try {
						Resource resource = resourceLoader.getResource("classpath:/static"+directoryAssetImage);
						File file2 = resource.getFile();
//						uploadImagePath = file2.getAbsolutePath();
						uploadImagePath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file2.getAbsolutePath()
								: (directoryBasePath + directoryAssetImage);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						
					}
					File uploadImageDir = new File(uploadImagePath);
					if (!uploadImageDir.exists()) {
						uploadImageDir.mkdir();
					}
					
					String uuidSoal = uuidsSoal.isEmpty() ? null : uuidsSoal.get(0);
					// Save the file to the soal folder
					File destImageFile = new File(uploadImageDir.getAbsolutePath() + File.separator + uuidSoal+"_"+imageFile.getOriginalFilename());
					try {
						imageFile.transferTo(destImageFile);
						String pathImage = directoryAssetImage;
						pathImage = pathImage + "/" + uuidSoal+"_"+imageFile.getOriginalFilename();
						SoalAssetImage soalAssetImage = new SoalAssetImage();
						soalAssetImage.setSoalType(type.toString());
						soalAssetImage.setUuidSoal(uuidSoal);
						soalAssetImage.setFilePath(pathImage);
						
						soalAssetImageRepository.save(soalAssetImage);
					}catch (Exception e) {
						e.printStackTrace();
					}	
					
				});
				
			}

			response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}
	
	@Transactional(readOnly = false)
	public ResponseEntity<Object> updateSoalTKDWithUpload(String uuid, String namaSoal, String durasi,  String deskripsi , String jenis,
			String jawabanTwk, MultipartFile filesTwk, MultipartFile[] imagesTwk,
			String jawabanTiu, MultipartFile filesTiu, MultipartFile[] imagesTiu,
			String jawabanTkp, MultipartFile filesTkp, MultipartFile[] imagesTkp) {
		BaseResponse response = new BaseResponse();
		
		Optional<SoalTKD> soalExist = soalTKDRepository.findById(uuid);
		if(!soalExist.isPresent()) {
			response.setMessage("soal tidak tersedia");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		
//		String uploadPath = servletContext.getRealPath("/static"+directory);
		String uploadPath = "";
		try {
			Resource resource = resourceLoader.getResource("classpath:/static"+directoryTkd);
			File file2 = resource.getFile();
//			uploadPath = file2.getAbsolutePath();
			uploadPath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file2.getAbsolutePath()
					: (directoryBasePath + directoryTkd);
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
			String pathTwk = "";
			if(filesTwk != null) {
				File destFileTwk = new File(uploadDir.getAbsolutePath() + File.separator + filesTwk.getOriginalFilename());
				filesTwk.transferTo(destFileTwk);
				pathTwk = directoryTkd;
				pathTwk = pathTwk + "/" + filesTwk.getOriginalFilename();
			}
			
			String pathTiu = "";
			if(filesTwk != null) {
				File destFileTiu = new File(uploadDir.getAbsolutePath() + File.separator + filesTiu.getOriginalFilename());
				filesTiu.transferTo(destFileTiu);
				pathTiu = directoryTkd;
				pathTiu = pathTiu + "/" + filesTiu.getOriginalFilename();
			}
			
			String pathTkp = "";
			if(filesTwk != null) {
				File destFileTkp = new File(uploadDir.getAbsolutePath() + File.separator + filesTkp.getOriginalFilename());
				filesTkp.transferTo(destFileTkp);
				pathTkp = directoryTkd;
				pathTkp = pathTkp + "/" + filesTkp.getOriginalFilename();
			}
	        
	        		
				List<String> uuidsSoal = new ArrayList<>();
				String uuidSoal = updateTKD(uuid, namaSoal, durasi, deskripsi, jenis, 
						filesTwk, pathTwk, jawabanTwk,
						filesTiu, pathTiu, jawabanTiu,
						filesTkp, pathTkp, jawabanTkp);
				uuidsSoal.add(uuidSoal);

				if(imagesTwk != null && imagesTwk.length > 0) {
					soalAssetImageRepository.deleteByUuidSoalAndSoalType(uuidSoal, "TKD_TWK");
					IntStream.range(0, imagesTwk.length).forEach(index -> {
						MultipartFile imageFile = imagesTwk[index];
						
						String uploadImagePath = "";
						try {
							Resource resource = resourceLoader.getResource("classpath:/static"+directoryAssetImage);
							File file2 = resource.getFile();

							uploadImagePath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file2.getAbsolutePath()
									: (directoryBasePath + directoryAssetImage);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							
						}
						File uploadImageDir = new File(uploadImagePath);
						if (!uploadImageDir.exists()) {
							uploadImageDir.mkdir();
						}
						
						String uuidSoalGet = uuidsSoal.isEmpty() ? null : uuidsSoal.get(0);
						// Save the file to the soal folder
						File destImageFile = new File(uploadImageDir.getAbsolutePath() + File.separator + uuidSoalGet+"_"+imageFile.getOriginalFilename());
						try {
							imageFile.transferTo(destImageFile);
							String pathImage = directoryAssetImage;
							pathImage = pathImage + "/" + uuidSoalGet+"_"+imageFile.getOriginalFilename();
							SoalAssetImage soalAssetImage = new SoalAssetImage();
							soalAssetImage.setSoalType("TKD_TWK");
							soalAssetImage.setUuidSoal(uuidSoalGet);
							soalAssetImage.setFilePath(pathImage);
							
							soalAssetImageRepository.save(soalAssetImage);
						}catch (Exception e) {
							e.printStackTrace();
						}						
					});
			
				}
				
				if(imagesTiu != null && imagesTiu.length > 0) {
					soalAssetImageRepository.deleteByUuidSoalAndSoalType(uuidSoal, "TKD_TIU");
					IntStream.range(0, imagesTiu.length).forEach(index -> {
						MultipartFile imageFile = imagesTiu[index];
						
						String uploadImagePath = "";
						try {
							Resource resource = resourceLoader.getResource("classpath:/static"+directoryAssetImage);
							File file2 = resource.getFile();

							uploadImagePath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file2.getAbsolutePath()
									: (directoryBasePath + directoryAssetImage);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							
						}
						File uploadImageDir = new File(uploadImagePath);
						if (!uploadImageDir.exists()) {
							uploadImageDir.mkdir();
						}
						
						String uuidSoalGet = uuidsSoal.isEmpty() ? null : uuidsSoal.get(0);
						// Save the file to the soal folder
						File destImageFile = new File(uploadImageDir.getAbsolutePath() + File.separator + uuidSoalGet+"_"+imageFile.getOriginalFilename());
						try {
							imageFile.transferTo(destImageFile);
							String pathImage = directoryAssetImage;
							pathImage = pathImage + "/" + uuidSoalGet+"_"+imageFile.getOriginalFilename();
							SoalAssetImage soalAssetImage = new SoalAssetImage();
							soalAssetImage.setSoalType("TKD_TIU");
							soalAssetImage.setUuidSoal(uuidSoalGet);
							soalAssetImage.setFilePath(pathImage);
							
							soalAssetImageRepository.save(soalAssetImage);
						}catch (Exception e) {
							e.printStackTrace();
						}						
					});
			
				}
				
				if(imagesTkp != null && imagesTkp.length > 0) {
					soalAssetImageRepository.deleteByUuidSoalAndSoalType(uuidSoal, "TKD_TKP");
					IntStream.range(0, imagesTkp.length).forEach(index -> {
						MultipartFile imageFile = imagesTkp[index];
						
						String uploadImagePath = "";
						try {
							Resource resource = resourceLoader.getResource("classpath:/static"+directoryAssetImage);
							File file2 = resource.getFile();

							uploadImagePath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file2.getAbsolutePath()
									: (directoryBasePath + directoryAssetImage);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							
						}
						File uploadImageDir = new File(uploadImagePath);
						if (!uploadImageDir.exists()) {
							uploadImageDir.mkdir();
						}
						
						String uuidSoalGet = uuidsSoal.isEmpty() ? null : uuidsSoal.get(0);
						// Save the file to the soal folder
						File destImageFile = new File(uploadImageDir.getAbsolutePath() + File.separator + uuidSoalGet+"_"+imageFile.getOriginalFilename());
						try {
							imageFile.transferTo(destImageFile);
							String pathImage = directoryAssetImage;
							pathImage = pathImage + "/" + uuidSoalGet+"_"+imageFile.getOriginalFilename();
							SoalAssetImage soalAssetImage = new SoalAssetImage();
							soalAssetImage.setSoalType("TKD_TKP");
							soalAssetImage.setUuidSoal(uuidSoalGet);
							soalAssetImage.setFilePath(pathImage);
							
							soalAssetImageRepository.save(soalAssetImage);
						}catch (Exception e) {
							e.printStackTrace();
						}						
					});
			
				}

			response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

	}
	
	@Transactional(readOnly = false)
	public ResponseEntity<Object> uploadImagePreview(String username, MultipartFile[] images) {
		BaseResponse response = new BaseResponse();
		UUID uuid = UUID.randomUUID();
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = currentDate.format(formatter);
        
		List<String> pathImages = new ArrayList<>();
		if(images != null && images.length > 0) {
			
			IntStream.range(0, images.length).forEach(index -> {
				MultipartFile imageFile = images[index];

				String uploadImagePath = "";
//				if(directoryBasePath == "" || directoryBasePath.isEmpty()) {
				try {
					Resource resource = resourceLoader.getResource("classpath:/static" + directoryPreviewImage);
					File file2 = resource.getFile();
					uploadImagePath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file2.getAbsolutePath()
							: (directoryBasePath + directoryPreviewImage);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				}
					
//				} else {
//					uploadImagePath = directoryBasePath+directoryPreviewImage;
//				}
				
				File uploadImageDir = new File(uploadImagePath);
				if (!uploadImageDir.exists()) {
					uploadImageDir.mkdir();
				}
				String uuidText = uuid.toString();
				// Save the file to the soal folder
				File destImageFile = new File(uploadImageDir.getAbsolutePath() + File.separator + formattedDate+"_"+uuidText+"_"+imageFile.getOriginalFilename());
				try {
					imageFile.transferTo(destImageFile);
					String pathImage = directoryPreviewImage;
					pathImage = pathImage + "/" + formattedDate+"_"+uuidText+"_"+imageFile.getOriginalFilename();

					pathImages.add(pathImage);
				}catch (Exception e) {
					e.printStackTrace();
				}					
			});
			
		}
		response.setData(pathImages);
		response.setMessage(BaseResponse.SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@Transactional(readOnly = false)
	public ResponseEntity<Object> uploadPembelajaranVideo(String namaVideo, String deskripsi, String jenis,
			MultipartFile video) {
		BaseResponse response = new BaseResponse();
		
		Optional<PembelajaranVideo> dataIsExist = pembelajaranVideoRepository.findByNamaVideo(namaVideo);
		if(dataIsExist.isPresent()) {
			response.setMessage("nama video sudah tersedia");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		String uploadVideoPath = "";
		try {
			Resource resource = resourceLoader.getResource("classpath:/static" + directoryVideo);
			File file2 = resource.getFile();
//			uploadVideoPath = file2.getAbsolutePath();
			uploadVideoPath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file2.getAbsolutePath()
					: (directoryBasePath + directoryVideo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		File uploadVideoDir = new File(uploadVideoPath);
		if (!uploadVideoDir.exists()) {
			uploadVideoDir.mkdir();
		}
//				String uuidText = uuid.toString();
		// Save the file to the soal folder
		File destVideoFile = new File(uploadVideoDir.getAbsolutePath() + File.separator + video.getOriginalFilename());
		try {
			video.transferTo(destVideoFile);
			String pathVideo = directoryVideo;
			pathVideo = pathVideo + "/" + video.getOriginalFilename();

			PembelajaranVideo data = new PembelajaranVideo();
			data.setCreatedDate(new Date());
			data.setDeskripsi(deskripsi);
			data.setFilePath(pathVideo);
			
			String key = "zzzzzzzzzzzzzzzz"; // 16, 24, or 32 bytes
		    String iv = "1234567890123456"; // Exactly 16 bytes
		    String pathVideoEnryp = "";
	        try {
				String encryptedData = encrypt(pathVideo, key, iv);
				System.out.println("this.filePath " + pathVideo);
				System.out.println("encryptedData " + encryptedData);
				pathVideoEnryp = encryptedData;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
			data.setFilePathEncrypt(pathVideoEnryp);
			data.setJenis(jenis);
			data.setNamaVideo(namaVideo);

			pembelajaranVideoRepository.save(data);

		} catch (Exception e) {
			e.printStackTrace();
		}

		response.setMessage(BaseResponse.SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@Transactional(readOnly = false)
	public ResponseEntity<Object> uploadUpdatePembelajaranVideo(String uuid, String namaVideo, String deskripsi, String jenis,
			MultipartFile video) {
		BaseResponse response = new BaseResponse();
		
		Optional<PembelajaranVideo> dataIsExist = pembelajaranVideoRepository.findById(uuid);
		if(!dataIsExist.isPresent()) {
			response.setMessage("nama video tidak tersedia");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		PembelajaranVideo data = dataIsExist.get();
		data.setUpdateDate(new Date());
		data.setDeskripsi(deskripsi == null ? data.getDeskripsi():deskripsi);
		data.setJenis(jenis == null ? data.getJenis():jenis);
		data.setNamaVideo(namaVideo == null ? data.getNamaVideo():namaVideo);
		
		if(video != null) {
			String uploadVideoPath = "";
			try {
				Resource resource = resourceLoader.getResource("classpath:/static" + directoryVideo);
				File file2 = resource.getFile();
//				uploadVideoPath = file2.getAbsolutePath();
				uploadVideoPath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file2.getAbsolutePath()
						: (directoryBasePath + directoryVideo);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
			File uploadVideoDir = new File(uploadVideoPath);
			if (!uploadVideoDir.exists()) {
				uploadVideoDir.mkdir();
			}
//				String uuidText = uuid.toString();
			// Save the file to the soal folder
			File destVideoFile = new File(uploadVideoDir.getAbsolutePath() + File.separator + video.getOriginalFilename());
			try {
				video.transferTo(destVideoFile);
				String pathVideo = directoryVideo;
				pathVideo = pathVideo + "/" + video.getOriginalFilename();
				
				
				data.setFilePath(pathVideo);
				
				String key = "zzzzzzzzzzzzzzzz"; // 16, 24, or 32 bytes
				String iv = "1234567890123456"; // Exactly 16 bytes
				String pathVideoEnryp = "";
				try {
					String encryptedData = encrypt(pathVideo, key, iv);
					System.out.println("this.filePath " + pathVideo);
					System.out.println("encryptedData " + encryptedData);
					pathVideoEnryp = encryptedData;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				data.setFilePathEncrypt(pathVideoEnryp);
				
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		pembelajaranVideoRepository.save(data);

		response.setMessage(BaseResponse.SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	public ResponseEntity<Object> getVideos(Pageable pageable) {
		BaseResponse response = new BaseResponse();
		response.setMessage("Data found successfully");	
		Page<PembelajaranVideo> data = pembelajaranVideoRepository.findAll(pageable);
		response.setData(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	
	}
	
	public ResponseEntity<Object> getVideo(String uuid) {
		BaseResponse response = new BaseResponse();
		response.setMessage("Data found successfully");	
		Optional<PembelajaranVideo> data = pembelajaranVideoRepository.findById(uuid);
		response.setData(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	
	}
	
	public ResponseEntity<Object> deleteVideo(String uuid) {
		BaseResponse response = new BaseResponse();
		response.setMessage("Delete data successfully");
		Optional<PembelajaranVideo> dataIsExist = pembelajaranVideoRepository.findById(uuid);
		if (dataIsExist.isPresent()) {
			pembelajaranVideoRepository.deleteById(uuid);

			return new ResponseEntity<>(response, HttpStatus.OK);
		}

		response.setMessage("Data not found");
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

	}
	
	 public static String encrypt(String plainText, String key, String iv) throws Exception {
	        byte[] keyBytes = key.getBytes();
	        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

	        byte[] ivBytes = iv.getBytes();
	        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);

	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
	        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));

	        return Base64.getEncoder().encodeToString(encryptedBytes);
	    }
	 
	 public ResponseEntity<Object> downloadSoal(String path) {
			BaseResponse response = new BaseResponse();
			
			
			try {
				Resource resource = resourceLoader.getResource("classpath:/static"+path);
//				File file = resource.getFile();
				
				File file  = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? resource.getFile()
						: ( new File(directoryBasePath + path));
				
				InputStream inputStream = new FileInputStream(file);
		        
		        // Wrap the FileInputStream with InputStreamResource
		        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
		        return ResponseEntity.ok()
	                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" +"soal.xlsx")
	                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
	                    .body(inputStreamResource);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			response.setMessage(BaseResponse.FAILED);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

		}

		@Transactional(readOnly = false)
		public ResponseEntity<Object> createUjian(RequestCreateUjian request) {
			BaseResponse response = new BaseResponse();
			
			Optional<Ujian> dataUjian = ujianRepository.findByNamaUjian(request.getNamaUjian());	
			if(dataUjian.isPresent()) {
				response.setMessage("nama ujian sudah tersedia");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			Ujian ujian = new Ujian();
			ujian.setCreatedDate(new Date());
			ujian.setDeksripsi(request.getDeskripsi());
			ujian.setHarga(request.getHarga());
			ujian.setJenis(request.getJenis());
			ujian.setNamaUjian(request.getNamaUjian());

			ujianRepository.save(ujian);

			request.getUuidSoalEssay().forEach(data -> {
				UjianAssetSoal soalUjian = new UjianAssetSoal();
				soalUjian.setSoalType("ESSAY");
				soalUjian.setUuidSoal(data);
				soalUjian.setUuidUjian(data);
				soalUjian.setUuidUjian(ujian.getUuid());
				ujianAssetSoalRepository.save(soalUjian);

			});

			request.getUuidSoalPilihanGanda().forEach(data -> {
				UjianAssetSoal soalUjian = new UjianAssetSoal();
				soalUjian.setSoalType("PILIHANGANDA");
				soalUjian.setUuidSoal(data);
				soalUjian.setUuidUjian(data);
				soalUjian.setUuidUjian(ujian.getUuid());
				ujianAssetSoalRepository.save(soalUjian);
			});

			request.getUuidSoalPauli().forEach(data -> {
				UjianAssetSoal soalUjian = new UjianAssetSoal();
				soalUjian.setSoalType("PAULI");
				soalUjian.setUuidSoal(data);
				soalUjian.setUuidUjian(data);
				soalUjian.setUuidUjian(ujian.getUuid());
				ujianAssetSoalRepository.save(soalUjian);
			});

			request.getUuidSoalVideo().forEach(data -> {
				UjianAssetSoal soalUjian = new UjianAssetSoal();
				soalUjian.setSoalType("VIDEO");
				soalUjian.setUuidSoal(data);
				soalUjian.setUuidUjian(data);
				soalUjian.setUuidUjian(ujian.getUuid());
				ujianAssetSoalRepository.save(soalUjian);
			});

			response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);

		}
	 
		public ResponseEntity<Object> getListUjian(Pageable pageable) {
			BaseResponse response = new BaseResponse();
			response.setMessage("Data found successfully");
			Page<Ujian> data = ujianRepository.findAll(pageable);
			response.setData(data);
			return new ResponseEntity<>(response, HttpStatus.OK);

		}
		
		public ResponseEntity<Object> getDetailUjian(String uuid) {
			BaseResponse response = new BaseResponse();
			response.setMessage("Data found successfully");
			Optional<Ujian> dataUjian = ujianRepository.findById(uuid);
			ResponseDetailUjian responseUjian = new ResponseDetailUjian();
			
			if(dataUjian.isPresent()) {			
				responseUjian.setUjian(dataUjian.get());
				 List<SoalPilihanGanda> detailPilihanGandas = new ArrayList<SoalPilihanGanda>();
				 List<SoalEssay> detailEssays = new ArrayList<SoalEssay>();
				 List<SoalPauli> detailPaulis= new ArrayList<SoalPauli>();
				 List<PembelajaranVideo> detailVideos = new ArrayList<PembelajaranVideo>();
				
				List<UjianAssetSoal> soals = ujianAssetSoalRepository.findByUuidUjian(uuid);
				soals.forEach(soal ->{
					if ("PILIHANGANDA".equals(soal.getSoalType())) {
						Optional<SoalPilihanGanda> data = soalPilihanGandaRepository.findById(soal.getUuidSoal());
						if (data.isPresent()) {
							List<SoalAssetImage> assetImages = soalAssetImageRepository.findByUuidSoal(soal.getUuidSoal());
							SoalPilihanGanda dataResp = data.get();
							dataResp.setAssetImage(assetImages);
							detailPilihanGandas.add(dataResp);
							
						}
					} else if ("ESSAY".equals(soal.getSoalType())) {
						Optional<SoalEssay> data = soalEssayRepository.findById(soal.getUuidSoal());
						if (data.isPresent()) {
							List<SoalAssetImage> assetImages = soalAssetImageRepository.findByUuidSoal(soal.getUuidSoal());
							SoalEssay dataResp = data.get();
							dataResp.setAssetImage(assetImages);

							detailEssays.add(dataResp);
							
						}
					} else if ("PAULI".equals(soal.getSoalType())) {
						Optional<SoalPauli> data = soalPauliRepository.findById(soal.getUuidSoal());
						if (data.isPresent()) {
							detailPaulis.add(data.get());
						}
					} else if ("VIDEO".equals(soal.getSoalType())) {
						Optional<PembelajaranVideo> data = pembelajaranVideoRepository.findById(soal.getUuidSoal());
						if (data.isPresent()) {
							detailVideos.add(data.get());
						}
					}
				});
				
				responseUjian.setDetailEssays(detailEssays);
				responseUjian.setDetailPaulis(detailPaulis);
				responseUjian.setDetailPilihanGandas(detailPilihanGandas);
				responseUjian.setDetailVideos(detailVideos);
				
				response.setData(responseUjian);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			
			
			response.setMessage("Data not found");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

		}

		public ResponseEntity<Object> deleteUjian(String uuid) {
			BaseResponse response = new BaseResponse();
			response.setMessage("Delete data successfully");
			Optional<Ujian> dataUjian = ujianRepository.findById(uuid);
			if (dataUjian.isPresent()) {
				ujianRepository.deleteById(uuid);

				List<UjianAssetSoal> soals = ujianAssetSoalRepository.findByUuidUjian(uuid);
				if (soals.isEmpty()) {
					ujianAssetSoalRepository.deleteByUuidUjian(uuid);
				}
				return new ResponseEntity<>(response, HttpStatus.OK);
			}

			response.setMessage("Data not found");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

		}
		
		@Transactional(readOnly = false)
		public ResponseEntity<Object> updateUjian(RequestCreateUjian request, String uuid) {
			BaseResponse response = new BaseResponse();
			Optional<Ujian> dataUjian = ujianRepository.findById(uuid);
			if(!dataUjian.isPresent()) {
				response.setMessage("ujian tidak ditemukan");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			
			Ujian ujian = dataUjian.get();
			ujian.setUpdateDate(new Date());
			ujian.setDeksripsi(request.getDeskripsi() == null ? ujian.getDeksripsi():request.getDeskripsi());
			ujian.setHarga(request.getHarga() == null ? ujian.getHarga():request.getHarga());
			ujian.setJenis(request.getJenis() == null ? ujian.getJenis():request.getJenis());
			ujian.setNamaUjian(request.getNamaUjian() == null ? ujian.getNamaUjian():request.getNamaUjian());

			ujianRepository.save(ujian);
			
			if(request.getUuidSoalEssay() != null) {
				ujianAssetSoalRepository.deleteBySoalTypeAndUuidUjian("ESSAY", uuid);
				request.getUuidSoalEssay().forEach(data -> {
					UjianAssetSoal soalUjian = new UjianAssetSoal();
					soalUjian.setSoalType("ESSAY");
					soalUjian.setUuidSoal(data);
					soalUjian.setUuidUjian(data);
					soalUjian.setUuidUjian(ujian.getUuid());
					ujianAssetSoalRepository.save(soalUjian);
					
				});				
			}

			if(request.getUuidSoalPilihanGanda() != null) {
				ujianAssetSoalRepository.deleteBySoalTypeAndUuidUjian("PILIHANGANDA", uuid);
				request.getUuidSoalPilihanGanda().forEach(data -> {
					UjianAssetSoal soalUjian = new UjianAssetSoal();
					soalUjian.setSoalType("PILIHANGANDA");
					soalUjian.setUuidSoal(data);
					soalUjian.setUuidUjian(data);
					soalUjian.setUuidUjian(ujian.getUuid());
					ujianAssetSoalRepository.save(soalUjian);
				});				
			}

			if(request.getUuidSoalPauli() != null) {
				ujianAssetSoalRepository.deleteBySoalTypeAndUuidUjian("PAULI", uuid);
				request.getUuidSoalPauli().forEach(data -> {
					UjianAssetSoal soalUjian = new UjianAssetSoal();
					soalUjian.setSoalType("PAULI");
					soalUjian.setUuidSoal(data);
					soalUjian.setUuidUjian(data);
					soalUjian.setUuidUjian(ujian.getUuid());
					ujianAssetSoalRepository.save(soalUjian);
				});				
			}

			if(request.getUuidSoalVideo() != null) {
				ujianAssetSoalRepository.deleteBySoalTypeAndUuidUjian("VIDEO", uuid);
				request.getUuidSoalVideo().forEach(data -> {
					UjianAssetSoal soalUjian = new UjianAssetSoal();
					soalUjian.setSoalType("VIDEO");
					soalUjian.setUuidSoal(data);
					soalUjian.setUuidUjian(data);
					soalUjian.setUuidUjian(ujian.getUuid());
					ujianAssetSoalRepository.save(soalUjian);
				});			
			}
			
			response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);

		}

}
