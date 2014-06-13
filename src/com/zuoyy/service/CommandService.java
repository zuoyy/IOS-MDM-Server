package com.zuoyy.service;

import java.util.List;

import com.zuoyy.pojo.Command;


public interface CommandService {
	
	void save(Command command);
	
	Command getCommandById(String id);
	
	List<Command> getAllCommand();
	
	void deleteCommandById(String id);
	
	Command getCommandByHql(String queryString,Object... params);
	
	void saveOrUpdate(Command command);
}
