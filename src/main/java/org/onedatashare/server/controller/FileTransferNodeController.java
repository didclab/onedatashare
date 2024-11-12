package org.onedatashare.server.controller;

import org.onedatashare.server.model.node.FileTransferNodeMetaData;
import org.onedatashare.server.service.TransferSchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController()
@RequestMapping("/api/nodes")
public class FileTransferNodeController {

    private final TransferSchedulerService transferSchedulerService;

    public FileTransferNodeController(TransferSchedulerService transferSchedulerService) {
        this.transferSchedulerService = transferSchedulerService;
    }

    @GetMapping("/{user}")
    public ResponseEntity<List<FileTransferNodeMetaData>> getUserOdsConnectors(Principal principal) {
        return ResponseEntity.ok(this.transferSchedulerService.getUsersFileTransferNodes(principal.getName()));
    }

    @GetMapping("/ods")
    public ResponseEntity<List<FileTransferNodeMetaData>> getOdsNodes() {
        return ResponseEntity.ok(this.transferSchedulerService.getOdsNodes());
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> totalConnectedFileTransferNodes() {
        return ResponseEntity.ok(this.transferSchedulerService.totalConnectedFileTransferNodes());
    }
}
