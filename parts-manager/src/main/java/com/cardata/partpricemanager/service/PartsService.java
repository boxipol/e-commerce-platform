package com.cardata.partpricemanager.service;

import com.cardata.partpricemanager.models.Brand;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.sharing.SharedFolderMetadata;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

public interface PartsService {

	FileMetadata processBrand(Brand brand) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException, InterruptedException, ExecutionException;
	SharedFolderMetadata shareFolder() throws DbxException;
}