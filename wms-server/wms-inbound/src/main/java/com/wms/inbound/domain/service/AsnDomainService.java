package com.wms.inbound.domain.service;

import com.wms.common.exception.BusinessException;
import com.wms.inbound.domain.entity.AsnHeader;
import com.wms.inbound.domain.entity.AsnLine;
import com.wms.inbound.domain.entity.ReceiveHeader;
import com.wms.inbound.domain.entity.ReceiveLine;
import com.wms.inbound.domain.repository.AsnRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsnDomainService {
    private final AsnRepository asnRepository;

    /** 收货确认：校验数量 + 更新ASN行 */
    public AsnLine receiveLine(ReceiveHeader header, ReceiveLine receiveLine, Long userId) {
        if (header.getAsnHeaderId() == null || receiveLine.getAsnLineId() == null) {
            return null;
        }

        AsnLine asnLine =
                asnRepository
                        .findLineById(receiveLine.getAsnLineId())
                        .orElseThrow(() -> BusinessException.notFound("ASN行不存在"));

        BigDecimal remaining = asnLine.getRemainingQty();
        if (receiveLine.getReceiveQty().compareTo(remaining) > 0) {
            throw new BusinessException(
                    "收货数量 [" + receiveLine.getReceiveQty() + "] 超过剩余可收数量 [" + remaining + "]");
        }

        asnLine.setReceivedQty(asnLine.getReceivedQty().add(receiveLine.getReceiveQty()));
        asnLine.setUpdatedBy(userId);
        asnRepository.updateLine(asnLine);

        AsnHeader asnHeader = asnRepository.findById(header.getAsnHeaderId()).orElse(null);
        if (asnHeader != null) {
            updateAsnStatus(asnHeader, userId);
        }

        return asnLine;
    }

    private void updateAsnStatus(AsnHeader asnHeader, Long userId) {
        List<AsnLine> lines = asnRepository.findLinesByHeader(asnHeader.getId());
        boolean allReceived =
                lines.stream().allMatch(l -> l.getReceivedQty().compareTo(l.getExpectedQty()) >= 0);
        boolean anyReceived =
                lines.stream().anyMatch(l -> l.getReceivedQty().compareTo(BigDecimal.ZERO) > 0);

        if (allReceived) asnHeader.setStatus(AsnHeader.STATUS_RECEIVED);
        else if (anyReceived) asnHeader.setStatus(AsnHeader.STATUS_PARTIAL_RECEIVED);
        else asnHeader.setStatus(AsnHeader.STATUS_CREATED);
        asnHeader.setUpdatedBy(userId);
        asnRepository.updateHeader(asnHeader);
    }
}
