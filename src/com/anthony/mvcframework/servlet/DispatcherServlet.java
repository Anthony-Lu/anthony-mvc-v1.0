package com.anthony.mvcframework.servlet;

import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.anthony.mvcframework.annotation.Autowired;
import com.anthony.mvcframework.annotation.Controller;
import com.anthony.mvcframework.annotation.RequestMapping;
import com.anthony.mvcframework.annotation.Service;
import com.anthony.mvcframework.controller.UserController;
import com.anthony.mvcframework.utils.StringUtil;
/**
 * 
 * @author anthony	
 * @date 2018年5月9日 下午3:26:43
 */
public class DispatcherServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Properties properties = new Properties();
	private List<String> classNameList = new ArrayList<>();
	private Map<String, Object> beanMap = new HashMap<>();
	private Map<String, Method> handlerMap = new HashMap<>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		initConfig(config.getInitParameter("contextConfigLocation"));// 加载配置文件
		initScanner(properties.getProperty("scanPackage"));// 扫描当前包下的所有类
		try {
			initIoc();// 初始化ioc容器
			initAutowired();// bean的自动装配
		} catch (Exception e) {
			e.printStackTrace();
		}
		initHandlerMapping();// 初始化HandlerMapping(保存url-method的映射关系)
	}

	/**
	 * 加载配置文件
	 * 
	 * @param path
	 */
	private void initConfig(String path) {

		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(path)) {
			properties.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取包下面的所有类
	 * 
	 * @param path
	 */
	private void initScanner(String path) {

		URL url = this.getClass().getClassLoader().getResource("/" + path.replaceAll("\\.", "/"));
		File directory = new File(url.getFile());
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				initScanner(path + "." + file.getName());
			} else {
				String className = path + "." + file.getName().replace(".class", "");
				classNameList.add(className);
			}
		}

	}

	/**
	 * 将扫描到的所有类进行实例化并放入ioc容器中
	 * 
	 * @throws Exception
	 */
	private void initIoc() throws Exception {

		if (classNameList.isEmpty()) {
			return;
		}
		for (String className : classNameList) {
			Class<?> clazz = Class.forName(className);

			if (clazz.isAnnotationPresent(Controller.class)) {
				
				beanMap.put(StringUtil.toLowerCaseFirstWorld(clazz.getSimpleName()), clazz.newInstance());

			} else if (clazz.isAnnotationPresent(Service.class)) {

				Service service = clazz.getAnnotation(Service.class);
				String name = service.value();
				if (!"".equals(name.trim())) {
					beanMap.put(name, clazz.newInstance());
					continue;
				}

				Class<?>[] interfaces = clazz.getInterfaces();
				for (Class<?> ifs : interfaces) {
					beanMap.put(ifs.getName(), clazz.newInstance());
				}
			} else {
				continue;
			}

		}
	}

	/**
	 * 自动装备bean
	 * 
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private void initAutowired() {

		if (beanMap.isEmpty()) {
			return;
		}

		Set<Entry<String, Object>> set = beanMap.entrySet();

		for (Entry<String, Object> entry : set) {
			Field[] fields = entry.getValue().getClass().getDeclaredFields();

			for (Field field : fields) {
				if (!field.isAnnotationPresent(Autowired.class)) {
					continue;
				}
				Autowired annotation = field.getAnnotation(Autowired.class);
				field.setAccessible(true);// 设置属性的访问权限

				String beanName = annotation.value().trim();// 获取注解上的名称
				if ("".equals(beanName)) {
					beanName = field.getType().getName();
				}

				try {

					field.set(entry.getValue(), beanMap.get(beanName));// 给属性赋值

				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

			}
		}
	}

	/**
	 * 保存url和method之间的映射关系
	 */
	private void initHandlerMapping() {

		if (beanMap.isEmpty()) {
			return;
		}

		for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
			Class<?> clazz = entry.getValue().getClass();
			if (!clazz.isAnnotationPresent(Controller.class)) {
				continue;
			}
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				if (method.isAnnotationPresent(RequestMapping.class)) {
					String value = method.getAnnotation(RequestMapping.class).value();
					handlerMap.put(value, method);
				} else {
					continue;
				}
			}

		}

	}

	/**
	 * 处理浏览器请求，调用controller方法
	 * @param req
	 * @param resp
	 * @throws Exception 
	 * @throws IOException 
	 */
	private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws Exception{

		String requestURI = req.getRequestURI();
		String contextPath = req.getContextPath();
		String path = requestURI.replace(contextPath, "");
		Method method = handlerMap.get("/"+path.split("/")[2]);
		UserController userController = (UserController) beanMap.get(path.split("/")[1]);
		try {
			method.invoke(userController, new Object[] { req, resp });  
		} catch (Exception e) {
			throw e;
		} 
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			doDispatcher(req, resp);// 处理浏览器请求
		} catch (Exception e) {
			resp.getWriter().write("404");
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

}
