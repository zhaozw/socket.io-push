package com.yy.misaka.server.lbs.util;

import java.util.HashMap;
import java.util.Map;

import com.yy.misaka.server.support.ServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionCodeEnum {

    protected static Logger logger = LoggerFactory.getLogger(ExceptionCodeEnum.class);

    @SuppressWarnings("rawtypes")
    private static Map<String, Enum> enumMap = new HashMap<>();

    @SuppressWarnings("rawtypes")
    public static Enum findExceptionEnum(String retCode) {
        Enum ret = enumMap.get(retCode);
        if (ret == null) {
            return NotKnowExCode.NOT_KNOW;
        }
        return ret;
    }

    @SuppressWarnings("rawtypes")
    public static <T> T findOrThrow(String retCode, T retObj) {
        Enum e = findExceptionEnum(retCode);
        if (e == GeneralExCode.SUCCESS) {
            return retObj;
        }
        if (e instanceof GetSmsExCode) {
            GetSmsExCode e1 = (GetSmsExCode) e;
            throw new ServiceException(e1.code, e1.desc);
        } else if (e instanceof RegisterExCode) {
            RegisterExCode e1 = (RegisterExCode) e;
            throw new ServiceException(e1.code, e1.desc);
        } else if (e instanceof GeneralExCode) {
            GeneralExCode e1 = (GeneralExCode) e;
            throw new ServiceException(e1.code, e1.desc);
        }
        logger.error("uaas unknown code " + retCode);
        throw new ServiceException(NotKnowExCode.NOT_KNOW.code, NotKnowExCode.NOT_KNOW.desc);
    }

    public static void throwReqFail() {
        throw new ServiceException(ExceptionCodeEnum.GeneralExCode.REQ_FAIL.code, ExceptionCodeEnum.GeneralExCode.REQ_FAIL.desc);
    }

    public enum NotKnowExCode {
        NOT_KNOW("xxxxx", -9999, "未知错误!");
        public final String retCode;
        public final int code;
        public final String desc;

        private NotKnowExCode(String retCode, int code, String desc) {
            this.retCode = retCode;
            this.code = code;
            this.desc = desc;
        }
    }

    public enum GetSmsExCode {
        SMS_EXPIRE("20101", -21, "短信验证码或授权码已过期"), SMS_CHECK_FAIL("20102", -22, "短信验证码或授权码不正确");
        public final String retCode;
        public final int code;
        public final String desc;

        private GetSmsExCode(String retCode, int code, String desc) {
            this.retCode = retCode;
            this.code = code;
            this.desc = desc;
            enumMap.put(retCode, this);
        }
    }

    public enum RegisterExCode {
        EXISTED("20201", -31, "注册账号已存在"), REGISTER_FAIL("20203", -32, "账号注册失败"), NATION_ERROR("20204", -33, "国家代码不正确");
        public final String retCode;
        public final int code;
        public final String desc;

        private RegisterExCode(String retCode, int code, String desc) {
            this.retCode = retCode;
            this.code = code;
            this.desc = desc;
            enumMap.put(retCode, this);
        }
    }

    public enum GeneralExCode {
        SUCCESS("00000", 0, "success"), REQ_FAIL("99999", -1, "失败!后台服务器繁忙!"), PARAM_EMPTY("20001", -2, "参数为空"), MUST_PARAM_EMPTY("20002", -3, "必填项为空"),
        APPID_ERROR("20003", -4, "appId不正确"), PHONE_NUM_FORMAT_ERROR("20004", -5, "手机号码格式不正确"), EMAIL_FORMAT_ERROR("20005", -6, "邮箱格式不正确"),
        ACCOUNT_TYPE_ERROR("20006", -7, "账号类型不正确"), DEV_TYPE_ERROR("20007", -8, "设备类型不正确"), OPT_TYPE_ERROR("20008", -9, "操作类型不正确"),
        ACCOUNT_FORMAT_ERROR("20009", -10, "账号格式不正确"), AUTH_SOURCE_ERROR("20010", -11, "第三方授权来源不正确"), ACCOUNT_STATUS_ERROR("20011", -12, "账号状态不正确"),
        PASSPORT_INVALID("20011", -12, "一次性通行证无效"), ACCOUNT_NOT_EXIST("20202", -13, "账号不存在. 已经注册过的用户，进行各种操作会检查该用户是否存在"), REQUEST_TIMESTAMP_OUT("20401", -14, "请求已过期!");
        public final String retCode;
        public final int code;
        public final String desc;

        private GeneralExCode(String retCode, int code, String desc) {
            this.retCode = retCode;
            this.code = code;
            this.desc = desc;
            enumMap.put(retCode, this);
        }
    }
}
