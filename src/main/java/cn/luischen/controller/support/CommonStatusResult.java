package cn.luischen.controller.support;

import java.io.Serializable;

import cn.luischen.controller.util.ErrorMessageConstant;
import cn.luischen.controller.util.ErrorMessagePropertiesUtil;

/**
 * 服务端通用的返回值结构
 * @author wanglei
 *
 */
public class CommonStatusResult implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2339448797587149381L;
	public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FAIL = 1;

    public static String STATUS_TYPE_UPLOAD = "upload";
    public static String STATUS_TYPE_UPDATE = "update";
    public static String STATUS_TYPE_DELETE = "delete";
    public static String STATUS_TYPE_ADD = "add";
    private String statusType;
    private String statusDesc;
    private Object data;

    private int status;
    private String messageCode;
    private String message;
    private String exception;
    private Object body;

    public CommonStatusResult() {

    }

    public CommonStatusResult(int status, String messageCode, String message, String exceptionInfo, Object body) {
        this.status = status;
        this.messageCode = messageCode;
        this.message = message;
        this.exception = exceptionInfo;
        this.body = body;
    }
    
    public static CommonStatusResult create(int status, String messageCode, String message, String exceptionInfo, Object body) {
        CommonStatusResult data = new CommonStatusResult(status, messageCode, message, exceptionInfo, body);
        return data;
    }

    public static CommonStatusResult createAndInit(int status, String messageCode, String exceptionInfo, Object body, boolean isError, Object[] messageParValues) {
        String code = messageCode;
        String messageValue = null;
        if(messageParValues!=null && messageParValues.length>0){
            messageValue = ErrorMessagePropertiesUtil.getValueWithPars(messageCode, messageParValues);
        }else{
            messageValue = ErrorMessagePropertiesUtil.getValue(messageCode);
        }
        if(messageValue==null){
            if(isError){
                //异常处理
                code = ErrorMessageConstant.CODE_UNKNOWN_ERROR;
                if(exceptionInfo==null){
                    exceptionInfo = messageCode;
                }
                messageValue = ErrorMessagePropertiesUtil.getValue(code);
                if(messageValue==null){
                    messageValue = "服务端发生未知错误";
                }
            }else{
                //FoodException
                if(status!=STATUS_SUCCESS){
                    code = ErrorMessageConstant.CODE_UNKNOWN_ERROR;
                }else{
                    code = "";
                }
                messageValue = messageCode;
            }
        }
        
        if(exceptionInfo==null){
            exceptionInfo = "";
        }
        
        CommonStatusResult result = create(status, code, messageValue, exceptionInfo, body);
        return result;
    }
    
    /**
     * 成功
     * @param messageCode
     * @param body
     * @return 
     */
    public static CommonStatusResult success(String messageCode, Object body) {
        return createAndInit(STATUS_SUCCESS, messageCode, null, body, false, null);
    }
    
    /**
     * 成功
     * @param messageCode
     * @param body
     * @param messageParValues
     * @return 
     */
    public static CommonStatusResult successWithPars(String messageCode, Object body, Object... messageParValues) {
        return createAndInit(STATUS_SUCCESS, messageCode, null, body, false, messageParValues);
    }

    /**
     * 失败
     * @param messageCode
     * @param body
     * @return 
     */
    public static CommonStatusResult fail(String messageCode, Object body) {
        return createAndInit(STATUS_FAIL, messageCode, null, body, false, null);
    }
    
    /**
     * 失败
     * @param messageCode
     * @param body
     * @param messageParValues
     * @return 
     */
    public static CommonStatusResult failWithPars(String messageCode, Object body, Object... messageParValues) {
        return createAndInit(STATUS_FAIL, messageCode, null, body, false, messageParValues);
    }

    /**
     * 错误/异常
     * @param messageCode
     * @param exceptionInfo
     * @param body
     * @return 
     */
    public static CommonStatusResult error(String messageCode, String exceptionInfo, Object body) {
        return createAndInit(STATUS_FAIL, messageCode, exceptionInfo, body, true, null);
    }
    
    /**
     * 错误/异常
     * @param messageCode
     * @param exceptionInfo
     * @param body
     * @param messageParValues
     * @return 
     */
    public static CommonStatusResult errorWithPars(String messageCode, String exceptionInfo, Object body, Object... messageParValues) {
        return createAndInit(STATUS_FAIL, messageCode, exceptionInfo, body, true, messageParValues);
    }
    
    public static CommonStatusResult uploadSuccess() {
        return CommonStatusResult.uploadSuccess("", null);
    }

    public static CommonStatusResult uploadSuccess(Object object) {
        return CommonStatusResult.uploadSuccess("", object);
    }

    public static CommonStatusResult uploadSuccess(String statusDesc, Object object) {
        CommonStatusResult status = new CommonStatusResult();
        status.setBody(object);
        status.setStatus(STATUS_SUCCESS);
        status.setStatusType(CommonStatusResult.STATUS_TYPE_UPLOAD);
        status.setStatusDesc(statusDesc);
        return status;
    }

    public static CommonStatusResult uploadFail() {
        return CommonStatusResult.uploadFail("");
    }

    public static CommonStatusResult uploadFail(String statusDesc) {
        return CommonStatusResult.uploadFail(statusDesc, null);
    }

    public static CommonStatusResult uploadFail(String statusDesc, Object object) {
        CommonStatusResult status = new CommonStatusResult();
        status.setStatus(STATUS_FAIL);
        status.setStatusType(CommonStatusResult.STATUS_TYPE_UPLOAD);
        status.setStatusDesc(statusDesc);
        status.setBody(object);
        return status;
    }

    public static CommonStatusResult updateSuccess() {
        return CommonStatusResult.updateSuccess("", null);
    }

    public static CommonStatusResult updateSuccess(Object object) {
        return CommonStatusResult.updateSuccess("", object);
    }

    public static CommonStatusResult updateSuccess(String statusDesc, Object object) {
        CommonStatusResult status = new CommonStatusResult();
        status.setBody(object);
        status.setStatus(STATUS_SUCCESS);
        status.setStatusType(CommonStatusResult.STATUS_TYPE_UPDATE);
        status.setStatusDesc(statusDesc);
        return status;
    }

    public static CommonStatusResult updateFail() {
        return CommonStatusResult.updateFail("");
    }

    public static CommonStatusResult updateFail(String statusDesc) {
        CommonStatusResult status = new CommonStatusResult();
        status.setStatus(STATUS_FAIL);
        status.setStatusType(CommonStatusResult.STATUS_TYPE_UPDATE);
        status.setStatusDesc(statusDesc);
        return status;
    }

    public static CommonStatusResult addSuccess() {
        return CommonStatusResult.addSuccess("", null);
    }

    public static CommonStatusResult addSuccess(Object object) {
        return CommonStatusResult.addSuccess("", object);
    }

    public static CommonStatusResult addSuccess(String statusDesc, Object object) {
        CommonStatusResult status = new CommonStatusResult();
        status.setBody(object);
        status.setStatus(STATUS_SUCCESS);
        status.setStatusType(CommonStatusResult.STATUS_TYPE_ADD);
        return status;
    }

    public static CommonStatusResult addFail() {
        return CommonStatusResult.addFail("");
    }

    public static CommonStatusResult addFail(String statusDesc) {
        CommonStatusResult status = new CommonStatusResult();
        status.setStatus(STATUS_FAIL);
        status.setStatusType(CommonStatusResult.STATUS_TYPE_ADD);
        status.setStatusDesc(statusDesc);
        return status;
    }

    public static CommonStatusResult deleteSuccess() {
        return CommonStatusResult.deleteSuccess("");
    }

    public static CommonStatusResult deleteSuccess(String statusDesc) {
        CommonStatusResult status = new CommonStatusResult();
        status.setStatus(STATUS_SUCCESS);
        status.setStatusType(CommonStatusResult.STATUS_TYPE_DELETE);
        return status;
    }

    public static CommonStatusResult deleteFail(String statusDesc) {
        CommonStatusResult status = new CommonStatusResult();
        status.setStatus(STATUS_FAIL);
        status.setStatusType(CommonStatusResult.STATUS_TYPE_DELETE);
        status.setStatusDesc(statusDesc);
        return status;
    }

    public static CommonStatusResult deleteFail() {
        return CommonStatusResult.deleteFail("");
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getStatusType() {
        return statusType;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

}
