package org.onedatashare.server.model.requestdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InfluxData {

    private String networkInterface;

    private String odsUser;

    private String transferNodeName;

    private Long coreCount;

    private Double cpu_frequency_max;

    private Double cpu_frequency_current;

    private Double cpu_frequency_min;

    private String cpuArchitecture;

    private Double packetLossRate;

    private Long bytesSent;

    private Long bytesReceived;

    private Long packetSent;

    private Long packetReceived;

    private Long dropin;

    private Long dropout;

    private Long nicMtu;

    private Double latency;

    private Double rtt;

    private Double sourceRtt;

    private Double sourceLatency;

    private Double destinationRtt;

    private Double destLatency;

    private Long errin;

    private Long errout;

    private String jobId;

    private Double readThroughput;

    private Double writeThroughput;

    private Long bytesWritten;

    private Long bytesRead;

    private Long concurrency;

    private Long parallelism;

    private Long pipelining;

    private Long memory;

    private Long maxMemory;

    private Long freeMemory;

    private Long allocatedMemory;

    private Long jobSize;

    private Long avgFileSize;

    private String sourceType;

    private String sourceCredId;

    private String destType;

    private String destCredId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    @JsonProperty(value = "start_time")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    @JsonProperty(value = "end_time")
    private LocalDateTime endTime;

    private Double throughput;

    private Long dataBytesSent;

    private Boolean compression;


}
