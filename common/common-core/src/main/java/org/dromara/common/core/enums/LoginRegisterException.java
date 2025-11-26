package org.dromara.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录注册异常枚举
 * 状态码以90开头，表示登录注册专属状态码
 *
 * 9001-9019: 登录相关异常
 * 9020-9039: 注册相关异常
 * 9040-9049: 验证码相关异常
 * 9050-9059: 密码重置相关异常
 * 9060-9069: 第三方登录相关异常
 * 9070-9079: 权限相关异常
 * 9080-9099: 其他异常
 * @author Lion Li
 */
@Getter
@AllArgsConstructor
public enum LoginRegisterException {

    // ========== 登录相关异常 9001-9019 ==========
    /**
     * 用户名或密码错误
     */
    USERNAME_PASSWORD_ERROR(9001, "用户名或密码错误"),

    /**
     * 账号不存在
     */
    ACCOUNT_NOT_EXIST(9002, "账号不存在"),

    /**
     * 账号已被禁用
     */
    ACCOUNT_DISABLED(9003, "账号已被禁用"),

    /**
     * 账号已被锁定
     */
    ACCOUNT_LOCKED(9004, "账号已被锁定"),

    /**
     * 账号已过期
     */
    ACCOUNT_EXPIRED(9005, "账号已过期"),

    /**
     * 密码已过期
     */
    PASSWORD_EXPIRED(9006, "密码已过期，请修改密码"),

    /**
     * 登录失败次数过多
     */
    LOGIN_RETRY_LIMIT_EXCEED(9007, "登录失败次数过多，账号已被锁定"),

    /**
     * 验证码错误
     */
    CAPTCHA_ERROR(9008, "验证码错误"),

    /**
     * 验证码已过期
     */
    CAPTCHA_EXPIRED(9009, "验证码已过期，请重新获取"),

    /**
     * 短信验证码错误
     */
    SMS_CODE_ERROR(9010, "短信验证码错误"),

    /**
     * 短信验证码已过期
     */
    SMS_CODE_EXPIRED(9011, "短信验证码已过期，请重新获取"),

    /**
     * 邮箱验证码错误
     */
    EMAIL_CODE_ERROR(9012, "邮箱验证码错误"),

    /**
     * 邮箱验证码已过期
     */
    EMAIL_CODE_EXPIRED(9013, "邮箱验证码已过期，请重新获取"),

    /**
     * 令牌无效
     */
    TOKEN_INVALID(9014, "登录令牌无效"),

    /**
     * 令牌已过期
     */
    TOKEN_EXPIRED(9015, "登录令牌已过期，请重新登录"),

    /**
     * 会话已过期
     */
    SESSION_EXPIRED(9016, "会话已过期，请重新登录"),

    /**
     * 未登录
     */
    NOT_LOGIN(9017, "未登录，请先登录"),

    /**
     * 账号已在其他设备登录
     */
    ACCOUNT_LOGIN_ELSEWHERE(9018, "账号已在其他设备登录"),

    /**
     * 登录信息已失效
     */
    LOGIN_INFO_INVALID(9019, "登录信息已失效，请重新登录"),

    // ========== 注册相关异常 9020-9039 ==========
    /**
     * 用户名已存在
     */
    USERNAME_EXIST(9020, "用户名已存在"),

    /**
     * 手机号已被注册
     */
    PHONE_EXIST(9021, "手机号已被注册"),

    /**
     * 邮箱已被注册
     */
    EMAIL_EXIST(9022, "邮箱已被注册"),

    /**
     * 用户名格式不正确
     */
    USERNAME_FORMAT_ERROR(9023, "用户名格式不正确"),

    /**
     * 密码格式不正确
     */
    PASSWORD_FORMAT_ERROR(9024, "密码格式不正确，密码长度应为6-20位"),

    /**
     * 手机号格式不正确
     */
    PHONE_FORMAT_ERROR(9025, "手机号格式不正确"),

    /**
     * 邮箱格式不正确
     */
    EMAIL_FORMAT_ERROR(9026, "邮箱格式不正确"),

    /**
     * 两次密码不一致
     */
    PASSWORD_NOT_MATCH(9027, "两次输入的密码不一致"),

    /**
     * 注册失败
     */
    REGISTER_FAILED(9028, "注册失败，请稍后重试"),

    /**
     * 邀请码无效
     */
    INVITE_CODE_INVALID(9029, "邀请码无效"),

    /**
     * 邀请码已过期
     */
    INVITE_CODE_EXPIRED(9030, "邀请码已过期"),

    /**
     * 注册验证码错误
     */
    REGISTER_CODE_ERROR(9031, "注册验证码错误"),

    /**
     * 注册验证码已过期
     */
    REGISTER_CODE_EXPIRED(9032, "注册验证码已过期，请重新获取"),

    /**
     * 用户名长度不符合要求
     */
    USERNAME_LENGTH_ERROR(9033, "用户名长度应为3-20位"),

