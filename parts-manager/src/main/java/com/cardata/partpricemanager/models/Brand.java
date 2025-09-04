package com.cardata.partpricemanager.models;

import com.cardata.partpricemanager.brands.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.cardata.partpricemanager.models.SourceType.EXEL;
import static com.cardata.partpricemanager.models.SourceType.TXT;

@RequiredArgsConstructor
@Getter
public enum Brand {

	BMW(BMW.class, 130, TXT, false, 366_000),
	FORD(FORD.class, 285, TXT, false, 60_000),
	JAGUAR(JAGUAR.class, 285, TXT, false, 11_000),
	OPEL(OPEL.class, 650, TXT, true, 750_000),
	PORSCHE(PORSCHE.class, 710, EXEL, false, 370_000),
	RENAULT(RENAULT.class, 720, TXT, false, 566_000),
	ROVER(ROVER.class, 730, TXT, false, 25_000),
	VOLVO(VOLVO.class, 910, TXT, false, 32_000),
	SKODA(SKODA.class, 800, EXEL, false, 112_000),
	TOYOTA(TOYOTA.class, 860, TXT, false, 4_000_000);


	private final Class<? extends VehicleBrandTask> taskClass;
	private final int brandCode;
	private final SourceType sourceType;
	private final boolean removeWat;
	private final int averageRecords;
}
