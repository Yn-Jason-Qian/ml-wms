package com.wms.inbound.domain.repository;

import com.wms.inbound.domain.entity.ReceiveHeader;
import com.wms.inbound.domain.entity.ReceiveLine;

import java.util.List;
import java.util.Optional;

public interface ReceiveRepository {
    Optional<ReceiveHeader> findById(Long id);

    void saveHeader(ReceiveHeader h);

    void updateHeader(ReceiveHeader h);

    void saveLine(ReceiveLine l);

    List<ReceiveLine> findLinesByHeader(Long headerId);
}
