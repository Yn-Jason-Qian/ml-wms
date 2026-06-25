package com.wms.inbound.domain.repository;

import com.wms.inbound.domain.entity.QcHeader;
import com.wms.inbound.domain.entity.QcLine;

import java.util.List;
import java.util.Optional;

public interface QcRepository {
    Optional<QcHeader> findById(Long id);

    void saveHeader(QcHeader h);

    void saveLine(QcLine l);

    List<QcLine> findLinesByHeader(Long headerId);

    void updateHeader(QcHeader h);
}
