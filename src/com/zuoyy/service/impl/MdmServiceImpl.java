package com.zuoyy.service.impl;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.zuoyy.dao.MdmDao;
import com.zuoyy.pojo.Mdm;
import com.zuoyy.service.MdmService;

@Component
public class MdmServiceImpl implements MdmService {
	
	private MdmDao mdmDao;
	
	@Override
	public void save(Mdm mdm) {
		mdmDao.add(mdm);
	}
	
	@Override
	public Mdm getMdmById(String id) {
		return mdmDao.getMdmById(id);
	}

	@Override
	public List<Mdm> getAllMdm() {
		return mdmDao.getAll();
	}

	public MdmDao getMdmDao() {
		return mdmDao;
	}
	
	@Resource
	public void setMdmDao(MdmDao mdmDao) {
		this.mdmDao = mdmDao;
	}

	@Override
	public Mdm getMdmByHql(String queryString,Object... params) {
		return mdmDao.getByHql(queryString, params);
	}

	@Override
	public void saveOrUpdtae(Mdm mdm) {
		if(null == mdm.getId() || "".equals(mdm.getId())){
			mdm.setId(UUID.randomUUID().toString());
			mdmDao.add(mdm);
		}else{
			mdmDao.saveOrUpdate(mdm);
		}
	}
	
	
	
}
