package org.onedatashare.server.model.carbon;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CarbonIntensityMapKey {
    String ownerId;
    String transferNodeName;
    UUID jobUuid;
    LocalDateTime timeMeasuredAt;

}
