package org.onedatashare.server.model.requestdata;

import lombok.Data;
import org.onedatashare.server.model.useraction.UserActionResource;

@Data
public class TransferRequestData {
    UserActionResource src;
    UserActionResource dest;
    TransferOptions options;
}
