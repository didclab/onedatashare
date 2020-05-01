import { url } from '../constants';
import { axios, statusHandle, handleRequestFailure } from "./APICalls";

export async function globusFetchEndpoints(accept, fail) {
    let callback = accept;
    axios.get(url + 'globus/fetch-endpoints')
        .then((response) => {
            if (!(response.status === 200))
                callback = fail;
            statusHandle(response, callback);
        })
        .catch((error) => {
            handleRequestFailure(error, fail);
        });
}

export async function globusAddEndpoint(gep, accept, fail) {
    let callback = accept;
    axios.post(url + 'globus/add-endpoint', gep)
        .then((response) => {
            if (!(response.status === 200))
                callback = fail;
            statusHandle(response, callback);
        })
        .catch((error) => {
            handleRequestFailure(error, fail);
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
            handleRequestFailure(error, fail);
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
            handleRequestFailure(error, fail);
        });
}

export async function deleteEndpointId(id, accept, fail) {
    let callback = accept;
    axios.delete(url + 'globus/delete-endpoint/' + id)
        .then((response) => {
            if (!(response.status === 200))
                callback = fail;
            statusHandle(response, callback);
        })
        .catch((error) => {
            handleRequestFailure(error, fail);
        });
}

export async function globusListEndpoints(filter_fulltext, accept, fail) {
    let callback = accept;
    let getUrl = url + 'globus/list-endpoints?filter=' + filter_fulltext;
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