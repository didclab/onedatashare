package org.onedatashare.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import com.onedatashare.commonutils.model.credential.EndpointCredentialType;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDestination implements Serializable {

    @NonNull
    private String credId;

    @NonNull
    private EndpointCredentialType type;


    String fileDestinationPath = "";
}