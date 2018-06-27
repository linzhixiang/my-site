package cn.luischen.controller.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import cn.luischen.controller.support.CommonStatusResult;
import cn.luischen.controller.support.FoodException;
/**
 * 文件上传工具类
 * @author wanglei
 */
public class FileUploadUtils {

    private static Logger logger = LoggerFactory.getLogger(FileUploadUtils.class);
    private static SpringRestUtil restUtil = new SpringRestUtil();

    //存放附件目录名
    public static String upload = "/attach";

    // 获取web app根目录，最终存放附件路径是项目根目录 +附件目录名
    public static String contextPath = "";
    public static String remoteFileServicePath = "http://47.96.90.27:8080/fdWebFile";
    public static final String configFileName = "config";

    static {
    	
        contextPath = "E:\\ideaWorkSpace\\my-site";
        
        String fdWebFileURL = null;
        try {
            fdWebFileURL = "http://47.96.90.27:8080/fdWebFile";
        } catch (Exception e) {
        }
        if(fdWebFileURL!=null && !fdWebFileURL.isEmpty()){
            remoteFileServicePath = fdWebFileURL;
        }else{
            remoteFileServicePath = "http://10.1.63.118:80/fdWebFile";
        }
    }

    public static boolean copyFile(File originalFile, String uploadFileName, String tofile, boolean deleteOriginalFile) {
        boolean returnValue = true;
        InputStream bis = null;
        OutputStream bos = null;
        int len = 0;
        try {
            bis = new BufferedInputStream(new FileInputStream(originalFile));
            bos = new BufferedOutputStream(new FileOutputStream(new File(tofile)));
            //int size = (int) upload.length();
            byte[] buffer = new byte[1024];
            while ((len = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
            bos.flush();
            if(deleteOriginalFile){
                //文件复制成功后删除临时文件
                originalFile.delete();
            }
        } catch (FileNotFoundException e) {
            returnValue = false;
            logger.info("创建文件时出错:" + uploadFileName + e.getMessage());
            e.printStackTrace();
            //throw new IllegalArgumentException("创建文件时出错！", e);
        } catch (IOException e) {
            returnValue = false;
            logger.info("写入文件失败:" + uploadFileName + e.getMessage());
            e.printStackTrace();
            //throw new IllegalArgumentException("写入文件失败:" + uploadFileName, e);
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                returnValue = false;
                e.printStackTrace();
            }
        }
        return returnValue;
    }

    public static boolean copyFile(MultipartFile upload, String uploadFileName, String tofile) {
        boolean returnValue = false;

        try {
            File dst = new File(tofile);
            upload.transferTo(dst);
            returnValue = true;
        } catch (FileNotFoundException e) {
            logger.error("创建文件时出错:" + uploadFileName + e.getMessage());
            logger.error("创建文件时出错:", e);
        } catch (IOException e) {
            logger.error("写入文件失败:" + uploadFileName + e.getMessage());
            logger.error("写入文件失败:", e);
        }
        return returnValue;
    }

    public static String getFileName(String fileName) {
        String attName = "";

        if (!StringUtils.isEmpty(fileName) && fileName.contains(".")) {
            int index = fileName.lastIndexOf(".");
            Random r = new Random();
            int n2 = r.nextInt(1000000);
            n2 = Math.abs(r.nextInt() % 1000000);
            attName = n2 + System.currentTimeMillis()
                    + fileName.substring(index, fileName.length());
        } else {//如果名称不包括后缀
            Random r = new Random();
            int n2 = r.nextInt(100000);
            n2 = Math.abs(r.nextInt() % 100000);
            attName = "" + n2 + System.currentTimeMillis();
/*        	throw FoodException.returnException("no.suffix");*/
        }

        return attName;
    }

    /**
     * 判断文件是否存在
     *
     * @param fileName
     * @return
     */
    public static boolean isFileExist(String fileName) {
        File file = new File(fileName);
        if (file.isFile() && file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除单个文件
     *
     * @param fileName 被删除文件的文件名
     * @return 单个文件删除成功返回true,否则返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.isFile() && file.exists()) {
            file.delete();
            logger.debug("删除单个文件" + fileName + "成功！");
            return true;
        } else {
            logger.debug("删除单个文件" + fileName + "失败！");
            return false;
        }
    }
    
    /**
     * 上传文件到远程服务器
     * 
     * @param uploadFile
     * @param uploadFileName
     * @param attObjectType
     * @param tofile
     * @return 
     */
    public static String uploadFileToRemote(MultipartFile uploadFile, String uploadFileName, String attObjectType, String tofile){
        if(remoteFileServicePath==null || remoteFileServicePath.isEmpty()){
        	logger.error("remoteFileServicePath==null");
            return null;
        }
        if(copyFile(uploadFile, uploadFileName, tofile)){
            File dst = new File(tofile);
            logger.error("File dst = new File(tofile)");
            return uploadLocalFileToRemote(dst, attObjectType);
        }
        return null;
    }
    
    /**
     * 上传本地文件到远程服务器
     * 
     * @param localFile 
     * @param attObjectType
     * @return 
     */
    public static String uploadLocalFileToRemote(File localFile, String attObjectType){
        if(remoteFileServicePath==null || remoteFileServicePath.isEmpty()){
            return null;
        }
        try {
            Map<String, String> pars = new HashMap<>();
            List<File> files = new ArrayList<>();
            Map<String, List<File>> fileMap = new HashMap<>();
            fileMap.put("files", files);

            files.add(localFile);
            pars.put("fileName", "files");
            pars.put("attObjectType", attObjectType);

            CommonStatusResult s = restUtil.convertStringToObject(restUtil.processMultiPartPost(
                    remoteFileServicePath + "/manage/upload", pars, null, fileMap, null, null).getBody(), CommonStatusResult.class);
            if(s!=null && s.getStatus() == CommonStatusResult.STATUS_SUCCESS){
                Object body = s.getBody();
                if(body instanceof Map){
                    Map<String, String> returnMap = (Map<String, String>) body;
                    String originalFilename = localFile.getName();
                    if(returnMap.containsKey(originalFilename)){
                        return returnMap.get(originalFilename);
                    }
                }
            }
        } catch (Exception ex) {
        	logger.error("uploadLocalFileToRemote:=" +ex.getMessage());
        }
        return null;
    }
    
    /**
     * 删除远程服务器上的文件
     * @param filePath
     * @return 
     */
    public static boolean deleteFileOnRemote(String filePath) {
        boolean result = false;
        if(filePath==null || filePath.isEmpty()){
            return false;
        }
        try {
            String localPath = contextPath + filePath;
            if(isFileExist(localPath)){
                deleteFile(localPath);
            }
            
            Map<String, String> pars = new HashMap<>();
            pars.put("filePath", filePath);
            CommonStatusResult s = restUtil.convertStringToObject(restUtil.processNameValuePost(
                    remoteFileServicePath + "/manage/delete", pars, null, null).getBody(), CommonStatusResult.class);
            if(s!=null && s.getStatus() == CommonStatusResult.STATUS_SUCCESS){
                return true;
            }
        } catch (Exception e) {
        }
        return result;
    }
    
    /**
	 * 引用文件(包括图片)服务器地址
	 */
    public static String getImageFilePath() {
    	String fdWebFileURL = null;
        if (remoteFileServicePath==null || remoteFileServicePath.equals("")){
        	logger.error("必须在fdService的config.properties中指定fdWebFileURL路径。");
        	return "";
        }
        else
        	return remoteFileServicePath;
    }
    
	/**
	 * 上传单一证照并返回上传后的路径
	 * @param attObjectType 上传文件夹名称
	 * @param file 文件
	 * @return
	 * @throws FoodException
	 */
	@Transactional(readOnly = false)
	public static String uploadAttFile(String attObjectType, MultipartFile file)throws FoodException {
		// step 1: 判断参数
		if (null == file || file.getSize() <= 0) {
			throw FoodException.returnException("000009");
		}
		if (StringUtils.isEmpty(attObjectType)) {
			throw FoodException.returnException("000006");
		}
		String toFileName = FileUploadUtils.getFileName(file.getOriginalFilename());
		String path = FileUploadUtils.contextPath + FileUploadUtils.upload + "/" + attObjectType;
		File file1 = new File(path);
		if (!file1.exists()){
			file1.mkdirs();
		}
		String toFile = path + "/" + toFileName;
        String remotePath = FileUploadUtils.uploadFileToRemote(file, toFileName, attObjectType, toFile);
		if (remotePath!=null && !remotePath.isEmpty()) {      
			return remotePath;
		} else {
			throw FoodException.returnException("000002");
		}
	}
	
	/**
	 * 根据base64上传单一证照并返回上传后的路径
	 * @param attObjectType 上传文件夹名称
	 * @param file base64文件字符串
	 * @return
	 * @throws FoodException
	 */
	@Transactional(readOnly = false)
	public static String uploadAttFileBase(String attObjectType, String file)throws FoodException {		
		if(StringUtils.isEmpty(file)){
			throw FoodException.returnException("000009");
		}
		byte[] bytes = Base64.decode(file);
		if(bytes.length>1024*1024*1){
			throw FoodException.returnException("000009");
		}
		if (bytes==null||bytes.length<1) {
			throw FoodException.returnException("000009");
		}
		ByteArrayInputStream in2=new ByteArrayInputStream(bytes);
	
		String toFileName = FileUploadUtils.getFileName("")+".png";
		String upload = FileUploadUtils.contextPath + FileUploadUtils.upload;
		String path = upload + "/" + attObjectType;
		File file1 = new File(path);
		if (!file1.exists()){
			file1.mkdirs();
		}
		String toFile = path + "/" + toFileName;
		BufferedOutputStream out;
		try {
			out = new BufferedOutputStream(
					new FileOutputStream(new File(toFile)));
		int i=0;
	
		while ((i = in2.read()) != -1) {
			out.write(i);
		}
		out.flush();
		out.close();
		in2.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return FileUploadUtils.uploadLocalFileToRemote(new File(toFile), attObjectType);
	}
	
}
