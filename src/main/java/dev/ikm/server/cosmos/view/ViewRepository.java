package dev.ikm.server.cosmos.view;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentMap;

@Repository
public class ViewRepository {
//
//	private final ViewDatabaseConfig viewDatabaseConfig;
//	private final ConcurrentMap<String, ViewDTO> viewDB;
//
//	public ViewRepository(ViewDatabaseConfig viewDatabaseConfig) {
//		this.viewDatabaseConfig = viewDatabaseConfig;
//		DB db = DBMaker.fileDB(viewDatabaseConfig.getDirectory()).make();
//	}


		/*
	DB db = DBMaker.fileDB("file.db").make();
ConcurrentMap map = db.hashMap("map").createOrOpen();
map.put("something", "here");
db.close();
	 */
}
