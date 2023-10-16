package org.onedatashare.server.model;

import lombok.Data;
import org.onedatashare.server.model.request.TransferJobRequest;
import org.onedatashare.server.model.request.UserTransferOptions;

import java.io.Serializable;
import java.util.ArrayList;

@Data
public class TransferJobRequestDTO implements Serializable {
    private String ownerId;
    private FileSource source;
    private FileDestination destination;
    private UserTransferOptions options;
    private String transferNodeName;

    public static TransferJobRequestDTO transferFormUserRequest(TransferJobRequest transferJobRequest, String ownerId, String transferNodeName){
        TransferJobRequestDTO transferJobRequestDTO = new TransferJobRequestDTO();
        transferJobRequestDTO.setOptions(transferJobRequestDTO.getOptions());
        //construct file source
        FileSource fileSource = new FileSource();
        fileSource.setCredId(transferJobRequest.getSource().getCredId());
        fileSource.setFileSourcePath(transferJobRequest.getSource().getParentInfo().getPath());
        fileSource.setType(transferJobRequest.getSource().getType());
        fileSource.setResourceList(new ArrayList(transferJobRequest.getSource().getInfoList()));
        //construct file destination
        FileDestination fileDestination = new FileDestination();
        fileDestination.setCredId(transferJobRequest.getDestination().getCredId());
        fileDestination.setType(transferJobRequest.getDestination().getType());
        fileDestination.setFileDestinationPath(transferJobRequest.getDestination().getParentInfo().getPath());
        transferJobRequestDTO.setSource(fileSource);
        transferJobRequestDTO.setDestination(fileDestination);
        transferJobRequestDTO.setOwnerId(ownerId);
        if(transferNodeName == null){
            transferNodeName = "";
        }
        transferJobRequestDTO.setTransferNodeName(transferNodeName);
        return transferJobRequestDTO;
    }
}
