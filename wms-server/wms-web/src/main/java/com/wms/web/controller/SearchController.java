package com.wms.web.controller;

import com.wms.common.base.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SearchController {
    private final JdbcTemplate jdbc;

    @GetMapping("/search")
    public ApiResponse<List<Map<String,Object>>> search(@RequestParam String q,
                                                         @RequestParam(defaultValue="asn,order,sku,location,owner,wave") String types) {
        List<Map<String,Object>> results = new ArrayList<>();
        for (String t : types.split(",")) {
            String like = "%"+q+"%";
            RowCallbackHandler handler;
            switch (t.trim()) {
                case "asn":
                    handler = rs -> results.add(Map.of("type","ASN","id",rs.getLong("id"),"title",rs.getString("title"),"subtitle",rs.getString("subtitle"),"url","/inbound/asn"));
                    jdbc.query("SELECT 'ASN' AS type, id, asn_no AS title, status AS subtitle, '#/inbound/asn' AS url FROM wms_inbound_asn_header WHERE asn_no LIKE ? AND is_deleted=0 LIMIT 5",
                            handler, like);
                    break;
                case "order":
                    handler = rs -> results.add(Map.of("type","ORDER","id",rs.getLong("id"),"title",rs.getString("title"),"subtitle",rs.getString("subtitle"),"url","/outbound/order"));
                    jdbc.query("SELECT 'ORDER' AS type, id, order_no AS title, status AS subtitle, '#/outbound/order' AS url FROM wms_outbound_order_header WHERE order_no LIKE ? AND is_deleted=0 LIMIT 5",
                            handler, like);
                    break;
                case "sku":
                    handler = rs -> results.add(Map.of("type","SKU","id",rs.getLong("id"),"title",rs.getString("title"),"subtitle","","url","/masterdata/sku"));
                    jdbc.query("SELECT 'SKU' AS type, id, CONCAT(sku_code,' ',sku_name) AS title, '' AS subtitle, '#/masterdata/sku' AS url FROM wms_masterdata_sku WHERE (sku_code LIKE ? OR sku_name LIKE ?) AND is_deleted=0 LIMIT 5",
                            handler, like, like);
                    break;
                case "location":
                    handler = rs -> results.add(Map.of("type","LOCATION","id",rs.getLong("id"),"title",rs.getString("title"),"subtitle",rs.getString("subtitle"),"url","/masterdata/location"));
                    jdbc.query("SELECT 'LOCATION' AS type, id, location_code AS title, IF(status=1,'空闲','占用') AS subtitle, '#/masterdata/location' AS url FROM wms_masterdata_location WHERE location_code LIKE ? AND is_deleted=0 LIMIT 5",
                            handler, like);
                    break;
                case "owner":
                    handler = rs -> results.add(Map.of("type","OWNER","id",rs.getLong("id"),"title",rs.getString("title"),"subtitle","","url","/masterdata/owner"));
                    jdbc.query("SELECT 'OWNER' AS type, id, owner_name AS title, '' AS subtitle, '#/masterdata/owner' AS url FROM wms_masterdata_owner WHERE owner_name LIKE ? AND is_deleted=0 LIMIT 5",
                            handler, like);
                    break;
            }
        }
        return ApiResponse.ok(results);
    }
}
