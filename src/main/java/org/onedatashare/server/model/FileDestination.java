package org.onedatashare.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.onedatashare.server.model.core.EndpointType;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDestination implements Serializable {

    @NonNull
    private String credId;

    @NonNull
    private EndpointType type;

    @NonNull
    String fileDestinationPath;

}