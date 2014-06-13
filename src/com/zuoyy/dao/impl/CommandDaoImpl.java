package com.zuoyy.dao.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.zuoyy.dao.CommandDao;
import com.zuoyy.dao.common.BaseDao;
import com.zuoyy.pojo.Command;

@Component
public class CommandDaoImpl extends BaseDao implements CommandDao {

	@Override
	public void add(Command command) {
		super.add(command);
	}

	@Override
	public Command getCommandById(String id) {
		Object object = super.getById(Command.class, id);
		return (Command)object;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Command> getAll() {
		List<Command> list = (List<Command>) super.getAll(Command.class);
		return list;
	}

	@Override
	public void deleteCommandById(String id) {
		super.deleteById(Command.class, id);
	}
	
	public Command getByHql(String queryString,Object... params){
		return (Command)super.getByHql(queryString, params);
	}

	@Override
	public void saveOrUpdate(Command command) {
		super.saveOrUpdate(command);
	}
	
	
}
