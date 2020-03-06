import axios from 'axios';

import { logoutAction } from "../model/actions.js";
import { store } from "../App.js";

class Request {
    constructor() {
        this.axiosInstance = axios.create();
        this.axiosInstance.interceptors.response.use(this.handleSuccess, this.handleError);
    }

    handleSuccess(response) {
        return response;
    }

    handleError(error) {
        switch (error.response.status) {
            //Unauthorized user
            case 401:
                if (store.getState.login) {
                    console.debug(`The access token is missing, expired or forged`);
                    store.dispatch(logoutAction);
                }
                else {
                    console.debug(`Login to make requests to this endpoint`);
                }
                this.redirectTo(document, signInUrl);
                break;
            //Bad Request 
            case 404:
                console.debug(`Bad Request`)
                break;
            default:
                break;
        }
        return Promise.reject(error)
    }

    redirectTo(document, path) {
        document.location = path
    }

    get(path, callback) {
        return this.axiosInstance.get(path).then(
            (response) => callback(response.status, response.data)
        );
    }

    post(path, payload, callback) {
        return this.axiosInstance.request({
            method: 'POST',
            url: path,
            responseType: 'json',
            data: payload
        }).then((response) => callback(response.status, response.data));
    }

    put(path, payload, callback) {
        return this.axiosInstance.request({
            method: 'PUT',
            url: path,
            responseType: 'json',
            data: payload
        }).then((response) => callback(response.status, response.data));
    }

    delete(path, payload, callback) {
        return this.axiosInstance.request({
            method: 'DELETE',
            url: path,
            responseType: 'json',
            data: payload
        }).then((response) => callback(response.status, response.data));
    }

    update(path, pa)
}

export default new Request;