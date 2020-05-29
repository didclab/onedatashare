package org.onedatashare.server.model.filesystem.operations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationBase {
    protected String credId;
    protected String path;
    protected String id;
}
