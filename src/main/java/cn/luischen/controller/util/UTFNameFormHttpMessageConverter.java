package cn.luischen.controller.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.converter.FormHttpMessageConverter;

/**
 * 解决中文文件名乱码问题, 接收时文件名需要使用URLDecoder进行解析
 * 
 * @author wanglei
 */
public class UTFNameFormHttpMessageConverter extends FormHttpMessageConverter {
	@Override
	protected String getFilename(Object part) {
		String fileName = super.getFilename(part);
		if (fileName != null && !fileName.isEmpty()) {
			try {
				fileName = URLEncoder.encode(fileName, "UTF-8");
			} catch (UnsupportedEncodingException ex) {
				Logger.getLogger(UTFNameFormHttpMessageConverter.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return fileName;
	}
}
