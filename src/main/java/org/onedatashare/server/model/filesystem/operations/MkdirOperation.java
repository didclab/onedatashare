package org.onedatashare.server.model.filesystem.operations;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MkdirOperation extends OperationBase{
    private String folderToCreate;
}