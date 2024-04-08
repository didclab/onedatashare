package org.onedatashare.server.model.carbon;

import lombok.Data;

@Data
public class CarbonIpEntry {
    private String ip;
    private int carbonIntensity;
    private double lat;
    private double lon;

}
