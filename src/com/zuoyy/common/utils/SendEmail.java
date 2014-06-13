package com.zuoyy.common.utils;

import java.util.Map;

public class SendEmail {

	/**
	 * 向指定手机号发送短信
	 * @param phone
	 * @param content
	 */
	public void send(Map<String, String> taskParam){
		/**获取手机号和发送的内容**/
		String smtp = taskParam.get("smtp");
		String fromAddress = taskParam.get("fromAddress");
		String fromPass = taskParam.get("fromPass");
		String toAddress = taskParam.get("toAddress");
		String subject = taskParam.get("subject");
		String content = taskParam.get("content");
		
	}
}
