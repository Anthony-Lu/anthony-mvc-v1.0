package com.anthony.mvcframework.utils;

/**
 * 
 * @author luxuebing
 * @date 2018/03/06下午5:47:09
 */
public class StringUtil {

	/**
	 * 将首字母转为小写
	 * 
	 * @param value
	 * @return
	 */
	
	private StringUtil(){
		
	}
	
	public static String firstWord2LowerCase(String value) {
		if (Character.isLowerCase(value.charAt(0))) {
			return value;
		}
		return new StringBuilder()
				.append(Character.toLowerCase(value.charAt(0)))
				.append(value.substring(1))
				.toString();
	}

	/**
	 * 将首字母转为小写
	 * 
	 * @param str
	 * @return
	 */
	public static String toLowerCaseFirstWorld(String str) {
		char[] cs = str.toCharArray();
		cs[0] += 32;
		return String.valueOf(cs);
	}

	public static void main(String[] args) {
		String string = firstWord2LowerCase("Hello World");
		System.out.println(string);
		System.exit(0);
	}
}
