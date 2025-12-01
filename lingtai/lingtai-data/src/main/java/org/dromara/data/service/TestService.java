package org.dromara.data.service;

import org.dromara.resource.api.domain.RemoteFile;
import org.springframework.stereotype.Service;

@Service
public interface TestService {
    void save_data(RemoteFile remoteFile);
}
