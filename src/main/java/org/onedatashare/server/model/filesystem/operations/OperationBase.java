package org.onedatashare.server.model.filesystem.operations;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OperationBase {
    protected String credId;
    protected String path;
    protected String id;
}
