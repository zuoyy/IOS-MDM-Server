package com.zuoyy.dao;

import java.util.List;

import com.zuoyy.pojo.Mdm;


public interface MdmDao {
	
	void add(Mdm mdm);
	
	Mdm getMdmById(String id);
	
	List<Mdm> getAll();

	Mdm getByHql(String queryString,Object... params);
	
	void saveOrUpdate(Mdm mdm);
}
