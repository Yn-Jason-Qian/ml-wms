package com.wms.web.dto;

import lombok.Data;

@Data
public class DashboardStatsDTO {
    private long todayReceive;
    private long todayPutaway;
    private long todayPick;
    private long todayShip;
    private long yesterdayReceive;
    private long yesterdayPutaway;
    private long yesterdayPick;
    private long yesterdayShip;
}
