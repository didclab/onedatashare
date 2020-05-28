package org.onedatashare.server.model.filesystem.operations;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListOperation extends OperationBase{

    @Builder
    public ListOperation(String credId, String path, String id) {
        super(credId, path, id);
    }
}
