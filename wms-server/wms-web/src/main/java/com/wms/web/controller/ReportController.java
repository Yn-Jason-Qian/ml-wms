package com.wms.web.controller;

import com.wms.common.util.EasyExcelUtil;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportController {
    private final JdbcTemplate jdbc;

    @PostMapping("/inventory-summary")
    public void inventorySummary(
            @RequestBody(required = false) Map<String, Object> body, HttpServletResponse resp)
            throws IOException {
        writeExcel(
                resp,
                "收发存汇总",
                "SELECT s.sku_code, s.sku_name, s.batch_no, SUM(s.qty_on_hand) AS total_qty, SUM(s.qty_allocated) AS alloc_qty, SUM(s.qty_available) AS avail_qty FROM wms_inventory_stock s WHERE s.is_deleted=0 GROUP BY s.sku_code, s.sku_name, s.batch_no");
    }

    @PostMapping("/inventory-aging")
    public void inventoryAging(
            @RequestBody(required = false) Map<String, Object> body, HttpServletResponse resp)
            throws IOException {
        writeExcel(
                resp,
                "库龄分析",
                "SELECT sku_code, sku_name, batch_no, qty_on_hand, first_in_time, DATEDIFF(NOW(), first_in_time) AS age_days FROM wms_inventory_stock WHERE is_deleted=0 AND qty_on_hand>0 ORDER BY age_days DESC");
    }

    @PostMapping("/inventory-expiry")
    public void inventoryExpiry(
            @RequestBody(required = false) Map<String, Object> body, HttpServletResponse resp)
            throws IOException {
        writeExcel(
                resp,
                "效期预警",
                "SELECT sku_code, sku_name, batch_no, expiry_date, qty_on_hand, DATEDIFF(expiry_date, NOW()) AS days_left FROM wms_inventory_stock WHERE is_deleted=0 AND expiry_date IS NOT NULL ORDER BY days_left ASC");
    }

    @PostMapping("/receive-detail")
    public void receiveDetail(
            @RequestBody(required = false) Map<String, Object> body, HttpServletResponse resp)
            throws IOException {
        writeExcel(
                resp,
                "收货明细",
                "SELECT rh.receive_no, rh.created_at, rl.sku_code, rl.sku_name, rl.receive_qty, rl.batch_no FROM wms_inbound_receive_line rl JOIN wms_inbound_receive_header rh ON rl.receive_header_id=rh.id WHERE rl.is_deleted=0 ORDER BY rh.created_at DESC LIMIT 10000");
    }

    @PostMapping("/pick-detail")
    public void pickDetail(
            @RequestBody(required = false) Map<String, Object> body, HttpServletResponse resp)
            throws IOException {
        writeExcel(
                resp,
                "拣货明细",
                "SELECT ph.pick_no, ph.created_at, pl.sku_code, pl.sku_name, pl.pick_qty, pl.picked_qty, pl.to_container FROM wms_outbound_pick_line pl JOIN wms_outbound_pick_header ph ON pl.pick_header_id=ph.id WHERE pl.is_deleted=0 ORDER BY ph.created_at DESC LIMIT 10000");
    }

    @PostMapping("/ship-detail")
    public void shipDetail(
            @RequestBody(required = false) Map<String, Object> body, HttpServletResponse resp)
            throws IOException {
        writeExcel(
                resp,
                "发货明细",
                "SELECT sh.ship_no, sh.carrier_name, sh.tracking_no, sh.created_at, sl.sku_code, sl.sku_name, sl.ship_qty FROM wms_outbound_ship_line sl JOIN wms_outbound_ship_header sh ON sl.ship_header_id=sh.id WHERE sl.is_deleted=0 ORDER BY sh.created_at DESC LIMIT 10000");
    }

    @PostMapping("/asn-detail")
    public void asnDetail(
            @RequestBody(required = false) Map<String, Object> body, HttpServletResponse resp)
            throws IOException {
        writeExcel(
                resp,
                "ASN明细",
                "SELECT ah.asn_no, ah.asn_type, ah.created_at, al.sku_code, al.sku_name, al.expected_qty, al.received_qty FROM wms_inbound_asn_line al JOIN wms_inbound_asn_header ah ON al.asn_header_id=ah.id WHERE al.is_deleted=0 ORDER BY ah.created_at DESC LIMIT 10000");
    }

    @PostMapping("/order-detail")
    public void orderDetail(
            @RequestBody(required = false) Map<String, Object> body, HttpServletResponse resp)
            throws IOException {
        writeExcel(
                resp,
                "订单明细",
                "SELECT oh.order_no, oh.order_type, oh.customer_name, oh.created_at, ol.sku_code, ol.sku_name, ol.order_qty, ol.allocated_qty, ol.picked_qty, ol.shipped_qty FROM wms_outbound_order_line ol JOIN wms_outbound_order_header oh ON ol.order_header_id=oh.id WHERE ol.is_deleted=0 ORDER BY oh.created_at DESC LIMIT 10000");
    }

    @PostMapping("/stocktake-diff")
    public void stocktakeDiff(
            @RequestBody(required = false) Map<String, Object> body, HttpServletResponse resp)
            throws IOException {
        writeExcel(
                resp,
                "盘点差异",
                "SELECT sh.stocktake_no, sl.location_code, sl.sku_code, sl.sku_name, sl.batch_no, sl.book_qty, sl.first_count_qty, sl.second_count_qty, sl.diff_qty, sl.status FROM wms_inventory_stocktake_line sl JOIN wms_inventory_stocktake_header sh ON sl.stocktake_header_id=sh.id WHERE sl.is_deleted=0 AND sl.diff_qty IS NOT NULL ORDER BY sh.created_at DESC LIMIT 10000");
    }

    private void writeExcel(HttpServletResponse resp, String name, String sql) throws IOException {
        List<Map<String, Object>> rows = jdbc.queryForList(sql);
        List<List<Object>> data = new ArrayList<>();
        if (!rows.isEmpty()) {
            data.add(new ArrayList<>(rows.get(0).keySet()));
            for (Map<String, Object> row : rows) data.add(new ArrayList<>(row.values()));
        }
        EasyExcelUtil.export(resp, name, "Sheet1", null, null);
        // fallback: write directly
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader(
                "Content-Disposition",
                "attachment;filename="
                        + java.net.URLEncoder.encode(name, "UTF-8").replace("+", "%20")
                        + ".xlsx");
        com.alibaba.excel.EasyExcel.write(resp.getOutputStream()).sheet("Sheet1").doWrite(data);
    }
}
