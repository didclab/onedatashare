package org.onedatashare.server.service;

import org.onedatashare.server.model.core.EndpointType;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.filesystem.operations.*;
import org.onedatashare.server.model.request.TransferJobRequest;
import org.onedatashare.server.module.Resource;
import org.onedatashare.server.module.S3Resource;
import org.onedatashare.server.service.oauth.ResourceServiceBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.List;

@Service
public class S3Service extends ResourceServiceBase {

    @Autowired
    CredentialService credentialService;

    /**
     * Gets the Credential from the Credential-Service
     * @param credId
     * @return
     */
    @Override
    protected Mono<? extends Resource> getResource(String credId) {
        return credentialService.fetchAccountCredential(EndpointType.s3, credId)
                .flatMap(S3Resource::initialize)
                .subscribeOn(Schedulers.elastic());
    }

    /**
     * Delete the key of the specified key. The contents of Delete Operation will contain a Unique Key that is for an S3 file
     * @param operation
     * @return
     */

    @Override
    public Mono<Void> delete(DeleteOperation operation) {
        return this.getResource(operation.getCredId())
                .flatMap(s3Resource -> s3Resource.delete(operation));
    }

    /**
     * List a Bucket inside of the specified credId with a path otherwise list the entire buckets contents
     * @param operation
     * @return
     */
    @Override
    public Mono<Stat> list(ListOperation operation) {
        return this.getResource(operation.getCredId())
                .flatMap(s3Resource -> s3Resource.list(operation));
    }

    /**
     * Create a Directory inside of the specified credId
     * @param operation
     * @return
     */
    @Override
    public Mono<Void> mkdir(MkdirOperation operation) {
        return this.getResource(operation.getCredId())
                .flatMap(s3Resource -> s3Resource.mkdir(operation));
    }

    /**
     * This feature might not be needed at all here and will have to put into the Transfer-Service
     * @param operation
     * @return
     */
    @Override
    public Mono<String> download(DownloadOperation operation) { return null; }

    /**
     *
     * @param source
     * @return
     */
    @Override
    public Mono<List<TransferJobRequest.EntityInfo>> listAllRecursively(TransferJobRequest.Source source) {
        return null;
    }
}
