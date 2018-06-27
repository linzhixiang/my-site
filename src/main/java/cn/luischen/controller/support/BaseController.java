package cn.luischen.controller.support;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Controller的父类，提供一些公用的方法
 * @author wanglei
 * 
 */
public class BaseController {

	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
	public static final MediaType APPLICATION_TEXT_UTF8 = new MediaType(MediaType.TEXT_HTML.getType(), MediaType.TEXT_HTML.getSubtype(), Charset.forName("utf8"));

	protected int getIntParam(Map paramMap, String key) {
		int paramValue;
		String value = paramMap.get(key)+"";
		try {					
			paramValue = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			paramValue = Integer.MIN_VALUE;
		}
		return paramValue;
	}

	protected int getIntParam(HttpServletRequest request, String key) {
		int paramValue;
		String value = (String) request.getParameter(key);
		try {
			paramValue = Integer.parseInt(value);
		} catch (Exception e) {
			paramValue = Integer.MIN_VALUE;
		}
		return paramValue;
	}

	protected String getStringParam(Map paramMap, String key) {
		String value;
		try {
			value = (String) paramMap.get(key);
		} catch (Exception e) {
			value = "";
		}
		return value;
	}
	
	protected Integer getIntegerParam(Map paramMap, String key ){
		Integer value;
		try{
			value = (Integer) paramMap.get(key);
		} catch (Exception e){
			value=null;
		}
		return value;
	}

	protected String getStringParam(HttpServletRequest request, String key) {
		String value;
		try {
			value = (String) request.getParameter(key);
		} catch (Exception e) {
			value = "";
		}
		return value;
	}


	/**
	 * 将返回值对象包装为使用JSON格式的ResponseEntity并返回
	 * 
	 * @param body
	 * @param status
	 * @return
	 */
	protected <T> ResponseEntity<T> returnJSONWithStatus(T body, HttpStatus status) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON_UTF8);
		return new ResponseEntity<T>(body, headers, status);
	}

	/**
	 * 将返回值对象包装为使用JSON格式的ResponseEntity并返回
	 * 
	 * @param body
	 * @param status
	 * @return
	 */
	protected <T> ResponseEntity<T> returnJSONWithStatusForIE(T body, HttpStatus status) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_TEXT_UTF8);
		return new ResponseEntity<T>(body, headers, status);
	}

	/**
	 * 返回MultipartFile文件Map使用文件名为key
	 * 
	 */
	protected Map<String, MultipartFile> getMultipartFileNameMap(MultipartFile[] qualiFiles) {
		// 提取上传文件
		Map<String, MultipartFile> fileMap = new HashMap<String, MultipartFile>();
		if (qualiFiles != null && qualiFiles.length > 0) {
			for (MultipartFile file : qualiFiles) {
				if (file != null && file.getSize() > 0) {
					String fileName = file.getOriginalFilename();
					fileMap.put(fileName, file);
				}
			}
		}
		return fileMap;
	}

	/**
	 * 返回MultipartFile文件Map使用文件名为key
	 * 
	 */
	protected Map<String, MultipartFile> getMultipartFileNameMap(List<MultipartFile> qualiFiles) {
		// 提取上传文件
		Map<String, MultipartFile> fileMap = new HashMap<String, MultipartFile>();
		if (qualiFiles != null && qualiFiles.size() > 0) {
			for (MultipartFile file : qualiFiles) {
				if (file != null && file.getSize() > 0) {
					String fileName = file.getOriginalFilename();
					fileMap.put(fileName, file);
				}
			}
		}
		return fileMap;
	}

	/**
	 * 从request中获取所有的上传文件
	 * 
	 * @param request
	 * @return
	 */
	protected List<MultipartFile> getMultipartFilesFromRequest(HttpServletRequest request) {
		List<MultipartFile> multipartFiles = new ArrayList<MultipartFile>();
		if (request != null && request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
			MultiValueMap<String, MultipartFile> fMap = multipartHttpServletRequest.getMultiFileMap();
			if (fMap != null && fMap.size() > 0) {
				for (Entry<String, List<MultipartFile>> entry : fMap.entrySet()) {
					List<MultipartFile> files = entry.getValue();
					if (files != null && files.size() > 0) {
						multipartFiles.addAll(files);
					}
				}
			}
		}
		return multipartFiles;
	}

	/**
	 * 从request中获取所有的上传文件并以Map形式返回
	 * 
	 * @param request
	 * @return
	 */
	protected Map<String, List<MultipartFile>> getMultipartFileMapFromRequest(HttpServletRequest request) {
		if (request != null && request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
			MultiValueMap<String, MultipartFile> fMap = multipartHttpServletRequest.getMultiFileMap();
			if (fMap != null) {
				return fMap;
			}
		}
		return null;
	}

	/**
	 * 把对象转换成json，忽略 null的属性。
	 * 
	 * @param obj
	 * @return Map
	 * @throws JsonProcessingException
	 */

	@SuppressWarnings("rawtypes")
	protected Map getCompactJSONMapFromObject(Object obj) throws Exception {
		ObjectMapper om = new ObjectMapper();
		om.setSerializationInclusion(Include.NON_NULL);
		om.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
		om.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		om.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
		om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		String json = om.writeValueAsString(obj);
		Map map = om.readValue(json, Map.class);
		return map;
	}

}
