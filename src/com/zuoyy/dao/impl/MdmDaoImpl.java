package com.zuoyy.dao.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.zuoyy.dao.MdmDao;
import com.zuoyy.dao.common.BaseDao;
import com.zuoyy.pojo.Mdm;

@Component
public class MdmDaoImpl extends BaseDao implements MdmDao {

	@Override
	public void add(Mdm mdm) {
		super.add(mdm);
	}

	@Override
	public Mdm getMdmById(String id) {
		Object object = super.getById(Mdm.class, id);
		return (Mdm)object;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Mdm> getAll() {
		List<Mdm> list = (List<Mdm>) super.getAll(Mdm.class);
		return list;
	}
	
	public Mdm getByHql(String queryString,Object... params){
		return (Mdm)super.getByHql(queryString, params);
	}

	@Override
	public void saveOrUpdate(Mdm mdm) {
		super.saveOrUpdate(mdm);
	}
	
	
	
}
