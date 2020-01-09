import { url } from '../constants';
import { logoutAction } from "../model/actions.js";
import { store } from "../App.js";
import Axios from "axios";

import { getType, getTypeFromUri } from '../constants.js';
import { getMapFromEndpoint, getIdsFromEndpoint } from '../views/Transfer/initialize_dnd.js';

import { cookies } from "../model/reducers.js";
const FETCH_TIMEOUT = 10000;


const axios = Axios.create({
  timeout: FETCH_TIMEOUT,
  headers: {
  	Accept: 'application/json',
	'Content-Type': 'application/json'
  }
});

function statusHandle(response, callback){
	//console.log(response)
	const statusFirstDigit = Math.floor(response.status/100);
	if(statusFirstDigit < 3){
		// 100-200 success code=
		callback(response.data);
	}else
	if(statusFirstDigit < 5){
		// 300-499 redirect/user error code
		callback(`${response.status} ${response.statusText}`);
	}else{
		// 500 error code
		if(response.name === "PermissionDenied" && store.getState().login){
			if (window.confirm('You have been logged out. Login again?'))
			{
				store.dispatch(logoutAction());
			}
		}
		if (response.status === 408 || response.code === 'ECONNABORTED') {
	      callback(`Timeout 10000ms`)
	      return;
	    }
		callback(`500`);
	}
}

/*
	Desc: Check if current email is a user
	input: Email
	accept: (successMessage:string){}
	fail: (errorMessage:string){}
*/
export async function checkLogin(email, accept, fail){
	axios.post(url+'user', {
	    action: 'verifyEmail',
	    email: email,
	}).then((response) => {
		if((response.data === true)){
			statusHandle(response, accept);
		}else if(response.data === false){
			statusHandle(response, fail);
		}
	})
	.catch((error) => {
		statusHandle(error, fail);
	});
}


/*
	Desc: Send a code to the user
	input: Email
	accept: (successMessage:string){}
	fail: (errorMessage:string){}
*/

export async function resetPasswordSendCode(email, accept, fail){
	var callback = accept;

	axios.post(url+'user', {
	    action: 'sendVerificationCode',
	    email: email
	}).then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	}).catch((error) => {
      statusHandle(error, fail);
    });
}


/*
	Desc: Verify Code for the user
	input: Email
	accept: (successMessage:string){}
	fail: (errorMessage:string){}
*/

export async function resetPasswordVerifyCode(email,code, accept, fail){
	var callback = accept;

	axios.post(url+'user', {
	    action: 'verifyCode',
	    email: email,
	    code: code
	}).then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	}).catch((error) => {
      statusHandle(error, fail);
    });
}

/*
	Desc: Send a code to the user
	input: Email
	accept: (successMessage:string){}
	fail: (errorMessage:string){}
*/
export async function resetPassword(email,code,password, cpassword, accept, fail){
	var callback = accept;

	axios.post(url+'user', {
	    action: 'setPassword',
	    email: email,
	    code: code,
	    password: password,
	    confirmPassword: cpassword
	}).then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	}).catch((error) => {
      statusHandle(error, fail);
    });
}

export async function resendVerificationCode(emailId){
	return axios.post(url+'user',{
		action:'resendVerificationCode',
		email: emailId
	})
	.then((response) => {
		return response
	})
	.catch((error) =>{

	});
}

export async function setPassword(emailId, code, password, confirmPassword) {

    return axios.post(url+'user', {
    	    action: "setPassword",
    	    email : emailId,
    	    code : code,
    	    password : password,
    	    confirmPassword : confirmPassword
    	})
    	.then((response) => {
    		if(!(response.status === 200))
    			throw new Error("Failed to set password for users account")
    		else {
                    return response;
                }
    	})
    	.catch((error) => {
          return {status : 500}
        });
}


/*
	Desc: Login and return a hash
	input: Email
	accept: (successMessage:string){}
	fail: (errorMessage:string){}
*/
export async function login(email, password, accept, fail){
	var callback = accept;

	axios.post(url+'user', {
	    action: 'login',
	    email: email,
	    password: password,
	}).then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {

      statusHandle(error, fail);
    });
}

