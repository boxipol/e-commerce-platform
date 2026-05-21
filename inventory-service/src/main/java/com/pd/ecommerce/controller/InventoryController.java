package com.pd.ecommerce.controller;

import com.pd.ecommerce.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public final class InventoryController {

	private final InventoryService service;


//	@PostMapping("/register")
//	public Mono<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
//		return service.register(request);
//	}
//
//	@PostMapping("/login")
//	public Mono<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
//		return service.login(request);
//	}
//
//	// todo update
//
//	@DeleteMapping("/delete")
//	public Mono<Void> delete(@RequestParam UUID id) {
//		return service.delete(id);
//	}
}