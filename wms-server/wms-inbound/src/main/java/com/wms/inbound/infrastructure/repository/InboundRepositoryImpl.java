package com.wms.inbound.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.inbound.domain.entity.*;
import com.wms.inbound.domain.repository.InboundRepository;
import com.wms.inbound.infrastructure.mapper.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InboundRepositoryImpl implements InboundRepository {
    private final AsnHeaderMapper asnHeaderMapper;
    private final AsnLineMapper asnLineMapper;
    private final ReceiveHeaderMapper rcvHeaderMapper;
    private final ReceiveLineMapper rcvLineMapper;
    private final QcHeaderMapper qcHeaderMapper;
    private final QcLineMapper qcLineMapper;
    private final PutawayHeaderMapper paHeaderMapper;
    private final PutawayLineMapper paLineMapper;

    // ASN
    @Override
    public Optional<AsnHeader> findAsnHeaderById(Long id) {
        return Optional.ofNullable(asnHeaderMapper.selectById(id));
    }

    @Override
    public List<AsnLine> findAsnLinesByHeader(Long hId) {
        return asnLineMapper.selectList(
                new LambdaQueryWrapper<AsnLine>()
                        .eq(AsnLine::getAsnHeaderId, hId)
                        .orderByAsc(AsnLine::getLineNo));
    }

    @Override
    public Optional<AsnLine> findAsnLineById(Long id) {
        return Optional.ofNullable(asnLineMapper.selectById(id));
    }

    @Override
    public void saveAsnHeader(AsnHeader h) {
        asnHeaderMapper.insert(h);
    }

    @Override
    public void updateAsnHeader(AsnHeader h) {
        asnHeaderMapper.updateById(h);
    }

    @Override
    public void saveAsnLine(AsnLine l) {
        asnLineMapper.insert(l);
    }

    @Override
    public void updateAsnLine(AsnLine l) {
        asnLineMapper.updateById(l);
    }

    @Override
    public boolean existsByAsnNo(Long tenantId, String no, Long exId) {
        return asnHeaderMapper.selectCount(
                        new LambdaQueryWrapper<AsnHeader>()
                                .eq(AsnHeader::getTenantId, tenantId)
                                .eq(AsnHeader::getAsnNo, no)
                                .ne(exId != null, AsnHeader::getId, exId))
                > 0;
    }

    // Receive
    @Override
    public Optional<ReceiveHeader> findReceiveHeaderById(Long id) {
        return Optional.ofNullable(rcvHeaderMapper.selectById(id));
    }

    @Override
    public void saveReceiveHeader(ReceiveHeader h) {
        rcvHeaderMapper.insert(h);
    }

    @Override
    public void updateReceiveHeader(ReceiveHeader h) {
        rcvHeaderMapper.updateById(h);
    }

    @Override
    public void saveReceiveLine(ReceiveLine l) {
        rcvLineMapper.insert(l);
    }

    @Override
    public List<ReceiveLine> findReceiveLinesByHeader(Long hId) {
        return rcvLineMapper.selectList(
                new LambdaQueryWrapper<ReceiveLine>().eq(ReceiveLine::getReceiveHeaderId, hId));
    }

    // QC
    @Override
    public Optional<QcHeader> findQcHeaderById(Long id) {
        return Optional.ofNullable(qcHeaderMapper.selectById(id));
    }

    @Override
    public void saveQcHeader(QcHeader h) {
        qcHeaderMapper.insert(h);
    }

    @Override
    public void saveQcLine(QcLine l) {
        qcLineMapper.insert(l);
    }

    @Override
    public List<QcLine> findQcLinesByHeader(Long hId) {
        return qcLineMapper.selectList(
                new LambdaQueryWrapper<QcLine>().eq(QcLine::getQcHeaderId, hId));
    }

    @Override
    public void updateQcHeader(QcHeader h) {
        qcHeaderMapper.updateById(h);
    }

    // Putaway
    @Override
    public Optional<PutawayHeader> findPutawayHeaderById(Long id) {
        return Optional.ofNullable(paHeaderMapper.selectById(id));
    }

    @Override
    public void savePutawayHeader(PutawayHeader h) {
        paHeaderMapper.insert(h);
    }

    @Override
    public void savePutawayLine(PutawayLine l) {
        paLineMapper.insert(l);
    }

    @Override
    public void updatePutawayLine(PutawayLine l) {
        paLineMapper.updateById(l);
    }

    @Override
    public List<PutawayLine> findPutawayLinesByHeader(Long hId) {
        return paLineMapper.selectList(
                new LambdaQueryWrapper<PutawayLine>().eq(PutawayLine::getPutawayHeaderId, hId));
    }

    @Override
    public void updatePutawayHeader(PutawayHeader h) {
        paHeaderMapper.updateById(h);
    }
}
