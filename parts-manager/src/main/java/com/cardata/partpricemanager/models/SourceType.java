package com.cardata.partpricemanager.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public enum SourceType {

	TXT(".txt"),
	EXEL(".xlsx"),
	MDB(".mdb");


	private final String extension;
}
