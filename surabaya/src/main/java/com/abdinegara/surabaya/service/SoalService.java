package com.abdinegara.surabaya.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;

import com.abdinegara.surabaya.entity.*;
import com.abdinegara.surabaya.message.*;
import com.abdinegara.surabaya.repository.*;
import com.dropbox.core.DbxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;
import freemarker.template.Configuration;

import lombok.extern.slf4j.Slf4j;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;

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

	@Autowired
	private SoalGanjilGenapRepository soalGanjilGenapRepository;

	@Autowired
	private SoalHilangRepository soalHilangRepository;
	
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


	@Value("${directory.bukti.transfer}")
	private String directoryBuktiTransfer;

	@Value("${dropbox.accessToken}")
	private String accessToken;

	@Value("${mail.send.from}")
	String mailSendFrom;

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	Configuration fmConfiguration;

	@Autowired
	private PembelianUjianRepository pembelianUjianRepository;

	@Autowired
	private SiswaRepository siswaRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JawabanSiswaRepository jawabanSiswaRepository;


	private static final String UPLOAD_DIR = "C:\\Users\\Dell3420\\Documents\\abdinegaraexel";

	public enum SOALTYPE {
		PILIHANGANDA, ESSAY, PAULI, TKD, SOALHILANG, GANJILGENAP, TKD_TIU, TKD_TKP, TKD_TWK
	}

	public enum APPROVAL {
		NEED_APPROVAL, DECLINE, APPROVE
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

	@Transactional(readOnly = false)
	public ResponseEntity<Object> createSoalGanjilGenap(String uuid, RequestCreateSoalPauli request) {
		BaseResponse response = new BaseResponse();
		Optional<SoalGanjilGenap> soalExist = soalGanjilGenapRepository.findByNamaSoal(request.getNamaSoal());
		if(soalExist.isPresent() && "".equals(uuid)) {
			response.setMessage("nama soal sudah tersedia");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		Optional<SoalGanjilGenap> soalUpdateExist = soalGanjilGenapRepository.findById(uuid);
		if(!soalUpdateExist.isPresent() && !"".equals(uuid)) {
			response.setMessage("soal tidak tersedia");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		SoalGanjilGenap soal = new SoalGanjilGenap();
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

		soalGanjilGenapRepository.save(soal);
		response.setMessage(BaseResponse.SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> createSoalHilang(String uuid, RequestCreateSoalHilang request) {
		BaseResponse response = new BaseResponse();
		Optional<SoalHilang> soalExist = soalHilangRepository.findByNamaSoal(request.getNamaSoal());
		if(soalExist.isPresent() && "".equals(uuid)) {
			response.setMessage("nama soal sudah tersedia");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		Optional<SoalHilang> soalUpdateExist = soalHilangRepository.findById(uuid);
		if(!soalUpdateExist.isPresent() && !"".equals(uuid)) {
			response.setMessage("soal tidak tersedia");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		SoalHilang soal = new SoalHilang();
		if (soalUpdateExist.isPresent()) {
			soal = soalUpdateExist.get();
			soal.setUpdateDate(new Date());

			soal.setNamaSoal(request.getNamaSoal() == null ? soal.getNamaSoal() : request.getNamaSoal());
			soal.setDeskripsi(request.getDeskripsi() == null ? soal.getDeskripsi() : request.getDeskripsi());
			soal.setDurasi(request.getDurasi() == null ? soal.getDurasi() : request.getDurasi());
			soal.setJawaban(request.getJawaban() == null ? soal.getJawaban() : request.getJawaban());
			soal.setSoal(request.getSoal() == null ? soal.getSoal() : request.getSoal());
			soal.setMasterSoal(request.getMasterSoal() == null ? soal.getMasterSoal() : request.getMasterSoal());
			soal.setJenis(request.getJenis() == null ? soal.getJenis() : request.getJenis());
		} else {

			soal.setCreatedDate(new Date());
			soal.setNamaSoal(request.getNamaSoal());
			soal.setDeskripsi(request.getDeskripsi());
			soal.setDurasi(request.getDurasi());
			soal.setJawaban(request.getJawaban());
			soal.setSoal(request.getSoal());
			soal.setMasterSoal(request.getMasterSoal());
			soal.setJenis(request.getJenis());
		}

		soalHilangRepository.save(soal);
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
		} else if (SOALTYPE.TKD.equals(type)) {
			Page<SoalTKD> data = soalTKDRepository.findAll(pageable);
			response.setData(data);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else if (SOALTYPE.GANJILGENAP.equals(type)) {
			Page<SoalGanjilGenap> data = soalGanjilGenapRepository.findAll(pageable);
			response.setData(data);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else if (SOALTYPE.SOALHILANG.equals(type)) {
			Page<SoalHilang> data = soalHilangRepository.findAll(pageable);
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
				
				response.setData(dataResp);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
		} else if (SOALTYPE.ESSAY.equals(type)) {
			Optional<SoalEssay> data = soalEssayRepository.findById(uuid);
			if (data.isPresent()) {
				List<SoalAssetImage> assetImages = soalAssetImageRepository.findByUuidSoal(uuid);
				SoalEssay dataResp = data.get();
				dataResp.setAssetImage(assetImages);
				response.setData(dataResp);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
		} else if (SOALTYPE.PAULI.equals(type)) {
			Optional<SoalPauli> data = soalPauliRepository.findById(uuid);
			if (data.isPresent()) {

				response.setData(data);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
		} else if (SOALTYPE.TKD.equals(type)) {
			Optional<SoalTKD> data = soalTKDRepository.findById(uuid);
			if (data.isPresent()) {
				List<SoalAssetImage> assetImagesTwk = soalAssetImageRepository.findByUuidSoalAndSoalType(uuid, "TKD_TWK");
				List<SoalAssetImage> assetImagesTiu = soalAssetImageRepository.findByUuidSoalAndSoalType(uuid, "TKD_TIU");
				List<SoalAssetImage> assetImagesTkp = soalAssetImageRepository.findByUuidSoalAndSoalType(uuid, "TKD_TKP");
				SoalTKD dataResp = data.get();
				dataResp.setAssetImageTwk(assetImagesTwk);
				dataResp.setAssetImageTiu(assetImagesTiu);
				dataResp.setAssetImageTkp(assetImagesTkp);
				
				response.setData(dataResp);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
		} else if (SOALTYPE.GANJILGENAP.equals(type)) {
			Optional<SoalGanjilGenap> data = soalGanjilGenapRepository.findById(uuid);
			if (data.isPresent()) {

				response.setData(data);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
		} else if (SOALTYPE.SOALHILANG.equals(type)) {
			Optional<SoalHilang> data = soalHilangRepository.findById(uuid);
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
		} else if (SOALTYPE.TKD.equals(type)) {
			Optional<SoalTKD> data = soalTKDRepository.findById(uuid);
			if (data.isPresent()) {
				soalTKDRepository.deleteById(uuid);
				return new ResponseEntity<>(response, HttpStatus.OK);

			}
		} else if (SOALTYPE.GANJILGENAP.equals(type)) {
			Optional<SoalGanjilGenap> data = soalGanjilGenapRepository.findById(uuid);
			if (data.isPresent()) {
				soalGanjilGenapRepository.deleteById(uuid);
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

				/*try {
					uploadFile(imageFile, "/preview-image/"+imageFile.getOriginalFilename());
					log.info( "File uploaded successfully!");
				} catch (IOException | UploadErrorException e) {
					log.info("Failed to upload file: " + e.getMessage());
				} catch (DbxException e) {
					throw new RuntimeException(e);
				}*/

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

	private DbxClientV2 getClient() {
		DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/abdi_negara").build();
		return new DbxClientV2(config, accessToken);
	}

	public void uploadFile(MultipartFile file, String path) throws IOException, DbxException {
		try (InputStream in = file.getInputStream()) {
			getClient().files().uploadBuilder(path)
					.uploadAndFinish(in);
		}
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
			ujian.setDeskripsi(request.getDeskripsi());
			ujian.setHarga(request.getHarga());
			ujian.setJenis(request.getJenis());
			ujian.setNamaUjian(request.getNamaUjian());

			ujianRepository.save(ujian);

			request.getUuidSoalEssay().forEach(data -> {
				UjianAssetSoal soalUjian = new UjianAssetSoal();
				soalUjian.setSoalType("ESSAY");
				soalUjian.setUuidSoal(data);
				soalUjian.setUuidUjian(ujian.getUuid());
				ujianAssetSoalRepository.save(soalUjian);

			});

			request.getUuidSoalPilihanGanda().forEach(data -> {
				UjianAssetSoal soalUjian = new UjianAssetSoal();
				soalUjian.setSoalType("PILIHANGANDA");
				soalUjian.setUuidSoal(data);
				soalUjian.setUuidUjian(ujian.getUuid());
				ujianAssetSoalRepository.save(soalUjian);
			});

			request.getUuidSoalPauli().forEach(data -> {
				UjianAssetSoal soalUjian = new UjianAssetSoal();
				soalUjian.setSoalType("PAULI");
				soalUjian.setUuidSoal(data);
				soalUjian.setUuidUjian(ujian.getUuid());
				ujianAssetSoalRepository.save(soalUjian);
			});

			request.getUuidSoalVideo().forEach(data -> {
				UjianAssetSoal soalUjian = new UjianAssetSoal();
				soalUjian.setSoalType("VIDEO");
				soalUjian.setUuidSoal(data);
				soalUjian.setUuidUjian(ujian.getUuid());
				ujianAssetSoalRepository.save(soalUjian);
			});
			
			request.getUuidSoalTKD().forEach(data -> {
				UjianAssetSoal soalUjian = new UjianAssetSoal();
				soalUjian.setSoalType("TKD");
				soalUjian.setUuidSoal(data);
				soalUjian.setUuidUjian(ujian.getUuid());
				ujianAssetSoalRepository.save(soalUjian);
			});

			request.getUuidSoalHilang().forEach(data -> {
				UjianAssetSoal soalUjian = new UjianAssetSoal();
				soalUjian.setSoalType("SOALHILANG");
				soalUjian.setUuidSoal(data);
				soalUjian.setUuidUjian(ujian.getUuid());
				ujianAssetSoalRepository.save(soalUjian);
			});

			request.getUuidSoalGanjilGenap().forEach(data -> {
				UjianAssetSoal soalUjian = new UjianAssetSoal();
				soalUjian.setSoalType("GANJILGENAP");
				soalUjian.setUuidSoal(data);
				soalUjian.setUuidUjian(ujian.getUuid());
				ujianAssetSoalRepository.save(soalUjian);
			});


			response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);

		}
	 
		public ResponseEntity<Object> getListUjian(String jenis, Pageable pageable) {
			BaseResponse response = new BaseResponse();
			response.setMessage("Data found successfully");

			if(jenis == null){
				Page<Ujian> data = ujianRepository.findAll(pageable);
				response.setData(data);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			Page<Ujian> data = ujianRepository.findByJenis(jenis, pageable);
			response.setData(data);
			return new ResponseEntity<>(response, HttpStatus.OK);

		}
		
		public ResponseEntity<Object> getDetailUjian(String uuid, String userUuid) {
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
				 List<SoalTKD> detailTKDs= new ArrayList<SoalTKD>();
				 List<SoalHilang> detailSoalHilangs= new ArrayList<SoalHilang>();
				 List<SoalGanjilGenap> detailGanjilGenaps= new ArrayList<SoalGanjilGenap>();

				List<UjianAssetSoal> soals = ujianAssetSoalRepository.findByUuidUjian(uuid);
				soals.forEach(soal ->{
					if ("PILIHANGANDA".equals(soal.getSoalType())) {
						Optional<SoalPilihanGanda> data = soalPilihanGandaRepository.findById(soal.getUuidSoal());
						if (data.isPresent()) {
							List<SoalAssetImage> assetImages = soalAssetImageRepository.findByUuidSoal(soal.getUuidSoal());
							SoalPilihanGanda dataResp = data.get();
							dataResp.setAssetImage(assetImages);

							Optional<JawabanSiswa> jawabanSiswaExist = jawabanSiswaRepository.findFirstByUjianUuidAndSoalUuidAndUserUuidAndSoalTypeOrderByCreatedDateDesc(
									uuid, dataResp.getUuid(), userUuid, SOALTYPE.PILIHANGANDA.toString());
							dataResp.setNilai(jawabanSiswaExist.isPresent() ? jawabanSiswaExist.get().getNilai() : null);

							detailPilihanGandas.add(dataResp);
							
						}
					} else if ("ESSAY".equals(soal.getSoalType())) {
						Optional<SoalEssay> data = soalEssayRepository.findById(soal.getUuidSoal());
						if (data.isPresent()) {
							List<SoalAssetImage> assetImages = soalAssetImageRepository.findByUuidSoal(soal.getUuidSoal());
							SoalEssay dataResp = data.get();
							dataResp.setAssetImage(assetImages);

							Optional<JawabanSiswa> jawabanSiswaExist = jawabanSiswaRepository.findFirstByUjianUuidAndSoalUuidAndUserUuidAndSoalTypeOrderByCreatedDateDesc(
									uuid, dataResp.getUuid(), userUuid, SOALTYPE.ESSAY.toString());
							dataResp.setNilai(jawabanSiswaExist.isPresent() ? jawabanSiswaExist.get().getNilai() : null);

							detailEssays.add(dataResp);
							
						}
					} else if ("PAULI".equals(soal.getSoalType())) {
						Optional<SoalPauli> data = soalPauliRepository.findById(soal.getUuidSoal());
						if (data.isPresent()) {
							SoalPauli dataResp = data.get();
							Optional<JawabanSiswa> jawabanSiswaExist = jawabanSiswaRepository.findFirstByUjianUuidAndSoalUuidAndUserUuidAndSoalTypeOrderByCreatedDateDesc(
									uuid, dataResp.getUuid(), userUuid, SOALTYPE.PAULI.toString());
							dataResp.setNilai(jawabanSiswaExist.isPresent() ? jawabanSiswaExist.get().getNilai() : null);

							detailPaulis.add(dataResp);
						}
					} else if ("VIDEO".equals(soal.getSoalType())) {
						Optional<PembelajaranVideo> data = pembelajaranVideoRepository.findById(soal.getUuidSoal());
						if (data.isPresent()) {
							detailVideos.add(data.get());
						}
					} else if ("TKD".equals(soal.getSoalType())) {
						Optional<SoalTKD> data = soalTKDRepository.findById(soal.getUuidSoal());
						if (data.isPresent()) {
							List<SoalAssetImage> assetImagesTwk = soalAssetImageRepository.findByUuidSoalAndSoalType(uuid, "TKD_TWK");
							List<SoalAssetImage> assetImagesTiu = soalAssetImageRepository.findByUuidSoalAndSoalType(uuid, "TKD_TIU");
							List<SoalAssetImage> assetImagesTkp = soalAssetImageRepository.findByUuidSoalAndSoalType(uuid, "TKD_TKP");
							SoalTKD dataResp = data.get();
							dataResp.setAssetImageTwk(assetImagesTwk);
							dataResp.setAssetImageTiu(assetImagesTiu);
							dataResp.setAssetImageTkp(assetImagesTkp);

							Optional<JawabanSiswa> jawabanTwkExist = jawabanSiswaRepository.findFirstByUjianUuidAndSoalUuidAndUserUuidAndSoalTypeOrderByCreatedDateDesc(
									uuid, dataResp.getUuid(), userUuid, SOALTYPE.TKD_TWK.toString());
							dataResp.setNilaiTwk(jawabanTwkExist.isPresent() ? jawabanTwkExist.get().getNilai() : null);

							Optional<JawabanSiswa> jawabanTiuExist = jawabanSiswaRepository.findFirstByUjianUuidAndSoalUuidAndUserUuidAndSoalTypeOrderByCreatedDateDesc(
									uuid, dataResp.getUuid(), userUuid, SOALTYPE.TKD_TIU.toString());
							dataResp.setNilaiTiu(jawabanTiuExist.isPresent() ? jawabanTiuExist.get().getNilai() : null);

							Optional<JawabanSiswa> jawabanTkpExist = jawabanSiswaRepository.findFirstByUjianUuidAndSoalUuidAndUserUuidAndSoalTypeOrderByCreatedDateDesc(
									uuid, dataResp.getUuid(), userUuid, SOALTYPE.TKD_TKP.toString());
							dataResp.setNilaiTkp(jawabanTkpExist.isPresent() ? jawabanTkpExist.get().getNilai() : null);


							detailTKDs.add(dataResp);
							
						}
					} else if ("SOALHILANG".equals(soal.getSoalType())) {
						Optional<SoalHilang> data = soalHilangRepository.findById(soal.getUuidSoal());
						if (data.isPresent()) {
							SoalHilang dataResp = data.get();
							Optional<JawabanSiswa> jawabanSiswaExist = jawabanSiswaRepository.findFirstByUjianUuidAndSoalUuidAndUserUuidAndSoalTypeOrderByCreatedDateDesc(
									uuid, dataResp.getUuid(), userUuid, SOALTYPE.SOALHILANG.toString());
							dataResp.setNilai(jawabanSiswaExist.isPresent() ? jawabanSiswaExist.get().getNilai() : null);

							detailSoalHilangs.add(dataResp);
						}
					} else if ("GANJILGENAP".equals(soal.getSoalType())) {
						Optional<SoalGanjilGenap> data = soalGanjilGenapRepository.findById(soal.getUuidSoal());
						if (data.isPresent()) {
							SoalGanjilGenap dataResp = data.get();
							Optional<JawabanSiswa> jawabanSiswaExist = jawabanSiswaRepository.findFirstByUjianUuidAndSoalUuidAndUserUuidAndSoalTypeOrderByCreatedDateDesc(
									uuid, dataResp.getUuid(), userUuid, SOALTYPE.GANJILGENAP.toString());
							dataResp.setNilai(jawabanSiswaExist.isPresent() ? jawabanSiswaExist.get().getNilai() : null);

							detailGanjilGenaps.add(dataResp);
						}
					}
				});
				
				responseUjian.setDetailEssays(detailEssays);
				responseUjian.setDetailPaulis(detailPaulis);
				responseUjian.setDetailPilihanGandas(detailPilihanGandas);
				responseUjian.setDetailVideos(detailVideos);
				responseUjian.setDetailTKDs(detailTKDs);
				responseUjian.setDetailSoalHilangs(detailSoalHilangs);
				responseUjian.setDetailGanjilGenaps(detailGanjilGenaps);
				
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
				Optional<PembelianUjian> sudahBeliUjian = pembelianUjianRepository.findByUjianUuid(uuid);
				if(sudahBeliUjian.isPresent()){
					response.setMessage("Data ujian has been purchased");
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}

				ujianRepository.deleteById(uuid);

				List<UjianAssetSoal> soals = ujianAssetSoalRepository.findByUuidUjian(uuid);
				if (!soals.isEmpty()) {
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
			ujian.setDeskripsi(request.getDeskripsi() == null ? ujian.getDeskripsi():request.getDeskripsi());
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
					soalUjian.setUuidUjian(ujian.getUuid());
					ujianAssetSoalRepository.save(soalUjian);
				});			
			}
			
			if(request.getUuidSoalTKD() != null) {
				ujianAssetSoalRepository.deleteBySoalTypeAndUuidUjian("TKD", uuid);
				request.getUuidSoalTKD().forEach(data -> {
					UjianAssetSoal soalUjian = new UjianAssetSoal();
					soalUjian.setSoalType("TKD");
					soalUjian.setUuidSoal(data);
					soalUjian.setUuidUjian(ujian.getUuid());
					ujianAssetSoalRepository.save(soalUjian);
				});				
			}

			if(request.getUuidSoalHilang() != null) {
				ujianAssetSoalRepository.deleteBySoalTypeAndUuidUjian("SOALHILANG", uuid);
				request.getUuidSoalHilang().forEach(data -> {
					UjianAssetSoal soalUjian = new UjianAssetSoal();
					soalUjian.setSoalType("SOALHILANG");
					soalUjian.setUuidSoal(data);
					soalUjian.setUuidUjian(ujian.getUuid());
					ujianAssetSoalRepository.save(soalUjian);
				});
			}

			if(request.getUuidSoalGanjilGenap() != null) {
				ujianAssetSoalRepository.deleteBySoalTypeAndUuidUjian("GANJILGENAP", uuid);
				request.getUuidSoalGanjilGenap().forEach(data -> {
					UjianAssetSoal soalUjian = new UjianAssetSoal();
					soalUjian.setSoalType("GANJILGENAP");
					soalUjian.setUuidSoal(data);
					soalUjian.setUuidUjian(ujian.getUuid());
					ujianAssetSoalRepository.save(soalUjian);
				});
			}
			
			response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);

		}

	public void testMail() {
		Map<String, Object> model = new HashMap<>();
		model.put("tanggal","Surabaya, 19 April 2024");
		model.put("name","Aldiansyah");
		model.put("rekening","1234567");
		model.put("bank","BSI");
		model.put("tagihan","50.000");
		model.put("notagihan","UUID");
		model.put("email","aldiansyahxramadlan@gmail.com");
		model.put("deskripsi","Ujian porli tingkat 3");
		model.put("duedate","15 Apr 2024 00:20:28");
		sendEmail("","aldiansyahxramadlan@gmail.com","Tagihan test",model, null);
	}
	@Async
	public void sendEmail(String type, String to , String subject, Map<String, Object> model, String[] bcc) {
		MimeMessage message = javaMailSender.createMimeMessage();

		try {
			log.info("prepare send email to {}", to);
			log.info("prepare send email bcc {}", bcc);
			log.info("prepare send email subject {}", subject);

			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);

			mimeMessageHelper.setFrom(mailSendFrom);
			mimeMessageHelper.setSubject(subject);
			mimeMessageHelper.setTo(to);

			if(bcc != null){
				mimeMessageHelper.setBcc(bcc);
			}

			String mailContent = null;
			if ("tagihan_siswa".equals(type)) {
				mailContent = getMailOtpContentFromTemplate(model, "template-tagihan-siswa.flth");
			} else if ("pelunasan_siswa".equals(type)) {
				mailContent = getMailOtpContentFromTemplate(model, "template-pelunasan-siswa.flth");
			} else if ("tagihan_admin".equals(type)) {
				mailContent = getMailOtpContentFromTemplate(model, "template-tagihan-admin.flth");
			} else if ("pelunasan_admin".equals(type)) {
				mailContent = getMailOtpContentFromTemplate(model, "template-pelunasan-admin.flth");
			} else if ("approval_siswa".equals(type)) {
				mailContent = getMailOtpContentFromTemplate(model, "template-approval-siswa.flth");
			}  else if ("otp".equals(type)) {
				mailContent = getMailOtpContentFromTemplate(model, "template-otpMail.flth");
			}else {
				mailContent = getMailOtpContentFromTemplate(model, "template.flth");
			}



			mimeMessageHelper.setText(mailContent, true);

			javaMailSender.send(mimeMessageHelper.getMimeMessage());
			log.info("success send email");
		} catch (Exception e) {
			log.info("error sendMailHTML : {}", e.getMessage());
		}
	}

	private String getMailOtpContentFromTemplate(Map<String, Object> model, String template) {
		StringBuffer content = new StringBuffer();

		try {
			content.append(FreeMarkerTemplateUtils
					.processTemplateIntoString(fmConfiguration.getTemplate(template), model));
		} catch (Exception e) {
			log.error("error getMailOtpContentFromTemplate : {}", e.getMessage());
		}
		return content.toString();
	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> beliUjian(RequestBeliUjian request) {
		Optional<PembelianUjian> beliUjian = pembelianUjianRepository.findByUjianUuidAndUserUuid(request.getUjianUuid(), request.getUserUuid());
		BaseResponse response = new BaseResponse();
		if(beliUjian.isPresent()){
			response.setMessage("ujian sudah dibeli");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		Optional<Ujian> ujian = ujianRepository.findById(request.getUjianUuid());

		if(!ujian.isPresent()){
			response.setMessage("ujian tidak tersedia");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		Siswa siswa = siswaRepository.findByUserUuid(request.getUserUuid());
		if(siswa == null){
			response.setMessage("siswa tidak tersedia");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		PembelianUjian pembelianUjian = new PembelianUjian();
		pembelianUjian.setUjianUuid(request.getUjianUuid());
		pembelianUjian.setUserUuid(request.getUserUuid());
		pembelianUjian.setStatus("BELUM_BAYAR");
		pembelianUjian.setApproval(APPROVAL.NEED_APPROVAL.toString());
		Date createdDate = new Date();
		pembelianUjian.setCreatedDate(createdDate);
		pembelianUjianRepository.save(pembelianUjian);

		// send email harap bayar ke siswa

		SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID")); // Indonesian locale for month names

		// Format the date to the desired string
		String formattedDate = sdf.format(createdDate);

		NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

		// Format the number to currency
		String harga = ujian.get().getHarga();
		String formattedNumber = nf.format(Integer.parseInt(harga));

		Map<String, Object> model = new HashMap<>();
		model.put("tanggal",formattedDate);
		model.put("name",siswa.getName());
		model.put("rekening","123456789");
		model.put("bank","BSI");
		model.put("tagihan",formattedNumber);
		model.put("notagihan",pembelianUjian.getUuid());
		model.put("email",siswa.getEmail());
		model.put("deskripsi",ujian.get().getDeskripsi());
		sendEmail("tagihan_siswa", siswa.getEmail(),"Tagihan Pembelian Ujian : " + ujian.get().getNamaUjian(),model, null);


		// send email ke semua admin ada siswa yang beli soal
		Map<String, Object> modelAdmin = new HashMap<>();
		modelAdmin.put("tanggal",formattedDate);
		modelAdmin.put("name",siswa.getName());
		modelAdmin.put("tagihan",formattedNumber);
		modelAdmin.put("notagihan",pembelianUjian.getUuid());
		modelAdmin.put("email",siswa.getEmail());
		modelAdmin.put("deskripsi",ujian.get().getDeskripsi());

		List<String> emailAdmin = userRepository.findEmailAdmin();
		String[] emailAdminArray = emailAdmin.toArray(new String[0]);

		sendEmail("tagihan_admin", emailAdminArray[0],"Tagihan Pembelian Ujian : " + ujian.get().getNamaUjian(), modelAdmin, emailAdminArray);

		response.setMessage("Pembelian ujian berhasil");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> listApprovalBeliUjian(APPROVAL approval, Pageable pageable) {
		BaseResponse response = new BaseResponse();

		Page<PembelianUjian> pembelianUjian = null;
		if(approval == null ){
			pembelianUjian =  pembelianUjianRepository.findAll(pageable);

		} else {
			pembelianUjian =  pembelianUjianRepository.findByApproval(approval.toString(), pageable);

		}

		List<PembelianUjian> modifiedPembelianUjian = pembelianUjian.getContent().stream()
						.map(this::modifyData)
								.toList();

		response.setData(new PageImpl<>(modifiedPembelianUjian, pageable, pembelianUjian.getTotalElements()));
		response.setMessage("Data found successfully");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> detailApprovalBeliUjian(String uuid) {
		BaseResponse response = new BaseResponse();

		Optional<PembelianUjian> pembelianUjian = pembelianUjianRepository.findById(uuid);

		PembelianUjian pembelianUjianData = pembelianUjian.get();
		pembelianUjianData = modifyData(pembelianUjianData);


		response.setData(pembelianUjianData);
		response.setMessage("Data found successfully");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> historyBeliUjian(String userUuid, Pageable pageable) {
		BaseResponse response = new BaseResponse();

		Page<PembelianUjian> pembelianUjian =  pembelianUjianRepository.findByUserUuid(userUuid, pageable);

		List<PembelianUjian> modifiedPembelianUjian = pembelianUjian.getContent().stream()
				.map(this::modifyData)
				.toList();

		response.setData(new PageImpl<>(modifiedPembelianUjian, pageable, pembelianUjian.getTotalElements()));


		response.setMessage("Data found successfully");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> approvalsBeliUjian(APPROVAL approval, String userUuid, String ujianUuid, String adminName, String remark) {
		BaseResponse response = new BaseResponse();

		Optional<PembelianUjian> pembelianUjian =  pembelianUjianRepository.findByUjianUuidAndUserUuid(ujianUuid, userUuid);
		PembelianUjian data = pembelianUjian.get();
		data.setApproveBy(adminName);
		data.setApproval(approval.toString());
		data.setRemark(remark);
		pembelianUjianRepository.save(data);

		Siswa siswa = siswaRepository.findByUserUuid(pembelianUjian.get().getUserUuid());
		Optional<Ujian> ujian = ujianRepository.findById(pembelianUjian.get().getUjianUuid());

		Date createdDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID")); // Indonesian locale for month names

		// Format the date to the desired string
		String formattedDate = sdf.format(createdDate);

		NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

		// Format the number to currency
		String harga = ujian.get().getHarga();
		String formattedNumber = nf.format(Integer.parseInt(harga));
		// send email to siswa

		Map<String, Object> model = new HashMap<>();
		model.put("tanggal",formattedDate);
		model.put("status",approval.toString());
		model.put("name",siswa.getName());
		model.put("tagihan",formattedNumber);
		model.put("notagihan",pembelianUjian.get().getUuid());
		model.put("email",siswa.getEmail());
		model.put("deskripsi",ujian.get().getDeskripsi());
		sendEmail("approval_siswa", siswa.getEmail(),"Approval Pembelian Ujian : " + ujian.get().getNamaUjian(),model, null);


		response.setMessage("Data update successfully");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> totalBeliUjian() {
		BaseResponse response = new BaseResponse();

		List<PembelianUjian> pembelianUjian =  pembelianUjianRepository.findAll();

		BigDecimal total = BigDecimal.ZERO;
		for(PembelianUjian data : pembelianUjian){
			Optional<Ujian> ujian = ujianRepository.findById(data.getUjianUuid());
			BigDecimal amount = new BigDecimal(ujian.get().getHarga());

			total = total.add(amount);
		}

		response.setData(total);

		response.setMessage("Data found successfully");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	private PembelianUjian modifyData(PembelianUjian data){
		Optional<Ujian> dataUjian = ujianRepository.findById(data.getUjianUuid());
		Siswa siswa = siswaRepository.findByUserUuid(data.getUserUuid());
		data.setDetailUjian(dataUjian.get());
		data.setNamaSiswa( siswa == null ? "" : siswa.getName());
		return data;
	}

	public ResponseEntity<Object> deleteBeliUjian(String uuid) {
		BaseResponse response = new BaseResponse();
		response.setMessage("Delete data successfully");
		Optional<PembelianUjian> dataUjian = pembelianUjianRepository.findById(uuid);
		if (dataUjian.isPresent()) {
			pembelianUjianRepository.deleteById(uuid);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}

		response.setMessage("Data not found");
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> uploadTransferBeliUjian(String ujianUuid, String userUuid, String rekening, MultipartFile file) {
		BaseResponse response = new BaseResponse();

		String uploadPath = "";
		try {
			Resource resource = resourceLoader.getResource("classpath:/static"+directoryBuktiTransfer);
			File file2 = resource.getFile();
//			uploadPath = file2.getAbsolutePath();
			uploadPath = (directoryBasePath == "" || directoryBasePath.isEmpty()) ? file2.getAbsolutePath()
					: (directoryBasePath + directoryBuktiTransfer);
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

			String path = directoryBuktiTransfer;
			path = path + "/" + file.getOriginalFilename();


			Optional<PembelianUjian> pembelianUjian =  pembelianUjianRepository.findByUjianUuidAndUserUuid(ujianUuid, userUuid);
			PembelianUjian data = pembelianUjian.get();
			data.setRekening(rekening);
			data.setFilePath(path);
			data.setStatus("SUDAH_BAYAR");
			pembelianUjianRepository.save(data);

			Siswa siswa = siswaRepository.findByUserUuid(pembelianUjian.get().getUserUuid());
			Optional<Ujian> ujian = ujianRepository.findById(pembelianUjian.get().getUjianUuid());

			Date createdDate = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID")); // Indonesian locale for month names

			// Format the date to the desired string
			String formattedDate = sdf.format(createdDate);

			NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

			// Format the number to currency
			String harga = ujian.get().getHarga();
			String formattedNumber = nf.format(Integer.parseInt(harga));
			// send email to siswa

			Map<String, Object> model = new HashMap<>();
			model.put("tanggal",formattedDate);
			model.put("status","PAYMENT VERIFICATION PROCESS");
			model.put("name",siswa.getName());
			model.put("tagihan",formattedNumber);
			model.put("notagihan",pembelianUjian.get().getUuid());
			model.put("email",siswa.getEmail());
			model.put("deskripsi",ujian.get().getDeskripsi());
			sendEmail("pelunasan_siswa", siswa.getEmail(),"Pembayaran Pembelian Ujian : " + ujian.get().getNamaUjian(),model, null);


			// send email to admin
			Map<String, Object> modelAdmin = new HashMap<>();
			modelAdmin.put("tanggal",formattedDate);
			modelAdmin.put("name",siswa.getName());
			modelAdmin.put("tagihan",formattedNumber);
			modelAdmin.put("notagihan",pembelianUjian.get().getUuid());
			modelAdmin.put("email",siswa.getEmail());
			modelAdmin.put("deskripsi",ujian.get().getDeskripsi());

			List<String> emailAdmin = userRepository.findEmailAdmin();
			String[] emailAdminArray = emailAdmin.toArray(new String[0]);

			sendEmail("pelunasan_admin", emailAdminArray[0],"Pembayaran Pembelian Ujian : " + ujian.get().getNamaUjian(), modelAdmin, emailAdminArray);


			response.setMessage(BaseResponse.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			response.setMessage(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> jawabanUjian(RequestJawabanSiswaTKD request) {
		BaseResponse response = new BaseResponse();
		Optional<JawabanSiswa> jawabanSiswaExist = jawabanSiswaRepository.findFirstByUjianUuidAndSoalUuidAndUserUuidAndSoalTypeOrderByCreatedDateDesc(
				request.getUjianUuid(), request.getSoalUuid(), request.getUserUuid(), request.getSoalType());
		if(jawabanSiswaExist.isPresent()){
			response.setMessage("jawaban already submit");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		JawabanSiswa jawaban = new JawabanSiswa();
		jawaban.setJawaban(request.getJawaban());
		jawaban.setSoalUuid(request.getSoalUuid());
		jawaban.setUjianUuid(request.getUjianUuid());
		jawaban.setUserUuid(request.getUserUuid());
		jawaban.setSoalType(request.getSoalType());

		if (SOALTYPE.SOALHILANG.equals(SOALTYPE.valueOf(request.getSoalType()))){
			Optional<SoalHilang> byId = soalHilangRepository.findById(request.getSoalUuid());

			SoalHilang soal = byId.get();
			List<String> jawabanSiswa = convertStringToList(request.getJawaban(), "|");
			List<String> jawabanSoal = convertStringToList(soal.getJawaban(), "|");

//			IntStream.range(0, jawabanSoal.size()).forEach(index ->{
//			});
			int jawabanBenar = 0;
			for(int index=0; index<jawabanSoal.size(); index++){
				if(jawabanSoal.get(index).equalsIgnoreCase(jawabanSiswa.get(index))){
					jawabanBenar = jawabanBenar + 1;
				}
			}
			BigDecimal nilai = calculateResult(jawabanSoal.size(), jawabanBenar);
			jawaban.setJawabanSoal(soal.getJawaban());
			jawaban.setNilai(nilai.toString());

		} else if (SOALTYPE.GANJILGENAP.equals(SOALTYPE.valueOf(request.getSoalType()))){
			Optional<SoalGanjilGenap> byId = soalGanjilGenapRepository.findById(request.getSoalUuid());

			SoalGanjilGenap soal = byId.get();
			List<String> jawabanSiswa = convertStringToList(request.getJawaban(), ",");
			List<String> jawabanSoal = convertStringToList(soal.getJawaban(), ",");

			int jawabanBenar = 0;
			for(int index=0; index<jawabanSoal.size(); index++){
				if(jawabanSoal.get(index).equalsIgnoreCase(jawabanSiswa.get(index))){
					jawabanBenar = jawabanBenar + 1;
				}
			}
			BigDecimal nilai = calculateResult(jawabanSoal.size(), jawabanBenar);
			jawaban.setJawabanSoal(soal.getJawaban());
			jawaban.setNilai(nilai.toString());

		} else if (SOALTYPE.ESSAY.equals(SOALTYPE.valueOf(request.getSoalType()))){
			Optional<SoalEssay> byId = soalEssayRepository.findById(request.getSoalUuid());
			SoalEssay soal = byId.get();
			List<String> jawabanSiswa = convertStringToList(request.getJawaban(), ",");
			List<String> jawabanSoal = convertStringToList(soal.getJawaban(), "\\|");

			int jawabanBenar = 0;
			for(int index=0; index<jawabanSoal.size(); index++){
				List<String> jawabanSoalDetail = convertStringToList(jawabanSoal.get(index), ",");

				for(String data : jawabanSoalDetail){
					if(data.equalsIgnoreCase(jawabanSiswa.get(index))){
						jawabanBenar = jawabanBenar + 1;
						break;
					}
				}

			}
			BigDecimal nilai = calculateResult(jawabanSoal.size(), jawabanBenar);
			jawaban.setJawabanSoal(soal.getJawaban());
			jawaban.setNilai(nilai.toString());
		} else if (SOALTYPE.PAULI.equals(SOALTYPE.valueOf(request.getSoalType()))){
			Optional<SoalPauli> byId = soalPauliRepository.findById(request.getSoalUuid());
			SoalPauli soal = byId.get();
			List<String> jawabanSiswa = convertStringToList(request.getJawaban(), ",");
			List<String> jawabanSoal = convertStringToList(soal.getJawaban(), ",");

			int jawabanBenar = 0;
			for(int index=0; index<jawabanSoal.size(); index++){
				if(jawabanSoal.get(index).equalsIgnoreCase(jawabanSiswa.get(index))){
					jawabanBenar = jawabanBenar + 1;
				}
			}
			BigDecimal nilai = calculateResult(jawabanSoal.size(), jawabanBenar);
			jawaban.setJawabanSoal(soal.getJawaban());
			jawaban.setNilai(nilai.toString());

		} else if (SOALTYPE.PILIHANGANDA.equals(SOALTYPE.valueOf(request.getSoalType()))){
			Optional<SoalPilihanGanda> byId = soalPilihanGandaRepository.findById(request.getSoalUuid());
			SoalPilihanGanda soal = byId.get();
			List<String> jawabanSiswa = convertStringToList(request.getJawaban(), ",");
			List<String> jawabanSoal = convertStringToList(soal.getJawaban(), ",");

			int jawabanBenar = 0;
			for(int index=0; index<jawabanSoal.size(); index++){
				if(jawabanSoal.get(index).equalsIgnoreCase(jawabanSiswa.get(index))){
					jawabanBenar = jawabanBenar + 1;
				}
			}
			BigDecimal nilai = calculateResult(jawabanSoal.size(), jawabanBenar);
			jawaban.setJawabanSoal(soal.getJawaban());
			jawaban.setNilai(nilai.toString());

		} else if (SOALTYPE.TKD_TIU.equals(SOALTYPE.valueOf(request.getSoalType()))){
			Optional<SoalTKD> byId = soalTKDRepository.findById(request.getSoalUuid());
			SoalTKD soal = byId.get();
			List<String> jawabanSiswa = convertStringToList(request.getJawabanTIU(), ",");
			List<String> jawabanSoal = convertStringToList(soal.getJawabanTiu(), ",");

			int jawabanBenar = 0;
			for(int index=0; index<jawabanSoal.size(); index++){
				if(jawabanSoal.get(index).equalsIgnoreCase(jawabanSiswa.get(index))){
					jawabanBenar = jawabanBenar + 1;
				}
			}
			BigDecimal nilai = calculateResult(jawabanSoal.size(), jawabanBenar);
			jawaban.setJawabanSoal(soal.getJawabanTiu());
			jawaban.setNilai(nilai.toString());
			jawaban.setJawaban(request.getJawabanTIU());

		}  else if (SOALTYPE.TKD_TKP.equals(SOALTYPE.valueOf(request.getSoalType()))){
			Optional<SoalTKD> byId = soalTKDRepository.findById(request.getSoalUuid());
			SoalTKD soal = byId.get();
			List<String> jawabanSiswa = convertStringToList(request.getJawabanTKP(), ",");
			List<String> jawabanSoal = convertStringToList(soal.getJawabanTkp(), ",");

			int jawabanBenar = 0;
			for(int index=0; index<jawabanSoal.size(); index++){
				if(jawabanSoal.get(index).equalsIgnoreCase(jawabanSiswa.get(index))){
					jawabanBenar = jawabanBenar + 1;
				}
			}
			BigDecimal nilai = calculateResult(jawabanSoal.size(), jawabanBenar);
			jawaban.setJawabanSoal(soal.getJawabanTkp());
			jawaban.setNilai(nilai.toString());
			jawaban.setJawaban(request.getJawabanTKP());
		} else if (SOALTYPE.TKD_TWK.equals(SOALTYPE.valueOf(request.getSoalType()))){
			Optional<SoalTKD> byId = soalTKDRepository.findById(request.getSoalUuid());
			SoalTKD soal = byId.get();
			List<String> jawabanSiswa = convertStringToList(request.getJawabanTWK(), ",");
			List<String> jawabanSoal = convertStringToList(soal.getJawabanTwk(), ",");

			int jawabanBenar = 0;
			for(int index=0; index<jawabanSoal.size(); index++){
				if(jawabanSoal.get(index).equalsIgnoreCase(jawabanSiswa.get(index))){
					jawabanBenar = jawabanBenar + 1;
				}
			}
			BigDecimal nilai = calculateResult(jawabanSoal.size(), jawabanBenar);
			jawaban.setJawabanSoal(soal.getJawabanTwk());
			jawaban.setNilai(nilai.toString());
			jawaban.setJawaban(request.getJawabanTWK());
		}

		jawaban.setCreatedDate(new Date());
		jawabanSiswaRepository.save(jawaban);
		response.setData(jawaban.getNilai());
		response.setMessage("Success");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	private BigDecimal calculateResult(int soal, int jawabanBenar ){
		BigDecimal divisor = BigDecimal.valueOf(soal);
		BigDecimal result = BigDecimal.valueOf(100).divide(divisor, 2, RoundingMode.HALF_UP);
		result = result.multiply(BigDecimal.valueOf(jawabanBenar));

		result = result.setScale(1, RoundingMode.HALF_UP);
		return result;
	}

	private List<String> convertStringToList(String data, String bySplit){
		String[] wordsArray = data.split(bySplit);
		List<String> wordsList = Arrays.asList(wordsArray);
		return wordsList;
	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> jawabanUjianSiswa(String userUuid, String ujianUuid) {
		BaseResponse response = new BaseResponse();
		List<JawabanSiswa> jawabanUjianSiswa = jawabanSiswaRepository.findByUjianUuidAndUserUuid(
				ujianUuid,userUuid);

		response.setMessage("Success");
		response.setData(jawabanUjianSiswa);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Transactional(readOnly = false)
	public ResponseEntity<Object> jawabanUjianAllSiswa(String userUuid, String soalType) {
		BaseResponse response = new BaseResponse();
		List<JawabanSiswa> jawabanUjianSiswa = new ArrayList<>();
		if(soalType != null && !"".equals(soalType)){
			jawabanUjianSiswa = jawabanSiswaRepository.findByUserUuidAndSoalTypeStartsWith(userUuid, soalType);
		} else {
			jawabanUjianSiswa = jawabanSiswaRepository.findByUserUuid(userUuid);
		}

		List<JawabanSiswa> modifiedJawabanSiswa = jawabanUjianSiswa.stream()
				.map(this::modifyDataHistoryUjian)
				.toList();


		response.setMessage("Success");
		response.setData(modifiedJawabanSiswa);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	private JawabanSiswa modifyDataHistoryUjian(JawabanSiswa jawaban){
		Optional<Ujian> dataUjian = ujianRepository.findById(jawaban.getUjianUuid());
		String soalName = "";
		if ("PILIHANGANDA".equals(jawaban.getSoalType())) {
			Optional<SoalPilihanGanda> data = soalPilihanGandaRepository.findById(jawaban.getSoalUuid());
			if (data.isPresent()) {
				soalName = data.get().getNamaSoal();

			}
		} else if ("ESSAY".equals(jawaban.getSoalType())) {
			Optional<SoalEssay> data = soalEssayRepository.findById(jawaban.getSoalUuid());
			if (data.isPresent()) {
				soalName = data.get().getNamaSoal();

			}
		} else if ("PAULI".equals(jawaban.getSoalType())) {
			Optional<SoalPauli> data = soalPauliRepository.findById(jawaban.getSoalUuid());
			if (data.isPresent()) {
				soalName = data.get().getNamaSoal();
			}
		}else if (jawaban.getSoalType().contains("TKD")) {
			Optional<SoalTKD> data = soalTKDRepository.findById(jawaban.getSoalUuid());
			if (data.isPresent()) {

				soalName = data.get().getNamaSoal();
			}
		} else if ("SOALHILANG".equals(jawaban.getSoalType())) {
			Optional<SoalHilang> data = soalHilangRepository.findById(jawaban.getSoalUuid());
			if (data.isPresent()) {
				soalName = data.get().getNamaSoal();
			}
		} else if ("GANJILGENAP".equals(jawaban.getSoalType())) {
			Optional<SoalGanjilGenap> data = soalGanjilGenapRepository.findById(jawaban.getSoalUuid());
			if (data.isPresent()) {
				soalName = data.get().getNamaSoal();
			}
		}
		jawaban.setUjianName(dataUjian.get().getNamaUjian());
		jawaban.setSoalName(soalName);
		return jawaban;
	}
}
