package com.doubleclue.dcem.recruiting.restapi.model;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;

public class ApiRecruitingFile  {
	
	
	public ApiRecruitingFile(CloudSafeEntity cloudSafeEntity) {
		id = cloudSafeEntity.getId();
		fileName = cloudSafeEntity.getName();
		info = cloudSafeEntity.getInfo();
		size =cloudSafeEntity.getLength();
	}
	
		
	public ApiRecruitingFile() {
		super();
	}

	private int id;

	private String fileName;
	
	private String info;
	
	private long size;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
	
	
}
