package org.onedatashare.server.model.carbon;

import lombok.Data;

@Data
public class CarbonMeasureResponse {
    public String transferNodeName;
    public Double averageCarbonIntensity;
}
