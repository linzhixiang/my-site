package cn.luischen.controller.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 使用spring rest发送http请求<br>
 * 需要导入spring-web和jackson-databind
 *
 */
@Component
public class SpringRestUtil {

    protected static final Logger log = Logger.getLogger(SpringRestUtil.class.getName());
    protected static final String defaultCharSet = "UTF-8";

    protected RestTemplate restTemplate;
    protected ObjectMapper objectMapper;

    public static MediaType textMediaType = new MediaType("text", "plain", Charset.forName(defaultCharSet));
    public static MediaType jsonMediaType = new MediaType("application", "json", Charset.forName(defaultCharSet));
    public static MediaType formMediaType = new MediaType("application", "x-www-formurlencoded");
    public static MediaType multipartFormDataMediaType = new MediaType("multipart", "form-data");
    public static MediaType multipartFormDataMediaTypeWithCharset = new MediaType("multipart", "form-data", Charset.forName(defaultCharSet));
    public static MediaType octetStreamDataMediaType = new MediaType("application", "octet-stream", Charset.forName(defaultCharSet));

    protected String lastResponseSessionId = null;
    protected static final int CONNECT_TIMEOUT = 20000; //连接超时时间
    protected static final int SOCKET_TIMEOUT = 120000; //数据传输超时时间

    public SpringRestUtil() {
        this.init(new SimpleClientHttpRequestFactory());
    }

    //HttpComponentsClientHttpRequestFactory(采用 HttpClient), SimpleClientHttpRequestFactory(采用JDK URLConnection)
    public SpringRestUtil(ClientHttpRequestFactory clientHttpRequestFactory) {
        this.init(clientHttpRequestFactory);
    }

    protected void init(ClientHttpRequestFactory clientHttpRequestFactory) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new CustomJacksonObjectMapper();
        //ByteArrayHttpMessageConverter, StringHttpMessageConverter, and ResourceHttpMessageConverter
        UTFNameFormHttpMessageConverter formHttpMessageConverter = new UTFNameFormHttpMessageConverter();
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        jackson2HttpMessageConverter.setObjectMapper(this.objectMapper);

