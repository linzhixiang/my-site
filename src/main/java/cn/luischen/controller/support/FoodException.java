package cn.luischen.controller.support;

import java.util.List;

import cn.luischen.controller.util.ErrorMessageConstant;
import cn.luischen.controller.util.ErrorMessagePropertiesUtil;
/**
 * 通用的异常封装类
 * @author wanglei
 */
public final class FoodException extends RuntimeException {

	private static final long serialVersionUID = 1656883894481974649L;
	
	private String errorCode;
	
	public String getErrorCode() {
		return errorCode;
	}

	public FoodException() {
        super();
    }

    public FoodException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public FoodException(final String message) {
        super(message);
    }

    public FoodException(final Throwable cause) {
        super(cause);
    }

    public FoodException(final String message, final String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public static FoodException returnException(String errorCode){
        String code = errorCode;
        String messageValue = ErrorMessagePropertiesUtil.getValue(errorCode);
        if(messageValue==null){
            code = ErrorMessageConstant.CODE_UNKNOWN_ERROR;
            messageValue = errorCode;
        }
        return new FoodException(messageValue, code);
    }
    
    public static FoodException returnExceptionWithPars(String errorCode, Object... parValues){
        String code = errorCode;
        String messageValue = ErrorMessagePropertiesUtil.getValueWithPars(errorCode, parValues);
        if(messageValue==null){
            code = ErrorMessageConstant.CODE_UNKNOWN_ERROR;
            messageValue = errorCode;
        }
        return new FoodException(messageValue, code);
    }

    //返回extractCount条错误信息组成的字符串.
	public static FoodException returnException(List<String> warningList, int extractCount) {
		if(extractCount<1) extractCount = 10;
		 String code = ErrorMessageConstant.CODE_UNKNOWN_ERROR;
		 StringBuffer sb = new StringBuffer();
		 int count = extractCount;
		for (String warnMsg : warningList) {
			if(count<=0) {
				break;
			}
			sb.append(warnMsg);
			sb.append("\\n");
			count -- ;
		}
		String  messageValue = sb.toString();
        return new FoodException(messageValue, code);
	}
}