    /**
     * 密码强度不够
     */
    PASSWORD_STRENGTH_LOW(9034, "密码强度不够，需包含数字、字母和特殊字符"),

    /**
     * 注册IP受限
     */
    REGISTER_IP_LIMIT(9035, "该IP注册次数过多，请稍后重试"),

    // ========== 验证码相关异常 9040-9049 ==========
    /**
     * 验证码发送失败
     */
    CODE_SEND_FAILED(9040, "验证码发送失败，请稍后重试"),

    /**
     * 验证码发送过于频繁
     */
    CODE_SEND_FREQUENT(9041, "验证码发送过于频繁，请稍后再试"),

    /**
     * 今日验证码发送次数已达上限
     */
    CODE_SEND_LIMIT_TODAY(9042, "今日验证码发送次数已达上限"),

    /**
     * 验证码不能为空
     */
    CODE_NOT_EMPTY(9043, "验证码不能为空"),

    /**
     * 图形验证码错误
     */
    IMAGE_CAPTCHA_ERROR(9044, "图形验证码错误"),

    /**
     * 滑块验证失败
     */
    SLIDE_CAPTCHA_ERROR(9045, "滑块验证失败，请重试"),

    // ========== 密码重置相关异常 9050-9059 ==========
    /**
     * 原密码错误
     */
    OLD_PASSWORD_ERROR(9050, "原密码错误"),

    /**
     * 新密码不能与原密码相同
     */
    NEW_PASSWORD_SAME_AS_OLD(9051, "新密码不能与原密码相同"),

    /**
     * 密码重置失败
     */
    PASSWORD_RESET_FAILED(9052, "密码重置失败，请稍后重试"),

    /**
     * 密码重置链接已失效
     */
    PASSWORD_RESET_LINK_INVALID(9053, "密码重置链接已失效"),

    /**
     * 密码重置链接已过期
     */
    PASSWORD_RESET_LINK_EXPIRED(9054, "密码重置链接已过期，请重新申请"),

    /**
     * 密码修改过于频繁
     */
    PASSWORD_CHANGE_FREQUENT(9055, "密码修改过于频繁，请稍后再试"),

    // ========== 第三方登录相关异常 9060-9069 ==========
    /**
     * 第三方登录失败
     */
    THIRD_LOGIN_FAILED(9060, "第三方登录失败"),

    /**
     * 第三方账号未绑定
     */
    THIRD_ACCOUNT_NOT_BIND(9061, "第三方账号未绑定，请先绑定"),

    /**
     * 第三方账号已被其他用户绑定
     */
    THIRD_ACCOUNT_ALREADY_BIND(9062, "该第三方账号已被其他用户绑定"),

    /**
     * 获取第三方用户信息失败
     */
    THIRD_USER_INFO_FAILED(9063, "获取第三方用户信息失败"),

    /**
     * 微信授权失败
     */
    WECHAT_AUTH_FAILED(9064, "微信授权失败"),

    /**
     * QQ授权失败
     */
    QQ_AUTH_FAILED(9065, "QQ授权失败"),

    /**
     * 微博授权失败
     */
    WEIBO_AUTH_FAILED(9066, "微博授权失败"),

    // ========== 权限相关异常 9070-9079 ==========
    /**
     * 无访问权限
     */
    NO_PERMISSION(9070, "无访问权限"),

    /**
     * 访问被拒绝
     */
    ACCESS_DENIED(9071, "访问被拒绝"),

    /**
     * 需要管理员权限
     */
    NEED_ADMIN_PERMISSION(9072, "需要管理员权限"),

    /**
     * IP地址被限制
     */
    IP_RESTRICTED(9073, "您的IP地址被限制访问"),

    /**
     * 账号类型不支持该操作
     */
    ACCOUNT_TYPE_NOT_SUPPORT(9074, "当前账号类型不支持该操作"),

    // ========== 其他异常 9080-9099 ==========
    /**
     * 请求参数错误
     */
    PARAM_ERROR(9080, "请求参数错误"),

    /**
     * 系统繁忙
     */
    SYSTEM_BUSY(9081, "系统繁忙，请稍后重试"),

    /**
     * 操作失败
     */
    OPERATION_FAILED(9082, "操作失败"),

    /**
     * 请求过于频繁
     */
    REQUEST_FREQUENT(9083, "请求过于频繁，请稍后再试"),

    /**
     * 安全验证失败
     */
    SECURITY_VERIFY_FAILED(9084, "安全验证失败"),

    /**
     * 设备异常
     */
    DEVICE_ABNORMAL(9085, "检测到设备异常，请联系管理员"),

    /**
     * 未知错误
     */
    UNKNOWN_ERROR(9099, "未知错误");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 异常信息
     */
    private final String message;

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 枚举
     */
    public static LoginRegisterException getByCode(Integer code) {
        for (LoginRegisterException exception : values()) {
            if (exception.getCode().equals(code)) {
                return exception;
            }
        }
        return UNKNOWN_ERROR;
    }
}
