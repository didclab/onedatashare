package org.onedatashare.server.controller.endpoint;

import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.model.response.DownloadResponse;
import org.onedatashare.server.service.GridFtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/gridftp/")
public class GridFtpController extends EndpointBaseController{
    @Autowired
    private GridFtpService gridftpService;

    @Override
    protected Mono<Stat> listOperation(ListOperation listOperation) {
        return null;
    }

    @Override
    protected Mono<Void> mkdirOperation(MkdirOperation operation) {
        return null;
    }

    @Override
    protected Mono<Void> deleteOperation(DeleteOperation deleteOperation) {
        return null;
    }

    @Override
    protected Mono<DownloadResponse> downloadOperation(DownloadOperation downloadOperation) {
        return null;
    }

//    protected Mono<Void> mkdirOperation(OperationRequestData operationRequestData) {
//        UserAction userAction = UserAction.convertToUserAction(operationRequestData);
//        return gridftpService.mkdir(null, userAction);
//    }
//
//    protected Mono<Void> deleteOperation(OperationRequestData operationRequestData) {
//        UserAction userAction = UserAction.convertToUserAction(operationRequestData);
//        return gridftpService.delete(null, userAction);
//    }
//
//    protected Mono<String> downloadOperation(RequestData requestData) {
//        return null;
//    }
//
//    protected Mono<Stat> listOperation(RequestData requestData) {
//        UserAction userAction = UserAction.convertToUserAction(requestData);
//        return gridftpService.list(null, userAction);
//    }
}