export async function isAdmin(email, hash, accept, fail){
	var callback = accept;
	axios.post(url+'user', {
	    action: 'isAdmin',
	    email: email,
	    hash: hash,
	}).then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {

      statusHandle(error, fail);
    });
}

export async function history(uri, portNum, accept, fail){
	var callback = accept;

	axios.post(url+'user', {
	    action: 'history',
	    portNumber: portNum,
	    uri: encodeURI(uri)
	}).then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {
      statusHandle(error, fail);
    });
}

export async function globusEndpointIds(gep,  accept, fail){
	var callback = accept;
	axios.post(url+'globus', {
	    action: 'endpointId',

	    globusEndpoint: gep,
	}).then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {
      statusHandle(error, fail);
    });
}

export async function globusEndpointDetail(gep, accept, fail){
	var callback = accept;
	axios.post(url+'globus', {
	    action: 'endpoint',
	    globusEndpoint: gep,
	}).then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {
      statusHandle(error, fail);
    });
}

export async function globusEndpointActivate(gep,_username, _password, accept, fail){
	var callback = accept;
	axios.post(url+'globus', {
	    action: 'endpointActivate',
	    globusEndpoint: gep,
	    username: _username,
	    password: _password
	}).then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {
      statusHandle(error, fail);
    });
}


export async function deleteHistory(uri, accept, fail){
	var callback = accept;

	axios.post(url+'user', {
		action: "deleteHistory",
	    uri: encodeURI(uri)
	})
	.then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {
      statusHandle(error, fail);
    });
}

export async function deleteEndpointId(ged, accept, fail){
	var callback = accept;

	axios.post(url+'globus', {
		action: "deleteEndpointId",
	    globusEndpoint: ged,
	})
	.then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {
      statusHandle(error, fail);
    });
}

/*
	Desc: List credentials for dropbox and googledrive
*/
export async function savedCredList(accept, fail){
	var callback = accept;
	axios.get(url+'cred?action=list')
	.then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {

      statusHandle(error, fail);
    });
}

/*
	Desc: Extract all transfers for the user
*/
export async function fetchUserJobs(pageNo, pageSize, sortBy, order, accept, fail) {
	var callback = accept;
	axios.post(`${url}q`, {
		status: 'userJob',
		pageNo: pageNo,
		pageSize: pageSize,
		sortBy: sortBy,
		sortOrder: order
	})
		.then((response) => {
			if(!(response.status === 200))
				callback = fail;
			statusHandle(response, callback);
		})
		.catch((error) => {
			fail(error);
		});
}

/*
	Desc: Fetch all transfers. Only for Admins
*/
export async function fetchAllJobs(owner, pageNo, pageSize, sortBy, order, accept, fail) {
	var callback = accept;
	axios.post(url+'q', {
		status: 'all',
		owner: owner,
		pageNo: pageNo,
		pageSize: pageSize,
		sortBy: sortBy,
		sortOrder: order
	})
		.then((response) => {
			if(!(response.status === 200))
				callback = fail;
			statusHandle(response, callback);
		})
		.catch((error) => {
			fail(error);
		});
}

export async function updateJobStatus(jobIds,accept, fail){
	var callback = accept;
	axios.post(url+'q/update', jobIds)
	.then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {
      fail(error);
    });
}

// Service method that connects with ODS backend to submit an issue reported by the user and create a ticket.
export async function submitIssue(reqBody, success, fail){
	var callback = success;

	axios.post(url+'ticket', reqBody).then((resp) =>{
		if(!(resp.status === 200))
		 callback = fail;
		statusHandle(resp, callback)
	})
	.catch((err) =>{
		fail(err)
	});
}

export async function submit(src, srcEndpoint, dest, destEndpoint, options,accept, fail){
	var callback = accept;
	// console.log(src)
	var src0 = Object.assign({}, src);
	var dest0 = Object.assign({}, dest);
	if(Object.keys( src0.credential ).length === 0){
		delete src0["credential"];
	}
	if(Object.keys( dest0.credential ).length === 0){
		delete dest0["credential"];
	}

	axios.post(url+'submit', {
	    src: {...src0, type: getType(src0), map: getMapFromEndpoint(srcEndpoint)},
	    dest: {...dest0, type: getType(dest0), map: getMapFromEndpoint(destEndpoint)},
	    options:options
	}).then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {
      statusHandle(error, fail);
    });
}

