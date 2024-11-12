package org.onedatashare.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TransferSla {
    public double percentCarbon;
    public double percentThroughput;
    public double percentElectricity;

    public TransferSla() {
        percentCarbon = 0.0;
        percentThroughput = 0.0;
        percentElectricity = 0.0;
    }
}