package com.wms.common.base;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> records;
    private long total;
    private int pageNum;
    private int pageSize;

    public static <T> PageResponse<T> of(List<T> records, long total, int pageNum, int pageSize) {
        return new PageResponse<>(records, total, pageNum, pageSize);
    }

    public static <T> PageResponse<T> empty(int pageNum, int pageSize) {
        return new PageResponse<>(Collections.emptyList(), 0, pageNum, pageSize);
    }

    public <R> PageResponse<R> map(Function<? super T, ? extends R> mapper) {
        List<R> mapped = records.stream().map(mapper).collect(Collectors.toList());
        return new PageResponse<>(mapped, total, pageNum, pageSize);
    }
}
