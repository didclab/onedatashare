import { url } from '../constants';
import { axios, statusHandle, handleRequestFailure } from "./APICalls";


let globusBaseUrl = url + 'globus/';

/**
 * Fetches the globus endpoints stored in the database
 * @param {Function} accept 
 * @param {Function} fail 
 */
export async function globusFetchEndpoints(accept, fail) {
    let callback = accept;
    axios.get(globusBaseUrl + 'endpoints')
        .then((response) => {
            if (!(response.status === 200))
                callback = fail;
            statusHandle(response, callback);
        })
        .catch((error) => {
            handleRequestFailure(error, fail);
        });
}

/**
 * Adds a new globus endpoint on the backed
 * @param {Object} gep 
 * @param {Function} accept 
 * @param {Function} fail 
 */
export async function globusAddEndpoint(gep, accept, fail) {
    let callback = accept;
    axios.post(globusBaseUrl + 'endpoint', gep)
        .then((response) => {
            if (!(response.status === 200))
                callback = fail;
            statusHandle(response, callback);
        })
        .catch((error) => {
            handleRequestFailure(error, fail);
        });
}

/**
 * Fetches the globus endpoint details on the frontend
 * @param {String} endpointId 
 * @param {*} accept 
 * @param {*} fail 
 */
export async function globusEndpointDetail(endpointId, accept, fail) {
    let callback = accept;
    
    axios.get(globusBaseUrl + 'endpoint/' + endpointId)
        .then((response) => {
            if (!(response.status === 200))
                callback = fail;
            statusHandle(response, callback);
        })
        .catch((error) => {
            handleRequestFailure(error, fail);
        });
}


/**
 * Opens a new window to activate globus endoint via globus website
 * @param {String} id 
 */
export async function globusEndpointActivateWeb(id) {
    console.log("In globus activate web");
    axios.get(globusBaseUrl + 'endpoint-activate/' + id)
        .then((response) => {
            if (!(response.status === 200)){
            }
            console.log(`Enpoint needs to be activated on ${response.data.url}`)
            window.open(response.data.url);
        })
        .catch((error) => {
            console.error("Unable to fetch globus activation URL");
        });
}

/**
 * Deletes the endpoint with the given Id from the backend database
 * @param {String} id 
 * @param {*} accept 
 * @param {*} fail 
 */
export async function deleteEndpointId(id, accept, fail) {
    
    let callback = accept;
    axios.delete(globusBaseUrl + 'endpoint/' + id)
        .then((response) => {
            if (!(response.status === 200))
                callback = fail;
            statusHandle(response, callback);
        })
        .catch((error) => {
            handleRequestFailure(error, fail);
        });
}

/**
 * Fetches the endpoints with the filtered text visible to the user
 * @param {String} filter_fulltext 
 * @param {*} accept 
 * @param {*} fail 
 */
export async function globusListEndpoints(filter_fulltext, accept, fail) {
    let callback = accept;
    let getUrl = globusBaseUrl + 'endpoints/all?filter=' + filter_fulltext;
    return axios.get(getUrl)
        .then((response) => {
            if (!(response.status === 200))
                callback = fail;
            statusHandle(response, callback);
        })
        .catch((error) => {
            handleRequestFailure(error, fail);
        });
}

export async function globusAttemptActivation(id, accept, fail) {
    let callback = accept;
    let postUrl = url + 'globus/attempt-activation/' + id;
    return axios.post(postUrl)
        .then((response) => {
            if (!(response.status === 200))
                callback = fail;
            statusHandle(response, callback);
        })
        .catch((error) => {
            handleRequestFailure(error, fail);
        });
}
