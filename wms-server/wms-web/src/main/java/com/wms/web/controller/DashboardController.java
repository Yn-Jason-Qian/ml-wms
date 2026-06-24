package com.wms.web.controller;

import com.wms.common.base.ApiResponse;
import com.wms.web.dto.DashboardStatsDTO;
import com.wms.web.dto.TrendItemDTO;
import com.wms.web.dto.TrendRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final JdbcTemplate jdbc;

    @GetMapping("/stats")
    public ApiResponse<DashboardStatsDTO> stats() {
        DashboardStatsDTO d = new DashboardStatsDTO();
        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1).toString();
        d.setTodayReceive(count("wms_inbound_receive_header", today));
        d.setYesterdayReceive(count("wms_inbound_receive_header", yesterday));
        d.setTodayPutaway(count("wms_inbound_putaway_header", today));
        d.setYesterdayPutaway(count("wms_inbound_putaway_header", yesterday));
        d.setTodayPick(count("wms_outbound_pick_header", today));
        d.setYesterdayPick(count("wms_outbound_pick_header", yesterday));
        d.setTodayShip(count("wms_outbound_ship_header", today));
        d.setYesterdayShip(count("wms_outbound_ship_header", yesterday));
        return ApiResponse.ok(d);
    }

    private long count(String table, String date) {
        String sql =
                "SELECT COUNT(*) FROM " + table + " WHERE DATE(created_at) = ? AND is_deleted = 0";
        Long c = jdbc.queryForObject(sql, Long.class, date);
        return c != null ? c : 0;
    }

    @PostMapping("/trend")
    public ApiResponse<List<TrendItemDTO>> trend(@RequestBody TrendRequest req) {
        List<TrendItemDTO> list = new ArrayList<>();
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(req.getDays() - 1);
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            TrendItemDTO item = new TrendItemDTO();
            item.setDate(d.toString());
            item.setInboundQty(
                    jdbc.queryForObject(
                            "SELECT COALESCE(SUM(rl.receive_qty),0) FROM wms_inbound_receive_line rl "
                                    + "JOIN wms_inbound_receive_header rh ON rl.receive_header_id=rh.id "
                                    + "WHERE DATE(rh.created_at)=? AND rl.is_deleted=0",
                            BigDecimal.class,
                            d.toString()));
            item.setOutboundQty(
                    jdbc.queryForObject(
                            "SELECT COALESCE(SUM(sl.ship_qty),0) FROM wms_outbound_ship_line sl "
                                    + "JOIN wms_outbound_ship_header sh ON sl.ship_header_id=sh.id "
                                    + "WHERE DATE(sh.created_at)=? AND sl.is_deleted=0",
                            BigDecimal.class,
                            d.toString()));
            list.add(item);
        }
        return ApiResponse.ok(list);
    }

    @GetMapping("/expiry-alert")
    public ApiResponse<List<Map<String, Object>>> expiryAlert() {
        String sql =
                "SELECT sku_code, sku_name, batch_no, location_id, expiry_date, qty_on_hand, "
                        + "DATEDIFF(expiry_date, NOW()) AS days_left FROM wms_inventory_stock "
                        + "WHERE expiry_date IS NOT NULL AND expiry_date <= DATE_ADD(NOW(), INTERVAL 30 DAY) "
                        + "AND qty_on_hand > 0 AND is_deleted = 0 ORDER BY expiry_date ASC LIMIT 5";
        return ApiResponse.ok(jdbc.queryForList(sql));
    }
}
