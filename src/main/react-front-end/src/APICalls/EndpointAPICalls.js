import { url, ENDPOINT_OP_URL, LIST_OP_URL, SHARE_OP_URL, MKDIR_OP_URL, SFTP_DOWNLOAD_URL, DEL_OP_URL, DOWNLOAD_OP_URL } from '../constants';
import { axios, statusHandle } from "./APICalls";
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
    };

    body = Object.keys(endpoint.credential).length > 0 ? { ...body, credential: endpoint.credential } : body;

    let callback = accept;

    axios.post(buildEndpointOperationURL(ENDPOINT_OP_URL, getUriType(uri), LIST_OP_URL), JSON.stringify(body))
        .then((response) => {
            if (!(response.status === 200))
                callback = fail;
            statusHandle(response, callback);
        })
        .catch((error) => {
            statusHandle(error, fail);
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
            statusHandle(error, fail);
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
            statusHandle(error, fail);
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

            statusHandle(error, fail);
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
	openOAuth("/api/stork/oauth?type=dropbox");
}

export async function openGoogleDriveOAuth() {
	openOAuth("/api/stork/oauth?type=googledrive");
}

export async function openGridFtpOAuth() {
	openOAuth("/api/stork/oauth?type=gridftp");
}

export async function openBoxOAuth(){
    openOAuth("api/stork/oauth?type=box");
}

export async function openOAuth(url){
	window.location = url;
}


export async function globusEndpointIds(gep, accept, fail) {
	let callback = accept;
	axios.post(url + 'globus', {
		action: 'endpointId',

		globusEndpoint: gep,
	}).then((response) => {
		if (!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
		.catch((error) => {
			statusHandle(error, fail);
		});
}

export async function globusEndpointDetail(gep, accept, fail) {
	let callback = accept;
	axios.post(url + 'globus', {
		action: 'endpoint',
		globusEndpoint: gep,
	}).then((response) => {
		if (!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
		.catch((error) => {
			statusHandle(error, fail);
		});
}

export async function globusEndpointActivate(gep, _username, _password, accept, fail) {
	let callback = accept;
	axios.post(url + 'globus', {
		action: 'endpointActivate',
		globusEndpoint: gep,
		username: _username,
		password: _password
	}).then((response) => {
		if (!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
		.catch((error) => {
			statusHandle(error, fail);
		});
}

export async function deleteEndpointId(ged, accept, fail) {
	let callback = accept;

	axios.post(url + 'globus', {
		action: "deleteEndpointId",
		globusEndpoint: ged,
	})
		.then((response) => {
			if (!(response.status === 200))
				callback = fail;
			statusHandle(response, callback);
		})
		.catch((error) => {
			statusHandle(error, fail);
		});
}

export async function globusListEndpoints(filter_fulltext, accept, fail) {
	let callback = accept;
	return axios.post(url + 'globus', {
		action: "endpoint_list",
		filter_fulltext: filter_fulltext
		})
		.then((response) => {
			if (!(response.status === 200))
				callback = fail;
			statusHandle(response, callback);
		})
		.catch((error) => {

			statusHandle(error, fail);
		});
}





//api call for terminal
export async function CliInterface(inp_cmd, accept, fail) {
	let callback = accept;
	return axios.post('http://localhost:3000/api/ssh/console',
                                { "host": "timberlake.cse.buffalo.edu",
                                  "commandWithPath": inp_cmd,
                                  "credential" : {"username" : "stella3","password" : "P@ssword234"},
                                  "port": 22 }).then((response) => {
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


