package com.wms.common.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public final class EasyExcelUtil {

    private EasyExcelUtil() {}

    /**
     * 导出 Excel 到 HTTP 响应流
     *
     * @param response  HTTP 响应
     * @param fileName  文件名（不含扩展名）
     * @param sheetName sheet 名称
     * @param headClass 表头类（@ExcelProperty 注解）
     * @param dataList  数据列表
     */
    public static <T> void export(HttpServletResponse response,
String fileName,
String sheetName,
                                   Class<T> headClass,
                                   List<T> dataList) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + encodedName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), headClass)
                .sheet(sheetName)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .doWrite(dataList);
    }

    /**
     * 从 InputStream 读取 Excel
     *
     * @param inputStream 输入流
     * @param headClass   表头类
     * @param consumer    每读取一批数据后的回调
     */
    public static <T> void read(InputStream inputStream,
                                 Class<T> headClass,
                                 Consumer<List<T>> consumer) {
        EasyExcel.read(inputStream, headClass, new PageReadListener<>(consumer::accept))
                .sheet()
                .doRead();
    }
}
