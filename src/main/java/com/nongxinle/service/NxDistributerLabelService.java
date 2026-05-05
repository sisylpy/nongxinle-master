package com.nongxinle.service;

import com.nongxinle.entity.NxDistributerLabelEntity;

import java.util.List;

public interface NxDistributerLabelService {

    NxDistributerLabelEntity queryObject(Integer nxDistributerLabelId);

    void save(NxDistributerLabelEntity nxDistributerLabel);

    void update(NxDistributerLabelEntity nxDistributerLabel);

    void delete(Integer nxDistributerLabelId);

    List<NxDistributerLabelEntity> queryLabelsByDisId(Integer disId);
}
