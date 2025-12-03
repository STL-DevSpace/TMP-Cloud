package org.dromara.gateway.config.properties;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 放行白名单配置
 *
 * @author
 */
@Slf4j
@Data
@Component
@RefreshScope  // 如果使用配置中心才需要
@ConfigurationProperties(prefix = "security.ignore")
public class IgnoreWhiteProperties {

    /**
     * 放行白名单配置，网关不校验此处的白名单
     */
    private List<String> whites = new ArrayList<>();

    @PostConstruct
    public void init() {
        log.info("╔═══════════════════════════════════════╗");
        log.info("║     白名单配置加载                      ║");
        log.info("╠═══════════════════════════════════════╣");
        log.info("║ 白名单数量: {}", String.format("%-25s", whites.size()) + "║");
        if (!whites.isEmpty()) {
            log.info("║ 白名单路径:                            ║");
            whites.forEach(white ->
                log.info("║   - {}", String.format("%-33s", white) + "║")
            );
        } else {
            log.warn("║ ⚠️  警告: 白名单为空!                   ║");
        }
        log.info("╚═══════════════════════════════════════╝");
    }
}
