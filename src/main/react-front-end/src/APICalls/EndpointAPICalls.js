/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


import { ENDPOINT_OP_URL, LIST_OP_URL, SHARE_OP_URL, MKDIR_OP_URL, SFTP_DOWNLOAD_URL, DEL_OP_URL, DOWNLOAD_OP_URL, getType } from '../constants';
import { axios, statusHandle, handleRequestFailure } from "./APICalls";
import { getMapFromEndpoint, getIdsFromEndpoint } from '../views/Transfer/initialize_dnd.js';
import { cookies } from "../model/reducers";

function getUriType(uri) {
    return uri.split(":")[0].toLowerCase();
}

function buildEndpointOperationURL(baseURL, endpointType, operation) {
    return baseURL + "/" + endpointType + operation;
}

export async function listFiles(uri, endpoint, id, accept, fail) {
    let body = {
        uri: encodeURI(uri),
        id: id,
        portNumber: endpoint.portNumber,
        type: LIST_OP_URL,
        credential : "",
    };
    endpoint.credential["type"] = LIST_OP_URL
    body["credential"] = endpoint.credential
    let callback = accept;
    let url = buildEndpointOperationURL(ENDPOINT_OP_URL, getUriType(uri), LIST_OP_URL)
    axios.get(url, {params: body})
        .then((response) => {
            if (!(response.status === 200))
                callback = fail;
            statusHandle(response, callback);
        })
        .catch((error) => {
            handleRequestFailure(error, fail);
        });
}

export async function share(uri, endpoint, accept, fail) {
    let callback = accept;

    axios.post(buildEndpointOperationURL(ENDPOINT_OP_URL, getUriType(uri), SHARE_OP_URL), {
        credential: endpoint.credential,
        uri: encodeURI(uri),
        map: getMapFromEndpoint(endpoint),

    })
        .then((response) => {
            if (!(response.status === 200))
                callback = fail;
            statusHandle(response, callback);
        })
        .catch((error) => {
            handleRequestFailure(error, fail);
        });
}

export async function mkdir(uri, type, endpoint, accept, fail) {
    let callback = accept;

    const ids = getIdsFromEndpoint(endpoint);
    const id = ids[ids.length - 1];
    axios.post(buildEndpointOperationURL(ENDPOINT_OP_URL, getUriType(uri), MKDIR_OP_URL), {
        credential: endpoint.credential,
        uri: encodeURI(uri),
        id: id,
        map: getMapFromEndpoint(endpoint),
    })
        .then((response) => {
            if (!(response.status === 200))
                callback = fail;
            statusHandle(response, callback);
        })
        .catch((error) => {
            handleRequestFailure(error, fail);
        });
}

export async function deleteCall(uri, endpoint, id, accept, fail) {
    let callback = accept;

    axios.post(buildEndpointOperationURL(ENDPOINT_OP_URL, getUriType(uri), DEL_OP_URL), {
        credential: endpoint.credential,
        uri: encodeURI(uri),
        id: id,
        map: getMapFromEndpoint(endpoint)
    })
        .then((response) => {
            if (!(response.status === 200))
                callback = fail;
            statusHandle(response, callback);
        })
        .catch((error) => {

            handleRequestFailure(error, fail);
        });
}

// Returns the url for file. It is used to download the file and also to display in share url popup
async function getDownloadLink(uri, credential, _id) {
    return axios.post(buildEndpointOperationURL(ENDPOINT_OP_URL, getUriType(uri), DOWNLOAD_OP_URL), {
        credential: credential,
        uri: encodeURI(uri),
        id: _id,
    })
        .then((response) => {
            if (!(response.status === 200))
                console.log("Error in download API call");
            else {
                return response.data
            }
        })
        .catch((error) => {
            handleRequestFailure(error);
            console.log("Error encountered while generating download link");
        });
}

export async function getSharableLink(uri, credential, _id) {
    return getDownloadLink(uri, credential, _id).then((response) => {
        return response
    })
}

export async function download(uri, credential, _id) {
    return getDownloadLink(uri, credential, _id).then((response) => {
        if (response !== "") {
            window.open(response)
        }
        else {
            console.log("Error encountered while generating download link");
        }
    })
}

export async function getDownload(uri, credential) {
    let json_to_send = {
        credential: credential,
        uri: uri,
    }

    const jsonStr = JSON.stringify(json_to_send);
    cookies.set("CX", jsonStr, { expires: 1 });

    window.location = buildEndpointOperationURL(ENDPOINT_OP_URL, getUriType(uri), SFTP_DOWNLOAD_URL);
    setTimeout(() => {
        cookies.remove("CX");
    }, 5000);
}


export async function openDropboxOAuth() {
	openOAuth("/api/oauth?type=dropbox");
}

export async function openGoogleDriveOAuth() {
	openOAuth("/api/oauth?type=gdrive");
}

export async function openGridFtpOAuth() {
	openOAuth("/api/oauth?type=gftp");
}

export async function openBoxOAuth(){
    openOAuth("api/oauth?type=box");
}

export async function openOAuth(url){
	window.location = url;
}


