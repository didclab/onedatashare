package org.onedatashare.server.model.filesystem.operations;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteOperation extends OperationBase{
    private String folderToDelete;

    @Builder
    public DeleteOperation(String credId, String path, String id) {
        super(credId, path, id);
    }
}
