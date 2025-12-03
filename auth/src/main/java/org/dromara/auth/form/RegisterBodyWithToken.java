package org.dromara.auth.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

// 注册返回对象（继承RegisterBody并添加token字段）
@Data
@EqualsAndHashCode(callSuper = true)
public class RegisterBodyWithToken extends RegisterBody {
    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 令牌过期时间（秒）
     */
    private Long expiresIn;
}
