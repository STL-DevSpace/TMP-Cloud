package org.dromara.auth.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BodyVO {
    private String tenantId;
    private String username;
    private String password;
    private Boolean rememberMe;
    private String clientId;
    private String grantType;
}
