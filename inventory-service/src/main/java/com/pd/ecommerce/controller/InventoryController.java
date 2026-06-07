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


	// todo add crud for admin
}