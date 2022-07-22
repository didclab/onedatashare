package org.onedatashare.server.model.requestdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InfluxData {

    @JsonProperty(value = "interface")
    private String networkInterface;

    @JsonProperty(value = "ods_user")
    private String odsUser;

    @JsonProperty(value = "transfer_node_name")
    private String transferNodeName;

    @JsonProperty(value = "active_core_count")
    private Double coreCount;

    @JsonProperty(value = "cpu_frequency_max")
    private Double cpu_frequency_max;

    @JsonProperty(value = "cpu_frequency_current")
    private Double cpu_frequency_current;

    @JsonProperty(value = "cpu_frequency_min")
    private Double cpu_frequency_min;

    @JsonProperty(value = "energy_consumed")
    private Double energyConsumed;

    @JsonProperty(value = "cpu_arch")
    private String cpuArchitecture;

    @JsonProperty(value = "packet_loss_rate")
    private Double packetLossRate;

    @JsonProperty(value = "link_capacity")
    private Double linkCapacity;

    /* Delta values*/
    private Long bytesSentDelta;

    private Long bytesReceivedDelta;

    private Long packetsSentDelta;

    private Long packetsReceivedDelta;

    //NIC values

    @JsonProperty(value = "bytes_sent")
    private Long bytesSent;

    @JsonProperty(value = "bytes_recv")
    private Long bytesReceived;

    @JsonProperty(value = "packets_sent")
    private Long packetSent;

    @JsonProperty(value = "packets_recv")
    private Long packetReceived;

    @JsonProperty(value = "dropin")
    private Double dropin;

    @JsonProperty(value = "dropout")
    private Double dropout;

    @JsonProperty(value = "nic_speed")
    private Double nicSpeed;

    @JsonProperty(value = "nic_mtu")
    private Double nicMtu;

    //2022-06-01 10:41:15.123591
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    @JsonProperty(value = "start_time")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    @JsonProperty(value = "end_time")
    private LocalDateTime endTime;

    @JsonProperty(value = "latency")
    private Double latency;

    @JsonProperty(value = "rtt")
    private Double rtt;

    @JsonProperty(value = "errin")
    private Double errin;

    @JsonProperty(value = "errout")
    private Double errout;

    //Job Values

    private Long jobId;

    private Double throughput;

    private Integer concurrency;

    private Integer parallelism;

    private Integer pipelining;

    private Long memory;

    private Long maxMemory;

    private Long freeMemory;

    private Long jobSize;

    private Long avgFileSize;

    private Long dataBytesSent;

    private Boolean compression;

    private Long allocatedMemory;

    private String sourceType;

    private String destType;

}
