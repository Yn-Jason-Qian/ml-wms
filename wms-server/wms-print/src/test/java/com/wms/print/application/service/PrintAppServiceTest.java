package com.wms.print.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.wms.print.application.dto.PrintCmd;
import com.wms.print.application.dto.PrintResultDTO;
import com.wms.print.application.dto.PrintTemplateCreateCmd;
import com.wms.print.application.dto.PrintTemplateDTO;
import com.wms.print.domain.entity.PrintRecord;
import com.wms.print.domain.entity.PrintTemplate;
import com.wms.print.infrastructure.mapper.PrintRecordMapper;
import com.wms.print.infrastructure.mapper.PrintTemplateMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class PrintAppServiceTest {

    @Mock private PrintTemplateMapper templateMapper;
    @Mock private PrintRecordMapper recordMapper;

    @InjectMocks private PrintAppService service;

    @Test
    void testCreateTemplate() {
        when(templateMapper.insert(any(PrintTemplate.class)))
                .thenAnswer(
                        inv -> {
                            PrintTemplate t = inv.getArgument(0);
                            t.setId(1L);
                            return 1;
                        });

        PrintTemplateCreateCmd cmd = new PrintTemplateCreateCmd();
        cmd.setTemplateCode("SKU_LABEL_01");
        cmd.setTemplateName("SKU标签");
        cmd.setTemplateType("SKU_LABEL");
        cmd.setPaperWidth(BigDecimal.valueOf(100));
        cmd.setPaperHeight(BigDecimal.valueOf(80));

        PrintTemplateDTO dto = service.createTemplate(cmd);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("SKU_LABEL_01", dto.getTemplateCode());
        assertEquals("SKU_LABEL", dto.getTemplateType());

        ArgumentCaptor<PrintTemplate> captor = ArgumentCaptor.forClass(PrintTemplate.class);
        verify(templateMapper).insert(captor.capture());
        assertEquals(1, captor.getValue().getStatus());
    }

    @Test
    void testGenerateZplByType() {
        Map<String, String> data = Map.of("locationCode", "A-01");

        String zpl = service.generateZpl("LOCATION_LABEL", data);
        assertTrue(zpl.contains("A-01"));
        assertTrue(zpl.contains("^BCN"));

        zpl =
                service.generateZpl(
                        "SHIPPING_LABEL",
                        Map.of("trackingNo", "SF001", "carrierName", "SF", "packageCount", "1"));
        assertTrue(zpl.contains("SF001"));

        zpl = service.generateZpl("PALLET_LABEL", Map.of("palletNo", "P1", "details", ""));
        assertTrue(zpl.contains("P1"));
        assertTrue(zpl.contains("^BQN"));

        // unknown type falls back to receive label
        zpl =
                service.generateZpl(
                        "UNKNOWN",
                        Map.of("skuCode", "X", "qty", "1", "date", "", "locationCode", ""));
        assertTrue(zpl.contains("X"));
    }

    @Test
    void testExecutePrintWithTemplate() {
        PrintTemplate template = new PrintTemplate();
        template.setId(1L);
        template.setTemplateType("SKU_LABEL");
        template.setContentJson(null); // triggers ZPL generation
        when(templateMapper.selectById(1L)).thenReturn(template);
        when(recordMapper.insert(any(PrintRecord.class)))
                .thenAnswer(
                        inv -> {
                            PrintRecord r = inv.getArgument(0);
                            r.setId(200L); // simulate MyBatis-Plus ASSIGN_ID
                            return 1;
                        });

        PrintCmd cmd = new PrintCmd();
        cmd.setTemplateId(1L);
        cmd.setRefType("ASN");
        cmd.setRefId(100L);
        cmd.setPrinterName("Zebra-ZT410");
        cmd.setPrintCount(2);

        PrintResultDTO result = service.executePrint(cmd);

        assertNotNull(result.getRecordId());
        assertNotNull(result.getZpl());
        assertEquals("Zebra-ZT410", result.getPrinterName());

        ArgumentCaptor<PrintRecord> captor = ArgumentCaptor.forClass(PrintRecord.class);
        verify(recordMapper).insert(captor.capture());
        assertEquals("ASN", captor.getValue().getRefType());
        assertEquals(2, captor.getValue().getPrintCount());
    }

    @Test
    void testExecutePrintTemplateNotFound() {
        when(templateMapper.selectById(999L)).thenReturn(null);
        PrintCmd cmd = new PrintCmd();
        cmd.setTemplateId(999L);

        assertThrows(IllegalArgumentException.class, () -> service.executePrint(cmd));
    }
}