export async function listFiles(uri, endpoint, id, accept, fail){
	var body = {
	    uri: encodeURI(uri),
	    id: id,
	    portNumber: endpoint.portNumber,
	    type: getTypeFromUri(uri)
	  };

	body = Object.keys(endpoint.credential).length > 0 ? {...body, credential: endpoint.credential} : body;

	var callback = accept;
	axios.post(url+'ls', JSON.stringify(body))
	.then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {
      statusHandle(error, fail);
    });
}

export async function share(uri, endpoint, accept, fail){
	var callback = accept;

	axios.post(url+'share', {
	    credential: endpoint.credential,
	    uri: encodeURI(uri),
	    type: getTypeFromUri(uri),
	    map: getMapFromEndpoint(endpoint),

	})
	.then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {

      statusHandle(error, fail);
    });
}

export async function mkdir(uri,type, endpoint,  accept, fail){
	var callback = accept;
	const ids = getIdsFromEndpoint(endpoint);
	const id = ids[ids.length - 1];
	axios.post(url+'mkdir', {
	    credential: endpoint.credential,
	    uri: encodeURI(uri),
	    id: id,
	    type: type,
	    map: getMapFromEndpoint(endpoint),
	})
	.then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {
      statusHandle(error, fail);
    });
}

export async function deleteCall(uri, endpoint, id, accept, fail){
	var callback = accept;
	axios.post(url+'delete', {
	    credential: endpoint.credential,
	    uri: encodeURI(uri),
	    id: id,
	    type: getTypeFromUri(uri),
	    map: getMapFromEndpoint(endpoint)
	})
	.then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {

      statusHandle(error, fail);
    });
}

// Returns the url for file. It is used to download the file and also to display in share url popup
async function getDownloadLink(uri, credential, _id){
	return axios.post(url+'download', {
		type: getTypeFromUri(uri),
		credential: credential,
		uri: encodeURI(uri),
		id: _id,
	})
	.then((response) => {
		if(!(response.status === 200))
			console.log("Error in download API call");
		else{
			return response.data
		}
	})
	.catch((error) => {
			console.log("Error encountered while generating download link");
	});
}

export async function getSharableLink(uri, credential, _id){
		return getDownloadLink(uri, credential, _id).then((response) => {
			return response
		})
}

export async function download(uri, credential, _id){
	return getDownloadLink(uri, credential, _id).then((response) => {
		if(response !== ""){
			window.open(response)
		}
		else{
			console.log("Error encountered while generating download link");
		}
	})
}

export async function getDownload(uri, credential){

	let json_to_send = {
		credential: credential,
		uri: uri,
	}

	const jsonStr = JSON.stringify(json_to_send);
	cookies.set("CX", jsonStr, { expires : 1});

	window.location = url + "download/file";
	setTimeout(() => {
		cookies.remove("CX");
	  }, 5000);
}

export async function upload(uri, credential, accept, fail){
	var callback = accept;
	axios.post(url+'share', {
	    credential: credential,
	    uri: encodeURI(uri),
	    type: getTypeFromUri(uri)
	}).then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {

      statusHandle(error, fail);
    });
}

/*
	Desc: Retrieve all the available users
*/
export async function getUsers(type, pageNo, pageSize, sortBy, order, accept, fail){
	var callback = accept;

	axios.post(url+'user', {
			action: type,
			pageNo: pageNo,
			pageSize: pageSize,
			sortBy: sortBy,
			sortOrder: order
	})
	.then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {

      statusHandle(error, fail);
    });
}

export async function getUser(email, accept, fail){
    var callback = accept;

    axios.post(url+'user',{
        action: "getUser",
        email: email,
    })
    .then((response) => {
        if(!(response.status === 200))
        callback = fail;
        statusHandle(response, callback);
    })
    .catch((error) => {
        statusHandle(error, fail);
    })
}

export async function updateSaveOAuth(email, saveOAuth, successCallback){
	axios.post(url + 'user', {
		action: "updateSaveOAuth",
		email: email,
		saveOAuth: saveOAuth
	})
	.then((response) => {
		if(response.status === 200)
			successCallback();
	})
	.catch((error) => {
			console.log("Error encountered while updating the user.");
	});
}

