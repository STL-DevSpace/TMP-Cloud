package org.dromara.data.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.data.entity.SysImage;
import org.dromara.data.mapper.ImageMapper;
import org.dromara.data.service.TestService;
import org.dromara.resource.api.domain.RemoteFile;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TestServiceImpl implements TestService {

    private final ImageMapper baseMapper;

    @Override
    public void save_data(RemoteFile remoteFile) {
        SysImage sysImage = new SysImage()
            .setImageName(remoteFile.getName())
            .setImageType(remoteFile.getFileSuffix())
            .setImageSuffix(remoteFile.getFileSuffix())
            .setAccessUrl(remoteFile.getUrl());
        baseMapper.insert(sysImage);

    }
}
