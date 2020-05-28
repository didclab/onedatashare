package org.onedatashare.server.model.filesystem.operations;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MkdirOperation extends OperationBase{
    private String folderToCreate;

    @Builder
    public MkdirOperation(String credId, String path, String id) {
        super(credId, path, id);
    }
}
