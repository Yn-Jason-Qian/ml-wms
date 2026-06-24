package com.wms.print.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class ZplGeneratorTest {

    @Test
    void testReceiveLabelContainsRequiredFields() {
        Map<String, String> data = new HashMap<>();
        data.put("skuCode", "ABC123456");
        data.put("skuName", "测试商品");
        data.put("qty", "50");
        data.put("batchNo", "B20260601");
        data.put("date", "2026-06-23");
        data.put("locationCode", "A-01-01-01");

        String zpl = ZplGenerator.generateReceiveLabel(data);

        assertTrue(zpl.contains("^XA"), "should start with ^XA");
        assertTrue(zpl.contains("^XZ"), "should end with ^XZ");
        assertTrue(zpl.contains("ABC123456"), "should contain SKU code");
        assertTrue(zpl.contains("50"), "should contain quantity");
        assertTrue(zpl.contains("B20260601"), "should contain batch number");
        assertTrue(zpl.contains("A-01-01-01"), "should contain location");
        assertTrue(zpl.contains("RECEIVE"), "should contain label type header");
    }

    @Test
    void testReceiveLabelWithoutOptionalFields() {
        Map<String, String> data = new HashMap<>();
        data.put("skuCode", "XYZ");
        data.put("qty", "1");
        data.put("date", "2026-01-01");
        data.put("locationCode", "L1");

        String zpl = ZplGenerator.generateReceiveLabel(data);

        assertTrue(zpl.startsWith("^XA"));
        assertTrue(zpl.contains("XYZ"));
        assertFalse(zpl.contains("BATCH:"), "batch section should not appear without batchNo");
    }

    @Test
    void testLocationLabel() {
        Map<String, String> data = new HashMap<>();
        data.put("locationCode", "A-01-02-03");
        data.put("areaName", "收货区");

        String zpl = ZplGenerator.generateLocationLabel(data);

        assertTrue(zpl.contains("A-01-02-03"));
        assertTrue(zpl.contains("收货区"));
        assertTrue(zpl.contains("^BCN"), "should contain barcode128 command");
    }

    @Test
    void testShipLabel() {
        Map<String, String> data = new HashMap<>();
        data.put("trackingNo", "SF1234567890");
        data.put("carrierName", "顺丰速运");
        data.put("packageCount", "5");

        String zpl = ZplGenerator.generateShipLabel(data);

        assertTrue(zpl.contains("SF1234567890"));
        assertTrue(zpl.contains("顺丰速运"));
        assertTrue(zpl.contains("SHIPMENT"));
        assertTrue(zpl.contains("5"));
    }

    @Test
    void testPalletLabel() {
        Map<String, String> data = new HashMap<>();
        data.put("palletNo", "PLT-001");
        data.put("details", "SKU:5种 / 数量:120件");

        String zpl = ZplGenerator.generatePalletLabel(data);

        assertTrue(zpl.contains("PLT-001"));
        assertTrue(zpl.contains("^BQN"), "should contain QR code command");
        assertTrue(zpl.contains("120件"));
    }

    @Test
    void testAllLabelsStartAndEndCorrectly() {
        Map<String, String> base = new HashMap<>();
        base.put("skuCode", "T");
        base.put("qty", "1");
        base.put("date", "2026-01-01");
        base.put("locationCode", "L");
        Map<String, String> ship = new HashMap<>();
        ship.put("trackingNo", "T1");
        ship.put("carrierName", "C");
        ship.put("packageCount", "1");
        Map<String, String> loc = new HashMap<>();
        loc.put("locationCode", "L1");
        Map<String, String> pallet = new HashMap<>();
        pallet.put("palletNo", "P1");
        pallet.put("details", "D");

        for (String zpl :
                new String[] {
                    ZplGenerator.generateReceiveLabel(base),
                    ZplGenerator.generateShipLabel(ship),
                    ZplGenerator.generateLocationLabel(loc),
                    ZplGenerator.generatePalletLabel(pallet)
                }) {
            assertTrue(zpl.startsWith("^XA"), "Every ZPL must start with ^XA");
            assertTrue(zpl.endsWith("^XZ\n") || zpl.endsWith("^XZ"), "Every ZPL must end with ^XZ");
        }
    }
}
