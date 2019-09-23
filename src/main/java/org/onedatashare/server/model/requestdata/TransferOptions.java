package org.onedatashare.server.model.requestdata;

import lombok.Data;

@Data
public class TransferOptions {
    private Boolean compress;
    private Boolean encrypt;
    private String optimizer;
    private boolean overwrite;
    private Integer retry;
    private Boolean verify;
}
