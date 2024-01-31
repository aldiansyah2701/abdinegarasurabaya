package com.abdinegara.surabaya.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
