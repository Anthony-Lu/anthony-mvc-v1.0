package com.anthony.mvcframework.service.impl;

import com.anthony.mvcframework.annotation.Service;
import com.anthony.mvcframework.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	public void save(String username, String password) {

		System.out.println("service中的方法执行-------");
	}

}
