package org.onedatashare.server.model.filesystem.operations;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DownloadOperation extends OperationBase{
    public String fileToDownload;
    public DownloadOperation(String credId, String path, String id) {
        super(credId, path, id);
    }
}
