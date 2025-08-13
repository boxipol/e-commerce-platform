package com.cardata.partpricemanager.service;

import com.cardata.partpricemanager.models.Brand;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;


public interface PartsService {

	StreamingResponseBody processBrand(Brand brand) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException, InterruptedException, ExecutionException;
}