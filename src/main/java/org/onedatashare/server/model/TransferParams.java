package org.onedatashare.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferParams {
    Integer concurrency;
    Integer parallelism;
    Integer pipelining;
    Long chunkSize;
    String transferNodeName;
}
