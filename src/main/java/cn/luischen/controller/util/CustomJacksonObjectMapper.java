package cn.luischen.controller.util;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * 设置json到java对象互相转换时的规则
 * 
 * @author wanglei
 */
public class CustomJacksonObjectMapper extends ObjectMapper {
	public CustomJacksonObjectMapper() {
		super();
		this.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
		this.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		this.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		this.setSerializationInclusion(Include.ALWAYS);
		// this.getSerializationConfig().setDateFormat(new
		// SimpleDateFormat("yyyy-MM-dd"));
		// this.getSerializationConfig().getDateFormat().format(new
		// SimpleDateFormat("yyyy-MM-dd"));
		this.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
		// json中存在字段，但JAVA中没有的
		this.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		/*
		 * final AnnotationIntrospector introspector = new
		 * JaxbAnnotationIntrospector();
		 * 
		 * this.configure(org.codehaus.jackson.map.DeserializationConfig.Feature
		 * .UNWRAP_ROOT_VALUE, true);
		 * this.configure(org.codehaus.jackson.map.SerializationConfig.Feature.
		 * WRAP_ROOT_VALUE, true);
		 * 
		 * this.configure(org.codehaus.jackson.map.SerializationConfig.Feature.
		 * WRITE_NULL_PROPERTIES, false);
		 * 
		 * this.setDeserializationConfig(this.getDeserializationConfig().
		 * withAnnotationIntrospector(introspector));
		 * this.setSerializationConfig(this.getSerializationConfig().
		 * withAnnotationIntrospector(introspector));
		 */
	}
}
