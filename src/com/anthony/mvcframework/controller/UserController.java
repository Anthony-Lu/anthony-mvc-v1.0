package com.anthony.mvcframework.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.anthony.mvcframework.annotation.Autowired;
import com.anthony.mvcframework.annotation.Controller;
import com.anthony.mvcframework.annotation.RequestMapping;
import com.anthony.mvcframework.service.UserService;

@Controller
@RequestMapping("/userController")
public class UserController {

	@Autowired
	private UserService userService;

	@RequestMapping("/getUser")
	public void getUser(HttpServletRequest req, HttpServletResponse resp) {

		String username = "12";
		String password = "34";
		userService.save(username, password);

		System.out.println("Controller中的getUser方法被请求，执行成功------");
	}

}
