package com.wms.print.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.base.PageResponse;
import com.wms.print.application.dto.PrintCmd;
import com.wms.print.application.dto.PrintResultDTO;
import com.wms.print.application.dto.PrintTemplateCreateCmd;
import com.wms.print.application.dto.PrintTemplateDTO;
import com.wms.print.domain.entity.PrintRecord;
import com.wms.print.domain.entity.PrintTemplate;
import com.wms.print.domain.service.ZplGenerator;
import com.wms.print.infrastructure.mapper.PrintRecordMapper;
import com.wms.print.infrastructure.mapper.PrintTemplateMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrintAppService {

    private final PrintTemplateMapper templateMapper;
    private final PrintRecordMapper recordMapper;

    // ── 模板管理 ──

    @Transactional
    public PrintTemplateDTO createTemplate(PrintTemplateCreateCmd cmd) {
        PrintTemplate t = new PrintTemplate();
        t.setTemplateCode(cmd.getTemplateCode());
        t.setTemplateName(cmd.getTemplateName());
        t.setTemplateType(cmd.getTemplateType());
        t.setPaperWidth(cmd.getPaperWidth());
        t.setPaperHeight(cmd.getPaperHeight());
        t.setContentJson(cmd.getContentJson());
        t.setStatus(1);
        templateMapper.insert(t);

        PrintTemplateDTO dto = new PrintTemplateDTO();
        dto.setId(t.getId());
        dto.setTemplateCode(t.getTemplateCode());
        dto.setTemplateName(t.getTemplateName());
        dto.setTemplateType(t.getTemplateType());
        dto.setPaperWidth(t.getPaperWidth());
        dto.setPaperHeight(t.getPaperHeight());
        dto.setContentJson(t.getContentJson());
        dto.setStatus(t.getStatus());
        dto.setCreatedAt(t.getCreatedAt());
        return dto;
    }

    public PageResponse<PrintTemplateDTO> pageTemplates(int pageNum, int pageSize) {
        IPage<PrintTemplate> result =
                templateMapper.selectPage(
                        new Page<>(pageNum, pageSize),
                        new LambdaQueryWrapper<PrintTemplate>()
                                .orderByDesc(PrintTemplate::getCreatedAt));

        return PageResponse.of(
                result.getRecords().stream()
                        .map(
                                t -> {
                                    PrintTemplateDTO dto = new PrintTemplateDTO();
                                    dto.setId(t.getId());
                                    dto.setTemplateCode(t.getTemplateCode());
                                    dto.setTemplateName(t.getTemplateName());
                                    dto.setTemplateType(t.getTemplateType());
                                    dto.setPaperWidth(t.getPaperWidth());
                                    dto.setPaperHeight(t.getPaperHeight());
                                    dto.setContentJson(t.getContentJson());
                                    dto.setStatus(t.getStatus());
                                    dto.setCreatedAt(t.getCreatedAt());
                                    return dto;
                                })
                        .toList(),
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize());
    }

    // ── 打印执行 ──

    /** 根据模板类型和数据生成 ZPL 标签文本 */
    public String generateZpl(String templateType, Map<String, String> data) {
        return switch (templateType) {
            case "SKU_LABEL", "INBOUND_LABEL", "RECEIVE_LABEL" ->
                    ZplGenerator.generateReceiveLabel(data);
            case "LOCATION_LABEL" -> ZplGenerator.generateLocationLabel(data);
            case "SHIPPING_LABEL" -> ZplGenerator.generateShipLabel(data);
            case "PALLET_LABEL" -> ZplGenerator.generatePalletLabel(data);
            default -> ZplGenerator.generateReceiveLabel(data); // fallback to receive label
        };
    }

    /** 执行打印 — 生成 ZPL 并记录打印历史 后续可通过蓝牙/网络发送 ZPL 到打印机 */
    @Transactional
    public PrintResultDTO executePrint(PrintCmd cmd) {
        PrintTemplate template = templateMapper.selectById(cmd.getTemplateId());
        if (template == null) {
            throw new IllegalArgumentException("打印模板不存在: " + cmd.getTemplateId());
        }

        // 生成 ZPL 文本
        String zpl = template.getContentJson();
        if (zpl == null || zpl.isBlank()) {
            // 无自定义模板时使用内置 ZPL 生成器
            zpl = generateZpl(template.getTemplateType(), new HashMap<>());
        }

        // 记录打印历史
        PrintRecord record = new PrintRecord();
        record.setTemplateId(cmd.getTemplateId());
        record.setPrinterName(cmd.getPrinterName());
        record.setPrintContent(zpl);
        record.setPrintCount(cmd.getPrintCount() != null ? cmd.getPrintCount() : 1);
        record.setRefType(cmd.getRefType());
        record.setRefId(cmd.getRefId());
        record.setPrintAt(LocalDateTime.now());
        recordMapper.insert(record);

        PrintResultDTO result = new PrintResultDTO();
        result.setRecordId(record.getId());
        result.setZpl(zpl);
        result.setPrinterName(cmd.getPrinterName());
        return result;
    }
}
