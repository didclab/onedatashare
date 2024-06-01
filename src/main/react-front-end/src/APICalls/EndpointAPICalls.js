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


import { ENDPOINT_OP_URL, LIST_OP_URL, MKDIR_OP_URL, SFTP_DOWNLOAD_URL, DEL_OP_URL, DOWNLOAD_OP_URL, S3, GOOGLEDRIVE_TYPE, BOX_TYPE, DROPBOX_TYPE, apiBaseUrl, getType, showType, isOAuth } from '../constants';
import { axios, statusHandle, handleRequestFailure } from "./APICalls";
import { getIdsFromEndpoint } from '../views/Transfer/initialize_dnd.js';
import { cookies } from "../model/reducers";

function getUriType(uri) {
    return uri.split(":")[0].toLowerCase();
}

// S3 has special case for getting API uri
function getUriTypeFromEndpoint(endpoint) {
    return endpoint?.credential?.type === showType.s3 ? S3 : endpoint?.uri.split(":")[0].toLowerCase()
}

function buildEndpointOperationURL(baseURL, endpointType, operation) {
    return baseURL + "/" + endpointType + operation;
}


//Issue with listing files in FTP when going into another directory besides the root directory.
// I've tested with inputting different file paths in the "path" value,
// but it seems that no matter what I input,
// it always shows the root directory instead of the directory with the name I inputted
// summarized info can be found in EndpointAuthenicateComponent.js and EndpointBrowseComponent.js
// SFTP Problem: When attempting listing, server gives a "cannot be found" error on the uri. URI is formatted as username@url

//added argument to check if service is S3, this is because S3's uri does not have "s3" in it, so getUriType() would fail
export async function listFiles(uri, endpoint, id, accept, fail) {

    let { params } = constructParamsForList({uri, endpoint, id})
    // console.log(params)
    let callback = accept;
    let url = buildEndpointOperationURL(ENDPOINT_OP_URL, getUriTypeFromEndpoint(endpoint), LIST_OP_URL)
    console.log(params)
    axios.get(url, { params })
        .then((response) => {
            if (!(response.status === 200))
                callback = fail;
                // console.log(response)
            statusHandle(response, callback);
        })
        .catch((error) => {
            handleRequestFailure(error, fail);
        });

}



export async function mkdir(uri, endpoint, accept, fail) {
    let callback = accept;
    const ids = getIdsFromEndpoint(endpoint);
    const id = ids[ids.length - 1];
    let type = getType(endpoint)
    axios.post(buildEndpointOperationURL(ENDPOINT_OP_URL, getUriTypeFromEndpoint(endpoint), MKDIR_OP_URL), {
        "identifier": isOAuth[type] ?  id : endpoint["credential"]["name"],
        "id": isOAuth[type] ?  id : endpoint["credential"]["name"],
        "credId": endpoint["credential"]["credId"] ? endpoint["credential"]["credId"] : endpoint["credential"]["uuid"],
        "path": uri.substr(0, uri.lastIndexOf("/") + 1),
        "folderToCreate": uri.substr(uri.lastIndexOf("/") + 1)
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
    let type = getType(endpoint)
    axios.post(buildEndpointOperationURL(ENDPOINT_OP_URL, getUriTypeFromEndpoint(endpoint), DEL_OP_URL), {
        "identifier": id || endpoint["credential"]["name"],
        "credId": endpoint["credential"]["credId"] || endpoint["credential"]["uuid"],
        "path": encodeURI(`${uri.substr(0, uri.lastIndexOf("/"))}`),
        "toDelete": isOAuth[type] ? id : encodeURI(uri.substr(uri.lastIndexOf("/") + 1))
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
           "identifier": credential["name"],
           "credId": credential["uuid"],
           "path": uri,
           "fileToDownload": ""
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
            window.open(response.url)
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
	openOAuth(apiBaseUrl + "oauth?type=dropbox");
}

export async function openGoogleDriveOAuth() {
	openOAuth(apiBaseUrl + "oauth?type=gdrive");
}

export async function openBoxOAuth(){
    openOAuth(apiBaseUrl + "oauth?type=box");
}

export async function openOAuth(url){
	window.location = url;
}

export const OAuthFunctions = {
    [DROPBOX_TYPE]: openDropboxOAuth,
    [GOOGLEDRIVE_TYPE]: openGoogleDriveOAuth,
    [BOX_TYPE]: openBoxOAuth,
    other: openOAuth
};

//api call for terminal
export async function CliInterface(inp_cmd,host,uname,epw,port,accept, fail) {
	let callback = accept;
	return axios.post('/api/ssh/console',
                                { "host": host,
                                  "commandWithPath": inp_cmd,
                                  "credential" : {"username" : uname,"password" : epw},
                                  "port": port}).then((response) => {
			if (!(response.status === 200))
				callback = fail;
			statusHandle(response, callback);
		    //console.log(response.data);
		    return response.data;
		})
		.catch((error) => {
			statusHandle(error, fail);
		});
}

/*
Handler to set Body Params based on type
*/
function constructParamsForList({uri, endpoint, id}) {
    let params = {}
    let type = getType(endpoint)
    if (type === showType.ftp) {
        params = { credId: endpoint["credential"]["credId"], path: id || "/"}
    } else if (isOAuth[type]) {
    params = { "identifier": id,
            "credId": endpoint["credential"]["credId"] || endpoint["credential"]["uuid"],
            "path": encodeURI(uri),
        }
    } else {
        params = {"credId": endpoint["credential"]["credId"] || endpoint["credential"]["uuid"], "path": id || "/"}
    }
    return { params }
}
