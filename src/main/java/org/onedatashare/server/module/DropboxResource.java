/**
 * ##**************************************************************
 * ##
 * ## Copyright (C) 2018-2020, OneDataShare Team,
 * ## Department of Computer Science and Engineering,
 * ## University at Buffalo, Buffalo, NY, 14260.
 * ##
 * ## Licensed under the Apache License, Version 2.0 (the "License"); you
 * ## may not use this file except in compliance with the License.  You may
 * ## obtain a copy of the License at
 * ##
 * ##    http://www.apache.org/licenses/LICENSE-2.0
 * ##
 * ## Unless required by applicable law or agreed to in writing, software
 * ## distributed under the License is distributed on an "AS IS" BASIS,
 * ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * ## See the License for the specific language governing permissions and
 * ## limitations under the License.
 * ##
 * ##**************************************************************
 */


package org.onedatashare.server.module;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.EndpointCredential;
import org.onedatashare.server.model.credential.OAuthEndpointCredential;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.DownloadOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.model.request.TransferJobRequest;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import static org.onedatashare.server.model.core.ODSConstants.DROPBOX_URI_SCHEME;

/**
 * Resource class that provides services specific to Dropbox endpoint.
 */
public class DropboxResource extends Resource {

    private DbxClientV2 client;

    public DropboxResource(EndpointCredential credential, String clientIdentifier) {
        super(credential);
        DbxRequestConfig config = DbxRequestConfig.newBuilder(clientIdentifier).build();
        this.client = new DbxClientV2(config, ((OAuthEndpointCredential) credential).getToken());
    }

    @Override
    public String pathFromUrl(String url) throws UnsupportedEncodingException {
        if (url.startsWith(DROPBOX_URI_SCHEME)) {
            //Dropbox root starts with '/'
            url = url.substring(DROPBOX_URI_SCHEME.length());  //TODO: check if needed to add "/" at the beginning
        }
        return super.pathFromUrl(url);
    }

    private Stat statHelper(String url) throws FileNotFoundException, DbxException {
        Stat stat = new Stat().setName(url);
        ListFolderResult data = null;
        Metadata mData = null;
        try {
            data = this.client.files().listFolder((url.equals("/") ? "" : url));
        } catch (ListFolderErrorException e) {
            mData = this.client.files().getMetadata(url);
        }
        if (data == null && mData == null) {
            throw new FileNotFoundException();
        } else if (data == null) {
            stat = mDataToStat(mData);
        } else {
            stat.setDir(true);
            stat.setFile(false);
        }

        if (stat.isDir()) {
            ListFolderResult lfr = null;
            if (stat.getName().equals("/")) {
                lfr = this.client.files().listFolder("");
            } else {
                // If the metadata is a directory
                if (this.client.files().getMetadata(url) instanceof FolderMetadata) {
                    // list the directory files
                    lfr = this.client.files().listFolder(url);
                }
                // If the metadata is a file
                else if (this.client.files().getMetadata(url) instanceof FileMetadata) {
                    // Return the metadata as a stat object
                    stat = mDataToStat(this.client.files().getMetadata(url));
                }
            }
            List<Stat> sub = new LinkedList<>();
            for (Metadata child : lfr.getEntries())
                sub.add(mDataToStat(child));
            stat.setFiles(sub);
        }
        return stat;
    }

    private Stat mDataToStat(Metadata data) {
        Stat stat = new Stat(data.getName());
        if (data instanceof FileMetadata) {
            FileMetadata file = (FileMetadata) data;
            stat.setFile(true);
            stat.setSize(file.getSize());
            stat.setId(((FileMetadata) data).getId());
            stat.setTime(file.getClientModified().getTime() / 1000);
            stat.setId(((FileMetadata) data).getId());
            stat.setName(data.getName());
        }
        if (data instanceof FolderMetadata) {
            stat.setDir(true);
            stat.setId(((FolderMetadata) data).getId());
            stat.setName(data.getName());
        }
        return stat;
    }

    @Override
    public Mono<Stat> list(ListOperation operation) {
        return Mono.create(s -> {
            try {
                String url = pathFromUrl(operation.getPath());
                s.success(statHelper(url));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException | DbxException e) {
                s.error(e);
            }
        });
    }

    @Override
    public Mono<Void> mkdir(MkdirOperation operation) {
        return Mono.create(s -> {
            try {
                this.client.files().createFolderV2(this.pathFromUrl(operation.getPath() + operation.getFolderToCreate()));
                s.success();
            } catch (Exception e) {
                s.error(e);
            }
        });
    }

    @Override
    public Mono<Void> delete(DeleteOperation operation) {
        return Mono.create(s -> {
            try {
                this.client.files().deleteV2(this.pathFromUrl(operation.getPath() + operation.getToDelete()));
                s.success();
            } catch (DbxException | UnsupportedEncodingException e) {
                s.error(e);
            }
        });
    }

    @Override
    public Mono download(DownloadOperation operation) {
        return Mono.create(s -> {
            String downloadLink;
            try {
                String url = this.pathFromUrl(operation.getPath() + operation.getFileToDownload());
                //temporary link valid for 4 hours
                downloadLink = this.client.files().getTemporaryLink(url).getLink();
            } catch (DbxException | UnsupportedEncodingException e) {
                s.error(e);
                return;
            }
            s.success(downloadLink);
        });
    }

    public static Mono<? extends Resource> initialize(EndpointCredential credential, String clientIdentifier) {
        return Mono.create(s -> {
            try {
                DropboxResource dropBoxResource = new DropboxResource(credential, clientIdentifier);
                s.success(dropBoxResource);
            } catch (Exception e) {
                s.error(e);
            }
        });
    }
}
