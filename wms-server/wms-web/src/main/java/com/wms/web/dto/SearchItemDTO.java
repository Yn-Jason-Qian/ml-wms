package com.wms.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchItemDTO {
    private String type;
    private Long id;
    private String title;
    private String subtitle;
    private String url;
}