/*
	Desc: Call the backend to save the OAuth Credentials when the user toggles
        the button in account preferences to save credentials
	input: Array of OAuth credentials
	accept: (successMessage:string){}
	fail: (errorMessage:string){}
*/

export async function saveOAuthCredentials(credentials,accept, fail){
	var callback = accept;
	axios.post(url+'cred/saveCredentials', credentials)
	.then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {
      fail(error);
    });
}

export async function updateAdminRightsApiCall(email, isAdmin){
	return axios.put(url+'user', {
		action: "updateAdminRights",
		email: email,
		isAdmin: isAdmin
	})
	.then((response) => {
		if(!(response.status === 200))
			return false;
		else{
			return true;
		}
	})
	.catch((error) => {
			console.log("Error encountered while updating the user.");
	});
}

/*
	Desc: Change Password
*/
export async function changePassword(oldPassword, newPassword,confirmPassword, accept, fail){
	var callback = accept;

	axios.post(url+'user', {
		action: "resetPassword",
	    password: oldPassword,
	    newPassword: newPassword,
	    confirmPassword: confirmPassword
	})
	.then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {
      fail(error);
    });
}

export async function cancelJob(jobID, accept, fail){
	var callback = accept;
	fetch(url+'cancel', {
	  method: 'POST',
	  headers: {
	    Accept: 'application/json',
	    'Content-Type': 'application/json',
	  },
	  body: JSON.stringify({
	    job_id: jobID
	  }),
	})
	.then((response) => {
		if(!response.ok)
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {
      fail(error);
    });
}

export async function deleteCredentialFromServer(uri, accept, fail){
	var callback = accept;

	axios.post(url+'user', {
		action: "deleteCredential",
	    uuid: uri
	})
	.then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {
      statusHandle(error, fail);
    });
}


export async function restartJob(jobID, accept, fail){
	var callback = accept;
	axios.post(url+'restart',{
		job_id: jobID
	})
	.then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {

      statusHandle(error, fail);
    });
}

export async function deleteJob(jobID, accept, fail){
	var callback = accept;
	axios.post(url+'deleteJob',{
		job_id: jobID
	})
	.then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {

      statusHandle(error, fail);
    });
}

/*
	Store user's view preference in the backend on toggle
	input: Email, viewPreference
	accept: (successMessage:string){}
	fail: (errorMessage:string){}
*/

export async function updateViewPreference(email, compactViewEnabled, accept, fail){
	var callback = accept;
	axios.post(url+'user', {
	    action: 'updateViewPreference',
	    email: email,
      compactViewEnabled: compactViewEnabled
	}).then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	}).catch((error) => {
      statusHandle(error, fail);
    });
}

export async function openDropboxOAuth(){
	openOAuth("/api/stork/oauth?type=dropbox");
}

export async function openGoogleDriveOAuth(){
	openOAuth("/api/stork/oauth?type=googledrive");
}

export async function openGridFtpOAuth(){
	openOAuth("/api/stork/oauth?type=gridftp");
}

export async function openOAuth(url){
	window.location = url;
}


export async function registerUser(requestBody, errorCallback) {

	return axios.post(url+'user', {action: "register", ...requestBody})
				.then((response) => {
						if(response.data && response.data.status && response.data.status === 302) {
							console.log("User already exists");
							return {status : 302}
						}
						if(!(response.status === 200))
							throw new Error("Failed to register user")
						else {
								return response
						}
					}
				)
				.catch((error) => {
						//statusHandle(error, fail);
						console.error("Error while registering user");
						errorCallback();
						return new Error({status: 500});
					}
				);
}


export async function verifyRegistraionCode(emailId, code) {
    return axios.post(url+'user', {
    	    action: "verifyCode",
    	    email : emailId,
    	    code : code
    	})
    	.then((response) => {
            return response;
    		//statusHandle(response, callback);
    	})
    	.catch((error) => {
          //statusHandle(error, fail);
          console.error("Error while verifying the registration code")
          return {status : 500}
        });
}

export async function globusListEndpoints( filter_fulltext, accept, fail) {
    var callback = accept;
    return axios.post(url+'globus', {
	    action : "endpoint_list",
	    filter_fulltext : filter_fulltext
	})
	.then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {

      statusHandle(error, fail);
    });
}
