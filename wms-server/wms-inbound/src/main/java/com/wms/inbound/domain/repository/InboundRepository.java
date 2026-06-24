package com.wms.inbound.domain.repository;

import com.wms.inbound.domain.entity.*;

import java.util.List;
import java.util.Optional;

public interface InboundRepository {
    // ASN
    Optional<AsnHeader> findAsnHeaderById(Long id);

    List<AsnLine> findAsnLinesByHeader(Long headerId);

    Optional<AsnLine> findAsnLineById(Long id);

    void saveAsnHeader(AsnHeader h);

    void updateAsnHeader(AsnHeader h);

    void saveAsnLine(AsnLine l);

    void updateAsnLine(AsnLine l);

    boolean existsByAsnNo(Long tenantId, String asnNo, Long excludeId);

    // Receive
    Optional<ReceiveHeader> findReceiveHeaderById(Long id);

    void saveReceiveHeader(ReceiveHeader h);

    void updateReceiveHeader(ReceiveHeader h);

    void saveReceiveLine(ReceiveLine l);

    List<ReceiveLine> findReceiveLinesByHeader(Long headerId);

    // QC
    Optional<QcHeader> findQcHeaderById(Long id);

    void saveQcHeader(QcHeader h);

    void saveQcLine(QcLine l);

    List<QcLine> findQcLinesByHeader(Long headerId);

    void updateQcHeader(QcHeader h);

    // Putaway
    Optional<PutawayHeader> findPutawayHeaderById(Long id);

    void savePutawayHeader(PutawayHeader h);

    void savePutawayLine(PutawayLine l);

    void updatePutawayLine(PutawayLine l);

    List<PutawayLine> findPutawayLinesByHeader(Long headerId);

    void updatePutawayHeader(PutawayHeader h);
}
