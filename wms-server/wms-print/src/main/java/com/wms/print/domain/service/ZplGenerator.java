package com.wms.print.domain.service;

import java.util.Map;

/**
 * ZPL (Zebra Programming Language) 标签指令生成器
 * 根据标签类型和数据生成 ZPL 文本，可直接发送到 Zebra 打印机
 *
 * ZPL 基础指令说明:
 * ^XA ~ ^XZ  标签开始/结束
 * ^FO x,y    字段起始位置 (Field Origin)
 * ^A@N,h,w   字体(N=正常方向, h=高, w=宽)
 * ^FD text   字段数据 (Field Data)
 * ^FS        字段分隔符
 * ^BQ        二维码 (QR Code)
 * ^BY        条码尺寸
 * ^BC        128码
 * ^GB w,h,t  画矩形 (宽,高,线粗)
 */
public class ZplGenerator {

    // 常用标签尺寸 (dots @ 203dpi = 8 dots/mm)
    // 100mm × 80mm → 800 × 640 dots
    // 100mm × 60mm → 800 × 480 dots
    private static final int DPI = 8; // dots/mm

    /**
     * 收货标签: SKU码 + 名称 + 数量 + 批次 + 日期 + 库位
     */
    public static String generateReceiveLabel(Map<String, String> data) {
        StringBuilder zpl = new StringBuilder();
        zpl.append("^XA\n");
        zpl.append("^CF0,40\n"); // 默认字体

        // 标题
        zpl.append("^FO50,30^A@N,50,50^FD").append("RECEIVE").append("^FS\n");
        zpl.append("^FO50,90^GB700,2,2^FS\n"); // 分隔线

        // SKU
        zpl.append("^FO50,110^A@N,35,35^FDSKU:^FS\n");
        zpl.append("^FO180,110^A@N,40,40^FD").append(data.getOrDefault("skuCode", "")).append("^FS\n");
        zpl.append("^FO50,155^A@N,25,25^FD").append(data.getOrDefault("skuName", "")).append("^FS\n");

        // 数量
        zpl.append("^FO50,195^A@N,28,28^FDQTY:^FS\n");
        zpl.append("^FO150,190^A@N,50,50^FD").append(data.getOrDefault("qty", "")).append("^FS\n");

        // 批次
        String batchNo = data.get("batchNo");
        if (batchNo != null && !batchNo.isEmpty()) {
            zpl.append("^FO50,250^A@N,28,28^FDBATCH:^FS\n");
            zpl.append("^FO180,250^A@N,28,28^FD").append(batchNo).append("^FS\n");
        }

        // 日期
        zpl.append("^FO50,290^A@N,25,25^FD").append(data.getOrDefault("date", "")).append("^FS\n");

        // 库位
        zpl.append("^FO50,320^A@N,30,30^FDLOC:^FS\n");
        zpl.append("^FO160,320^A@N,30,30^FD").append(data.getOrDefault("locationCode", "")).append("^FS\n");

        // 条码 (SKU code as barcode128)
        zpl.append("^FO50,370^BY2,2,80^BCN,80,Y,N,N^FD").append(data.getOrDefault("skuCode", "")).append("^FS\n");

        zpl.append("^XZ");
        return zpl.toString();
    }

    /**
     * 库位标签: 库位码大号 + 条码
     */
    public static String generateLocationLabel(Map<String, String> data) {
        StringBuilder zpl = new StringBuilder();
        zpl.append("^XA\n");

        String code = data.getOrDefault("locationCode", "");

        // 库位码大号字体
        zpl.append("^FO50,40^A@N,60,60^FD").append(code).append("^FS\n");

        // 库位条码128
        zpl.append("^FO50,130^BY3,2,100^BCN,100,Y,N,N^FD").append(code).append("^FS\n");

        // 区域名称
        String areaName = data.get("areaName");
        if (areaName != null) {
            zpl.append("^FO50,250^A@N,30,30^FD").append(areaName).append("^FS\n");
        }

        zpl.append("^XZ");
        return zpl.toString();
    }

    /**
     * 发货标签: 运单号 + 收件信息 + 条码
     */
    public static String generateShipLabel(Map<String, String> data) {
        StringBuilder zpl = new StringBuilder();
        zpl.append("^XA\n");
        zpl.append("^CF0,35\n");

        zpl.append("^FO50,30^A@N,45,45^FD").append("SHIPMENT").append("^FS\n");
        zpl.append("^FO50,80^GB700,2,2^FS\n");

        // 运单号
        zpl.append("^FO50,100^A@N,30,30^FDTRACKING:^FS\n");
        zpl.append("^FO230,100^A@N,35,35^FD").append(data.getOrDefault("trackingNo", "")).append("^FS\n");

        // 承运商
        zpl.append("^FO50,145^A@N,28,28^FDCARRIER:^FS\n");
        zpl.append("^FO230,145^A@N,28,28^FD").append(data.getOrDefault("carrierName", "")).append("^FS\n");

        // 包裹数
        zpl.append("^FO50,185^A@N,28,28^FDPACKAGES:^FS\n");
        zpl.append("^FO230,185^A@N,35,35^FD").append(data.getOrDefault("packageCount", "")).append("^FS\n");

        // 运单条码128
        zpl.append("^FO50,240^BY2,2,100^BCN,100,Y,N,N^FD").append(data.getOrDefault("trackingNo", "")).append("^FS\n");

        zpl.append("^XZ");
        return zpl.toString();
    }

    /**
     * 托盘标签: 托盘号大号 + 二维码 + 明细列表
     */
    public static String generatePalletLabel(Map<String, String> data) {
        StringBuilder zpl = new StringBuilder();
        zpl.append("^XA\n");

        String palletNo = data.getOrDefault("palletNo", "");

        // 托盘号
        zpl.append("^FO50,30^A@N,55,55^FD").append(palletNo).append("^FS\n");

        // QR Code (含更多数据)
        zpl.append("^FO430,30^BQN,2,6^FDQA,").append(palletNo).append("^FS\n");

        // 明细
        zpl.append("^FO50,110^A@N,25,25^FD").append(data.getOrDefault("details", "")).append("^FS\n");

        zpl.append("^XZ");
        return zpl.toString();
    }
}
