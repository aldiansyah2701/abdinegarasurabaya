package com.abdinegara.surabaya.controller.siswa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abdinegara.surabaya.message.RequestQuote;
import com.abdinegara.surabaya.service.SiswaService;

@RestController
@RequestMapping("/api/v1/siswa")
public class SiswaController {
	
	@Autowired
	SiswaService siswaService;
	
	@PostMapping(path = "/quote/{type}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> getQuote(@PathVariable("type") String type, @RequestBody RequestQuote request) {
		return siswaService.createQuote(type, request);
	}
	
	@GetMapping(value = "/list/{type}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> getSiswa(@PathVariable("type") String type, Pageable pageable) {
		return siswaService.getSiswa(type, pageable);
	}

	@GetMapping(value = "/pemebelian-soal/{uuid}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<Object> getPembelianSoal(@PathVariable("uuid") String uuid, Pageable pageable) {
		return siswaService.getPembelianSoal(uuid, pageable);
	}
	

}
