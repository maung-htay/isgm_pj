package com.isgm.camreport.model;

import java.util.Collection;
import java.util.List;

public class Area {
    int areaId;
    String areaName;
    int circuitId;
    List<Route> routeList;

    public Area() { }

    public Area(int areaId, String areaName, int circuitId, List<Route> routeList) {
        this.areaId = areaId;
        this.areaName = areaName;
        this.circuitId = circuitId;
        this.routeList = routeList;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public int getCircuitId() {
        return circuitId;
    }

    public void setCircuitId(int circuitId) {
        this.circuitId = circuitId;
    }

    public Collection<? extends Route> getRouteList() {
        return routeList;
    }

    public void setRouteList(List<Route> routeList) {
        this.routeList = routeList;
    }

}
