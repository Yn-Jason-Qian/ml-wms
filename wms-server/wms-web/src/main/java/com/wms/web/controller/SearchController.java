package com.wms.web.controller;

import com.wms.common.base.ApiResponse;
import com.wms.web.dto.SearchItemDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SearchController {
    private final JdbcTemplate jdbc;

    @GetMapping("/search")
    public ApiResponse<List<SearchItemDTO>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "asn,order,sku,location,owner,wave") String types) {
        List<SearchItemDTO> results = new ArrayList<>();
        for (String t : types.split(",")) {
            String like = "%" + q + "%";
            switch (t.trim()) {
                case "asn":
                    jdbc.query(
                            "SELECT 'ASN' AS type, id, asn_no AS title, status AS subtitle FROM wms_inbound_asn_header WHERE asn_no LIKE ? AND is_deleted=0 LIMIT 5",
                            (org.springframework.jdbc.core.RowCallbackHandler)
                                    rs ->
                                            results.add(
                                                    new SearchItemDTO(
                                                            "ASN",
                                                            rs.getLong("id"),
                                                            rs.getString("title"),
                                                            rs.getString("subtitle"),
                                                            "/inbound/asn")),
                            like);
                    break;
                case "order":
                    jdbc.query(
                            "SELECT 'ORDER' AS type, id, order_no AS title, status AS subtitle FROM wms_outbound_order_header WHERE order_no LIKE ? AND is_deleted=0 LIMIT 5",
                            (org.springframework.jdbc.core.RowCallbackHandler)
                                    rs ->
                                            results.add(
                                                    new SearchItemDTO(
                                                            "ORDER",
                                                            rs.getLong("id"),
                                                            rs.getString("title"),
                                                            rs.getString("subtitle"),
                                                            "/outbound/order")),
                            like);
                    break;
                case "sku":
                    jdbc.query(
                            "SELECT 'SKU' AS type, id, CONCAT(sku_code,' ',sku_name) AS title FROM wms_masterdata_sku WHERE (sku_code LIKE ? OR sku_name LIKE ?) AND is_deleted=0 LIMIT 5",
                            (org.springframework.jdbc.core.RowCallbackHandler)
                                    rs ->
                                            results.add(
                                                    new SearchItemDTO(
                                                            "SKU",
                                                            rs.getLong("id"),
                                                            rs.getString("title"),
                                                            "",
                                                            "/masterdata/sku")),
                            like,
                            like);
                    break;
                case "location":
                    jdbc.query(
                            "SELECT 'LOCATION' AS type, id, location_code AS title, IF(status=1,'空闲','占用') AS subtitle FROM wms_masterdata_location WHERE location_code LIKE ? AND is_deleted=0 LIMIT 5",
                            (org.springframework.jdbc.core.RowCallbackHandler)
                                    rs ->
                                            results.add(
                                                    new SearchItemDTO(
                                                            "LOCATION",
                                                            rs.getLong("id"),
                                                            rs.getString("title"),
                                                            rs.getString("subtitle"),
                                                            "/masterdata/location")),
                            like);
                    break;
                case "owner":
                    jdbc.query(
                            "SELECT 'OWNER' AS type, id, owner_name AS title FROM wms_masterdata_owner WHERE owner_name LIKE ? AND is_deleted=0 LIMIT 5",
                            (org.springframework.jdbc.core.RowCallbackHandler)
                                    rs ->
                                            results.add(
                                                    new SearchItemDTO(
                                                            "OWNER",
                                                            rs.getLong("id"),
                                                            rs.getString("title"),
                                                            "",
                                                            "/masterdata/owner")),
                            like);
                    break;
            }
        }
        return ApiResponse.ok(results);
    }
}
