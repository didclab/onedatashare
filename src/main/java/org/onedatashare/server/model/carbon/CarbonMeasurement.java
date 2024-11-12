package org.onedatashare.server.model.carbon;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CarbonMeasurement {
    List<CarbonIpEntry> traceRouteCarbon;
    String ownerId;
    String transferNodeName;
    UUID jobUuid;
    LocalDateTime timeMeasuredAt;
}
