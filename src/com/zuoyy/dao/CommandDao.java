package com.zuoyy.dao;

import java.util.List;

import com.zuoyy.pojo.Command;

public interface CommandDao {
	
	void add(Command command);
	
	Command getCommandById(String id);
	
	List<Command> getAll();
	
	void deleteCommandById(String id);
	
	Command getByHql(String queryString,Object... params);
	
	void saveOrUpdate(Command command);
}
