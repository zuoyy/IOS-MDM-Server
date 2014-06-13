package com.zuoyy.service;

import java.util.List;

import com.zuoyy.pojo.Mdm;


public interface MdmService {
	
	void save(Mdm mdm);
	
	Mdm getMdmById(String id);
	
	List<Mdm> getAllMdm();
	
	Mdm getMdmByHql(String queryString,Object... params);
	
	void saveOrUpdtae(Mdm mdm);
}
