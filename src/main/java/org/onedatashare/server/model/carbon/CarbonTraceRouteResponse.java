package org.onedatashare.server.model.carbon;

import lombok.Data;

import java.util.List;

@Data
public class CarbonTraceRouteResponse {
    List<CarbonIpEntry> traceRoute;
    String transferNodeName;

}
