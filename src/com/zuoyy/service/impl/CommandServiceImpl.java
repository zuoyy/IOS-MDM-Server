package com.zuoyy.service.impl;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.zuoyy.dao.CommandDao;
import com.zuoyy.pojo.Command;
import com.zuoyy.service.CommandService;

@Component
public class CommandServiceImpl implements CommandService {
	
	private CommandDao commandDao;
	
	@Override
	public void save(Command command) {
		commandDao.add(command);
	}
	
	@Override
	public Command getCommandById(String id) {
		return commandDao.getCommandById(id);
	}

	@Override
	public void deleteCommandById(String id) {
		commandDao.deleteCommandById(id);
	}

	@Override
	public List<Command> getAllCommand() {
		return commandDao.getAll();
	}

	public CommandDao getCommandDao() {
		return commandDao;
	}
	
	@Resource
	public void setCommandDao(CommandDao commandDao) {
		this.commandDao = commandDao;
	}

	@Override
	public Command getCommandByHql(String queryString, Object... params) {
		return commandDao.getByHql(queryString, params);
	}
	
	@Override
	public void saveOrUpdate(Command command) {
		if(null == command.getId() || "".equals(command.getId())){
			command.setId(UUID.randomUUID().toString());
			commandDao.add(command);
		}else{
			commandDao.saveOrUpdate(command);
		}
	}
	
}