        //注册converter
        this.restTemplate.getMessageConverters().clear();
        //text/plain
        this.restTemplate.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName(defaultCharSet)));
        //application/x-www-formurlencoded, read MultiValueMap<String, String>, write MultiValueMap<String, Object>
        this.restTemplate.getMessageConverters().add(formHttpMessageConverter);
        //application/json
        this.restTemplate.getMessageConverters().add(jackson2HttpMessageConverter);
        //supports all media types (*/*), and writes with a Content-Type of application/octet-stream
        this.restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        //application/octet-stream
        this.restTemplate.getMessageConverters().add(new ResourceHttpMessageConverter());
        
        if(clientHttpRequestFactory instanceof SimpleClientHttpRequestFactory){
            SimpleClientHttpRequestFactory factory = (SimpleClientHttpRequestFactory)clientHttpRequestFactory;
            factory.setConnectTimeout(CONNECT_TIMEOUT);
            factory.setReadTimeout(SOCKET_TIMEOUT);
        }else if(clientHttpRequestFactory instanceof HttpComponentsClientHttpRequestFactory){
            HttpComponentsClientHttpRequestFactory factory = (HttpComponentsClientHttpRequestFactory)clientHttpRequestFactory;
            factory.setConnectTimeout(CONNECT_TIMEOUT);
            factory.setReadTimeout(SOCKET_TIMEOUT);
        }
        this.restTemplate.setRequestFactory(clientHttpRequestFactory);
    }

    /**
     * 转换Object对象为JSON字符串
     *
     * @param value
     * @return
     */
    public String convertValueToString(Object value) {
        String returnValue = null;
        if (value != null) {
            if (value instanceof String || value instanceof Number
                    || value instanceof Boolean || value instanceof Character) {
                returnValue = value.toString();
            } else {
                try {
                    returnValue = this.objectMapper.writeValueAsString(value);
                } catch (JsonProcessingException ex) {
                    log.log(Level.INFO, null, ex);
                }
            }
        }
        return returnValue;
    }

    /**
     * 发送http请求并处理返回值
     *
     * @param strUrl
     * @param method
     * @param headers
     * @param body
     * @param uriVariables
     * @return
     * @throws java.lang.Exception
     */
    protected ResponseEntity<String> executeMethod(String strUrl, HttpMethod method, HttpHeaders headers, 
            Object body, Map<String, String> uriVariables) throws Exception {
        Map<String, String> uriVs = uriVariables;
        if (uriVs == null) {
            uriVs = new HashMap<String, String>();
        }

        if (headers == null) {
            headers = new HttpHeaders();
        }
        
        HttpEntity<?> httpEntity = new HttpEntity(body, headers);
        if (httpEntity.getHeaders() != null) {
            log.log(Level.INFO, "Send message headers: {0}", httpEntity.getHeaders().toString());
        }
        if (httpEntity.getBody() != null) {
            log.log(Level.INFO, "Send message body: {0}", httpEntity.getBody().toString());
        }

        ResponseEntity<String> responseEntity = this.restTemplate.exchange(strUrl, method, httpEntity, String.class, uriVs);
        HttpStatus status = responseEntity.getStatusCode();

        List<String> cookie = responseEntity.getHeaders().get("Set-Cookie");
        if (cookie != null && !cookie.isEmpty()) {
            lastResponseSessionId = cookie.get(0);
            log.log(Level.INFO, "Get sessionId: {0}", lastResponseSessionId);
        } else {
            lastResponseSessionId = null;
        }

        if (!status.equals(HttpStatus.OK)) {
            log.log(Level.WARNING, "Get a [{0}] response using body: {1}", new Object[]{status.value(), responseEntity.getBody()});
            throw new RuntimeException("http request fail with code " + status.value());
        }
        return responseEntity;
    }

    /**
     * 将json字符串转换成对象
     * @param <T>
     * @param s
     * @param clazz
     * @return
     * @throws IOException 
     */
    public <T> T convertStringToObject(String s, Class<T> clazz) throws IOException{
        T result = null;
        if(s!=null && !s.isEmpty() && clazz != null){
            result = this.objectMapper.readValue(s, clazz);
        }
        return result;
    }
    
    public HttpMethod getHttpMethodByName(String methodName){
        HttpMethod httpMethod = HttpMethod.POST;
        if(methodName!=null){
            if(methodName.equalsIgnoreCase("POST")){
                httpMethod = HttpMethod.POST;
            }else if(methodName.equalsIgnoreCase("GET")){
                httpMethod = HttpMethod.GET;
            }else if(methodName.equalsIgnoreCase("PUT")){
                httpMethod = HttpMethod.PUT;
            }else if(methodName.equalsIgnoreCase("DELETE")){
                httpMethod = HttpMethod.DELETE;
            }else if(methodName.equalsIgnoreCase("HEAD")){
                httpMethod = HttpMethod.HEAD;
            }else if(methodName.equalsIgnoreCase("OPTIONS")){
                httpMethod = HttpMethod.OPTIONS;
            }else if(methodName.equalsIgnoreCase("PATCH")){
                httpMethod = HttpMethod.PATCH;
            }else if(methodName.equalsIgnoreCase("TRACE")){
                httpMethod = HttpMethod.TRACE;
            }
        }
        return httpMethod;
    }
    
    /**
     * 发送一个multipart-form请求
     *
     * @param method
     * @param strUrl
     * @param stringEntities
     * @param jsonEntities
     * @param fileEntities
     * @param uriVariables
     * @param headers
     * @return
     * @throws java.lang.Exception
     */
    public ResponseEntity<String> processMultiPartRequest(String method, String strUrl, Map<String, String> stringEntities,
            Map<String, Object> jsonEntities, Map<String, List<File>> fileEntities, 
            Map<String, String> uriVariables, Map<String, String> headers) throws Exception {
        HttpMethod httpMethod = getHttpMethodByName(method);
        log.log(Level.INFO, "Send message URL: {0}", (strUrl!=null)?strUrl:"");
        if(uriVariables!=null){
            for(Entry<String, String> entry : uriVariables.entrySet()){
                log.log(Level.INFO, "URL variable: {0}:{1}", new Object[]{entry.getKey(), entry.getValue()});
            }
        }
        
        log.log(Level.INFO, "==================message content=================");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();

        if (stringEntities != null) {
            HttpHeaders h = new HttpHeaders();
            h.setContentType(textMediaType);
            for (Entry<String, String> entry : stringEntities.entrySet()) {
                HttpEntity<Object> entity = new HttpEntity<Object>(entry.getValue(), h);
                map.add(entry.getKey(), entity);
                log.log(Level.INFO, "{0}:{1}", new Object[]{entry.getKey(), entry.getValue()});
            }
        }

        if (jsonEntities != null) {
            HttpHeaders h = new HttpHeaders();
            h.setContentType(jsonMediaType);
            for (Entry<String, Object> entry : jsonEntities.entrySet()) {
                String value = convertValueToString(entry.getValue());
                HttpEntity<Object> entity = new HttpEntity<Object>(value, h);
                map.add(entry.getKey(), entity);
                log.log(Level.INFO, "{0}:{1}", new Object[]{entry.getKey(), value});
            }
        }

        if (fileEntities != null) {
            HttpHeaders h = new HttpHeaders();
            h.setContentType(octetStreamDataMediaType);
            for (Entry<String, List<File>> entry : fileEntities.entrySet()) {
                String name = entry.getKey();
                List<File> files = entry.getValue();
                if(files!=null && !files.isEmpty()){
                    for(File file : files){
                        Resource resource = new FileSystemResource(file);
                        HttpEntity<Object> entity = new HttpEntity<Object>(resource, h);
                        map.add(name, entity);
                        log.log(Level.INFO, "{0}:{1}", new Object[]{name, file.getAbsolutePath()});
                    }
                }
            }
        }
        log.log(Level.INFO, "==================================================");
        
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(multipartFormDataMediaType);
        if(headers!=null && !headers.isEmpty()){
            Set<Entry<String, String>> entrys = headers.entrySet();
            for(Entry<String, String> entry : entrys){
                httpHeaders.add(entry.getKey(), entry.getValue());
            }
        }
        return executeMethod(strUrl, httpMethod, httpHeaders, map, uriVariables);
    }
    
    /**
     * 发送一个multipart-form post请求
     *
     * @param strUrl
     * @param stringEntities
     * @param jsonEntities
     * @param fileEntities
     * @param uriVariables
     * @param headers
     * @return
     * @throws java.lang.Exception
     */
    public ResponseEntity<String> processMultiPartPost(String strUrl, Map<String, String> stringEntities,
            Map<String, Object> jsonEntities, Map<String, List<File>> fileEntities, 
            Map<String, String> uriVariables, Map<String, String> headers) throws Exception {
        return processMultiPartRequest("POST", strUrl, stringEntities, jsonEntities, fileEntities, uriVariables, headers);
    }
    
    /**
     * 发送一个整个form body为一个字符串的请求
     *
     * @param method
     * @param strUrl
     * @param content
     * @param contentType
     * @param uriVariables
     * @param headers
     * @return
     * @throws java.lang.Exception
     */
    public ResponseEntity<String> processStringBodyRequest(String method, String strUrl, String content, MediaType contentType, 
            Map<String, String> uriVariables, Map<String, String> headers) throws Exception {
        HttpMethod httpMethod = getHttpMethodByName(method);
        log.log(Level.INFO, "Send message URL: {0}", (strUrl!=null)?strUrl:"");
        if(uriVariables!=null){
            for(Entry<String, String> entry : uriVariables.entrySet()){
                log.log(Level.INFO, "URL variable: {0}:{1}", new Object[]{entry.getKey(), entry.getValue()});
            }
        }
        
        log.log(Level.INFO, "==================message content=================");
        log.log(Level.INFO, (content!=null)?content:"");
        log.log(Level.INFO, "==================================================");
        
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(contentType);
        if(headers!=null && !headers.isEmpty()){
            Set<Entry<String, String>> entrys = headers.entrySet();
            for(Entry<String, String> entry : entrys){
                requestHeaders.add(entry.getKey(), entry.getValue());
            }
        }
        return executeMethod(strUrl, httpMethod, requestHeaders, content, uriVariables);
    }
    
    /**
     * 发送一个整个form body为一个字符串的请求
     *
     * @param strUrl
     * @param content
     * @param contentType
     * @param uriVariables
     * @param headers
     * @return
     * @throws java.lang.Exception
     */
    public ResponseEntity<String> processStringBodyPost(String strUrl, String content, MediaType contentType, 
            Map<String, String> uriVariables, Map<String, String> headers) throws Exception {
        return processStringBodyRequest("POST", strUrl, content, contentType, uriVariables, headers);
    }
    
    /**
     * 发送一个普通的key-value为内容的请求
     *
     * @param method
     * @param strUrl
     * @param stringEntities
     * @param uriVariables
     * @param headers
     * @return
     * @throws java.lang.Exception
     */
    public ResponseEntity<String> processNameValueRequest(String method, String strUrl, Map<String, String> stringEntities, 
            Map<String, String> uriVariables, Map<String, String> headers) throws Exception {
        HttpMethod httpMethod = getHttpMethodByName(method);
        log.log(Level.INFO, "Send message URL: {0}", (strUrl!=null)?strUrl:"");
        if(uriVariables!=null){
            for(Entry<String, String> entry : uriVariables.entrySet()){
                log.log(Level.INFO, "URL variable: {0}:{1}", new Object[]{entry.getKey(), entry.getValue()});
            }
        }
        
        log.log(Level.INFO, "==================message content=================");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        if (stringEntities != null) {
            HttpHeaders h = new HttpHeaders();
            h.setContentType(textMediaType);
            for (Entry<String, String> entry : stringEntities.entrySet()) {
                HttpEntity<Object> httpEntity = new HttpEntity<Object>(entry.getValue(), h);
                map.add(entry.getKey(), httpEntity);
                log.log(Level.INFO, "{0}:{1}", new Object[]{entry.getKey(), entry.getValue()});
            }
        }
        log.log(Level.INFO, "==================================================");
        
        HttpHeaders httpHeaders = new HttpHeaders();
        //headers.setContentType(formMediaType);
        if(headers!=null && !headers.isEmpty()){
            Set<Entry<String, String>> entrys = headers.entrySet();
            for(Entry<String, String> entry : entrys){
                httpHeaders.add(entry.getKey(), entry.getValue());
            }
        }
        return executeMethod(strUrl, httpMethod, httpHeaders, map, uriVariables);
    }
    
    /**
     * 发送一个普通的key-value为内容的post请求
     *
     * @param strUrl
     * @param stringEntities
     * @param uriVariables
     * @param headers
     * @return
     * @throws java.lang.Exception
     */
    public ResponseEntity<String> processNameValuePost(String strUrl, Map<String, String> stringEntities, 
            Map<String, String> uriVariables, Map<String, String> headers) throws Exception {
        return processNameValueRequest("POST", strUrl, stringEntities, uriVariables, headers);
    }
    
    /**
     * 发送get请求<br>
     * 注: GET请求参数如果是中文则在接收端必须使用ISO8859-1编码来接收并重新转换为UTF-8
     *
     * @param strUrl
     * @param queryStringEntities 暂不支持，无效参数
     * @param uriVariables
     * @param headers
     * @return
     * @throws java.lang.Exception
     */
    public ResponseEntity<String> processGet(String strUrl, Map<String, String> queryStringEntities, 
            Map<String, String> uriVariables, Map<String, String> headers) throws Exception {
        log.log(Level.INFO, "Send message URL: {0}", (strUrl!=null)?strUrl:"");
        if(uriVariables!=null){
            for(Entry<String, String> entry : uriVariables.entrySet()){
                log.log(Level.INFO, "URL variable: {0}:{1}", new Object[]{entry.getKey(), entry.getValue()});
            }
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        if(headers!=null && !headers.isEmpty()){
            Set<Entry<String, String>> entrys = headers.entrySet();
            for(Entry<String, String> entry : entrys){
                httpHeaders.add(entry.getKey(), entry.getValue());
            }
        }
        return executeMethod(strUrl, HttpMethod.GET, httpHeaders, null, uriVariables);
    }
}
