package com.nongxinle.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** 今日派车沙盘页全局配送地图 ViewModel。 */
@Getter
@Setter
public class SandboxTodayMapOverviewDto {
    private SandboxTodayMapSummaryDto summary;
    private SandboxTodayMapDepotDto depot;
    private List<SandboxTodayMapMarkerDto> markers = new ArrayList<SandboxTodayMapMarkerDto>();
    private List<SandboxTodayMapPolylineDto> polylines = new ArrayList<SandboxTodayMapPolylineDto>();
    private List<SandboxTodayMapLegendItemDto> legend = new ArrayList<SandboxTodayMapLegendItemDto>();
    private List<SandboxTodayMapMissingStopDto> missingCoordinateStops = new ArrayList<SandboxTodayMapMissingStopDto>();
    private Double centerLat;
    private Double centerLng;
    private Integer suggestedScale;
    /** 微信 map 腾讯位置服务 key，用于启用个性化地图样式。 */
    private String subkey;
    /** 腾讯位置服务个性化地图样式 id。 */
    private Integer layerStyle;
    private String emptyHint;
}
