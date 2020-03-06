package org.onedatashare.server.model.request;

import lombok.Data;
import org.onedatashare.server.model.useraction.UserActionResource;

@Data
public class TransferRequest {
    UserActionResource src;
    UserActionResource dest;
    TransferOptions options;
}
