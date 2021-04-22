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


import { url, AUTH_ENDPOINT, RESET_PASSWD_ENDPOINT, IS_REGISTERED_EMAIL_ENDPOINT, 
	SEND_PASSWD_RST_CODE_ENDPOINT, REGISTRATION_ENDPOINT, EMAIL_VERIFICATION_ENDPOINT,   
	UPDATE_ADMIN_RIGHTS,
	GET_USER_JOBS_ENDPOINT,
	GET_ADMIN_JOBS_ENDPOINT,
	GET_USERS_ENDPOINT,
	GET_ADMINS_ENDPOINT,
	GET_USER_UPDATES_ENDPOINT,
	GET_ADMIN_UPDATES_ENDPOINT,
	UPDATE_PASSWD_ENDPOINT,
	GET_SEARCH_JOBS_ENDPOINT,
	LOGOUT_ENDPOINT,
	apiCredUrl} from '../constants';
import { logoutAction } from "../model/actions.js";
import { store } from "../App.js";
import Axios from "axios";
import { getType, getTypeFromUri } from '../constants.js';
import { getMapFromEndpoint } from '../views/Transfer/initialize_dnd.js';

const FETCH_TIMEOUT = 10000;

export const axios = Axios.create({
	timeout: FETCH_TIMEOUT,
	headers: {
		Accept: 'application/json',
		'Content-Type': 'application/json'
	}
});


export function handleRequestFailure(error, failureCallback){
	console.debug(`Error is` , error);
	if(error.response !== undefined){
		const responseCode = error.response.status;
		console.debug(`In status handle error code is ${responseCode}`);
		if(responseCode.code === 401 && store.getState.login === true){
			console.debug(`UnAuthorized api call. Please login`);
			store.dispatch(logoutAction);
		}
	}
	if(failureCallback !== undefined){
		failureCallback(error);
	}
	else{
		console.error("Undefined callback APICalls:handleRequestFailure");
	}
}

export function statusHandle(response, callback) {
	//console.log(response)
	const statusFirstDigit = Math.floor(response.status / 100);
	if (statusFirstDigit < 3) {
		// 100-200 success code=
		callback(response.data);
	} else
		if (statusFirstDigit < 5) {
			// 300-499 redirect/user error code
			callback(`${response.status} ${response.statusText}`);
		} else {
			// 500 error code
			if (response.name === "PermissionDenied" && store.getState().login) {
				if (window.confirm('You have been logged out. Login again?')) {
					store.dispatch(logoutAction());
				}
			if (response.status === 408 || response.code === 'ECONNABORTED') {
				callback(`Timeout 10000ms`)
				return;
			}
			// console.log(response)
			//const errorText = JSON.stringify(response.response.data);
			callback(`500`);
		}
	}
}

/*
	Desc: Check if current email is a user
	input: Email
	accept: (successMessage:string){}
	fail: (errorMessage:string){}
*/
export async function checkLogin(email, accept, fail){
	axios.post(IS_REGISTERED_EMAIL_ENDPOINT, {
	    email: email,
	}).then((response) => {
		if ((response.data === true)) {
			statusHandle(response, accept);
		} else if (response.data === false) {
			statusHandle(response, fail);
		}
	})
		.catch((error) => {
			handleRequestFailure(error, fail);
		});
}


/*
	Desc: Send a code to the user
	input: Email
	accept: (successMessage:string){}
	fail: (errorMessage:string){}
*/

export async function resetPasswordSendCode(email, accept, fail) {
	let callback = accept;

	axios.post(SEND_PASSWD_RST_CODE_ENDPOINT, {
	    email: email
	}).then((response) => {
		if (!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	}).catch((error) => {
		handleRequestFailure(error, fail);
	});
}


/*
	Desc: Verify Code for the user
	input: Email
	accept: (successMessage:string){}
	fail: (errorMessage:string){}
*/

export async function resetPasswordVerifyCode(email, code, accept, fail) {
	let callback = accept;

	axios.post(EMAIL_VERIFICATION_ENDPOINT, {
	    email: email,
	    code: code
	}).then((response) => {
		if (!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	}).catch((error) => {
		handleRequestFailure(error, fail);
	});
}

/*
	Desc: Send a code to the user
	input: Email
	accept: (successMessage:string){}
	fail: (errorMessage:string){}
*/
export async function resetPassword(email, code, password, cpassword, accept, fail) {
	let callback = accept;

	axios.post(RESET_PASSWD_ENDPOINT, {
	    email: email,
	    code: code,
	    password: password,
	    confirmPassword: cpassword
	}).then((response) => {
		if (!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	}).catch((error) => {
		handleRequestFailure(error, fail);
	});
}

export async function resendVerificationCode(emailId){
	return axios.post(SEND_PASSWD_RST_CODE_ENDPOINT, {
			email: emailId
		})
		.then((response) => {
			return response.data;
		})
		.catch((error) => {
			// statusHandle(error, fail);
		});
	}

export async function getAllUsers(email) {
	return axios.get(url + 'admin/getAllUsers', {
		email: email,
	}).then((response) => {
		if (response.status === 200 && response.data) {
			return response.data
		}
	}).catch(error => handleRequestFailure(error));
}

export async function getAllMails(email) {
	return axios.get(url + 'admin/getMails', {
		email: email,
	}).then((response) => {
		if (response.status === 200 && response.data) {
			return response.data
		}
	}).catch(error => handleRequestFailure(error));
}

export async function getAllTrashMails(email) {
	return axios.get(url + 'admin/getTrashMails', {
		email: email,
	}).then((response) => {
		if (response.status === 200 && response.data) {
			return response.data
		}
	}).catch(error => handleRequestFailure(error));
}

export async function deleteMail(uuid) {
	return fetch(url + 'admin/deleteMail', {
		method: 'POST',
		headers: {
			Accept: 'application/json',
			'Content-Type': 'application/json',
		},
		body: JSON.stringify({
			mailId: uuid
		}),
	}).then((response) => {
		if (response.status === 200 && response.data) {
			return response.data;
		} else {
			return response;
		}
	}).catch((error) => {
		handleRequestFailure(error);
		return error.data;
	});
}


export async function sendEmailNotification(senderEmail, subject, message, emailList, isHtml) {
	return axios.post(url + 'admin/sendNotifications', {
		senderEmail: senderEmail,
		subject: subject,
		message: message,
		emailList: emailList,
		isHtml: isHtml
		})
		.then((response) => {
			return response.data;
		}).catch(error => handleRequestFailure(error));
}

/** Set passowrd for the first time is the same as reset password */
export async function setPassword(emailId, code, password, confirmPassword) {
    return axios.post(RESET_PASSWD_ENDPOINT, {
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
			handleRequestFailure(error);
			return {status : 500}
        });
}


/*
	Desc: Login and return a hash
	input: Email
	accept: (successMessage:string){}
	fail: (errorMessage:string){}
*/
export async function login(email, password, accept, fail) {
	let callback = accept;

	axios.post(AUTH_ENDPOINT, {
	    email: email,
	    password: password,
	},  {
		withCredentials: true
	}).then((response) => {
		if (!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
		.catch((error) => {
			handleRequestFailure(error, fail);
		});
}

// export async function getToken(email, accept, fail) {
// 	let callback = accept;
//
// 	axios.post(AUTH_ENDPOINT, {
// 		email: email,
// 	},  {
// 		withCredentials: true
// 	}).then((response) => {
// 		if (!(response.status === 200))
// 			callback = fail;
// 		statusHandle(response, callback);
// 	})
// 		.catch((error) => {
// 			handleRequestFailure(error, fail);
// 		});
// }
// alert(JSON.stringify(response));
//{"login":true,"admin":false,"email":"szheng39@buffalo.edu","compactViewEnabled":true,"saveOAuthTokens":true,"endpoint1":{"login":true,"credential":{"uuid":"9de7aca4-0ee1-4914-8af8-2d268c4b1ebe","name":"GoogleDrive: szheng39@buffalo.edu","tokenSaved":true},"uri":"googledrive:/","side":"left"},"endpoint2":{"login":false,"credential":{},"uri":"","side":"right"},"queue":[],"transferOptions":{"useTransferOptimization":"NONE","overwriteExistingFiles":true,"verifyFileInterity":false,"encryptDataChannel":false,"compressDataChannel":false}}
/* {"data":{"email":"szheng39@buffalo.edu","saveOAuthTokens":true,"compactViewEnabled":false,"expiresIn":86400,"admin":false},"status":200,"statusText":"OK","headers":{"cache-control":"no-cache, no-store, max-age=0, must-revalidate","content-length":"114","content-type":"application/json;charset=UTF-8","expires":"0","pragma":"no-cache","x-content-type-options":"nosniff","x-frame-options":"DENY","x-xss-protection":"1 ; mode=block"},"config":{"url":"/authenticate","method":"post","data":"{\"email\":\"szheng39@buffalo.edu\",\"password\":\"Iamawesome1!\"}","headers":{"Accept":"application/json","Content-Type":"application/json"},"transformRequest":[null],"transformResponse":[null],"timeout":10000,"xsrfCookieName":"XSRF-TOKEN","xsrfHeaderName":"X-XSRF-TOKEN","maxContentLength":-1},"request":{}} */
/* {"data":{"email":"szheng39@buffalo.edu","saveOAuthTokens":true,"compactViewEnabled":false,"expiresIn":86400,"admin":false},"status":200,"statusText":"OK","headers":{"cache-control":"no-cache, no-store, max-age=0, must-revalidate","content-length":"114","content-type":"application/json;charset=UTF-8","expires":"0","pragma":"no-cache","x-content-type-options":"nosniff","x-frame-options":"DENY","x-xss-protection":"1 ; mode=block"},"config":{"url":"/authenticate","method":"post","data":"{\"email\":\"*\",\"password\":\"*\"}","headers":{"Accept":"application/json","Content-Type":"application/json"},"transformRequest":[null],"transformResponse":[null],"timeout":10000,"withCredentials":true,"xsrfCookieName":"XSRF-TOKEN","xsrfHeaderName":"X-XSRF-TOKEN","maxContentLength":-1},"request":{}} */
export async function logout(){
	axios.post(LOGOUT_ENDPOINT, {})
		.then((response) => {
			console.debug(`Logout success`);
			store.dispatch(logoutAction());
		}).catch((error) => {
			console.debug(`Logout failed ${error}`)
		})
}

export async function isAdmin(email, hash, accept, fail) {
	let callback = accept;
	axios.post(url + 'user', {
		action: 'isAdmin',
		email: email,
		hash: hash,
	}).then((response) => {
		if (!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
		.catch((error) => {
			handleRequestFailure(error, fail);
		});
}

export async function history(uri, portNum, accept, fail) {
	let callback = accept;

	axios.post(url + 'user', {
		action: 'history',
		portNumber: portNum,
		uri: encodeURI(uri)
	}).then((response) => {
		if (!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
		.catch((error) => {
			handleRequestFailure(error, fail);
		});
}

export async function deleteHistory(uri, type, accept, fail) {
		let callback = accept;
		// console.log(uri + " " + type);

		axios.delete(apiCredUrl + type, {
			headers: {},
			data: uri
		})
			.then((response) => {
				if (!(response.status === 200))
					callback = fail;
				statusHandle(response, callback);
			})
			.catch((error) => {
				handleRequestFailure(error, fail);
			});

		// axios.post(apiCredUrl + 'user', {
		// 	action: "deleteHistory",
		// 	uri: isS3 ? uri : encodeURI(uri)
		// })
		// 	.then((response) => {
		// 		if (!(response.status === 200))
		// 			callback = fail;
		// 		statusHandle(response, callback);
		// 	})
		// 	.catch((error) => {
		// 		handleRequestFailure(error, fail);
		// 	});

		// axios.post(url + 'user', {
		// 	action: "deleteHistory",
		// 	uri: encodeURI(uri)
		// })
		// 	.then((response) => {
		// 		if (!(response.status === 200))
		// 			callback = fail;
		// 		statusHandle(response, callback);
		// 	})
		// 	.catch((error) => {
		// 		handleRequestFailure(error, fail);
		// 	});
		//return principalMono.map(Principal::getName)
	//                 .flatMap(user->credentialService.deleteCredential(user, type, credential.get("credential").toString()));

	//key values:
	//credential: credID

	//api/cred/s3/rm
}

export async function saveEndpointCred(type, body, accept, fail) {
	let callback = accept;
	console.log(type + "being saved to endpoint cred");
	console.log(body);
	axios.post(apiCredUrl + type.toLowerCase(), body
		).then((response) => {
			if(!(response.status === 200))
				callback = fail;
			statusHandle(response, callback);
		})
		.catch ((error) => {
			handleRequestFailure(error, fail);
		})
}

/*
	Desc: List credentials for dropbox and googledrive
*/
export async function savedCredList(type, accept, fail) {
	let callback = accept;
	axios.get(apiCredUrl + type.toLowerCase())
		.then((response) => {
			console.log(response);
			if (!(response.status === 200))
				callback = fail;
			statusHandle(response, callback);
		})
		.catch((error) => {
			handleRequestFailure(error, fail);
		});
}

/*
	Desc: Extract all transfers for the user
*/
export async function getJobsForUser(pageNo, pageSize, sortBy, order, accept, fail) {
	let callback = accept;
	axios.post(url + GET_USER_JOBS_ENDPOINT, {
		pageNo: pageNo,
		pageSize: pageSize,
		sortBy: sortBy,
		sortOrder: order
	})
		.then((response) => {
			console.log(`Get jobs response ${response}`)
			if(!(response.status === 200))
				callback = fail;
			statusHandle(response, callback);
		})
		.catch((error) => {
			handleRequestFailure(error, fail);
		});
}

/*
	Desc: Fetch all transfers. Only for Admins
*/
export async function getJobsForAdmin(owner, pageNo, pageSize, sortBy, order, accept, fail) {
	let callback = accept;
	axios.post(url+GET_ADMIN_JOBS_ENDPOINT, {
		status: 'all',
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
		handleRequestFailure(error, fail);
	});
}

export async function getSearchJobs(username, startJobId, endJobId, progress, pageNo, pageSize, sortBy, order, accept, fail) {
	let callback = accept;
	axios.post(url+GET_SEARCH_JOBS_ENDPOINT, {
		status: 'all',
		username: username,
		startJobId: startJobId,
		endJobId: endJobId,
		progress: progress,
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

export async function getJobUpdatesForUser(jobIds, accept, fail){
	let callback = accept;
	axios.post(url+GET_USER_UPDATES_ENDPOINT, jobIds)
	.then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {
		handleRequestFailure(error, fail);
    });
}


export async function getJobUpdatesForAdmin(jobIds,accept, fail){
	let callback = accept;
	axios.post(url+GET_ADMIN_UPDATES_ENDPOINT, jobIds)
	.then((response) => {
		if(!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
	.catch((error) => {
		handleRequestFailure(error, fail);
    });
}

// Service method that connects with ODS backend to submit an issue reported by the user and create a ticket.
export async function submitIssue(reqBody, success, fail) {
	let callback = success;

	axios.post(url + 'ticket', reqBody).then((resp) => {
		if (!(resp.status === 200))
			callback = fail;
		statusHandle(resp, callback)
	})
		.catch((error) => {
			handleRequestFailure(error, fail);
		});
}

export async function submit(src, srcEndpoint, dest, destEndpoint, options, accept, fail) {
	let callback = accept;
	// console.log(src)
	let src0 = Object.assign({}, src);
	let dest0 = Object.assign({}, dest);
	if (Object.keys(src0.credential).length === 0) {
		delete src0["credential"];
	}
	if (Object.keys(dest0.credential).length === 0) {
		delete dest0["credential"];
	}

	axios.post(url + 'submit', {
		src: { ...src0, type: getType(src0), map: getMapFromEndpoint(srcEndpoint) },
		dest: { ...dest0, type: getType(dest0), map: getMapFromEndpoint(destEndpoint) },
		options: options
	}).then((response) => {
		if (!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	}).catch((error) => {
		handleRequestFailure(error, fail);
	});
}

export async function upload(uri, credential, accept, fail) {
	let callback = accept;
	axios.post(url+'share', {
	    credential: credential,
	    uri: encodeURI(uri),
	    type: getTypeFromUri(uri)
	}).then((response) => {
		if (!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	})
		.catch((error) => {
			handleRequestFailure(error, fail);
		});
}

/*
	Desc: Retrieve all the available users
*/
export async function getUsers(pageNo, pageSize, sortBy, order, accept, fail) {
	let callback = accept;

	axios.post(url + GET_USERS_ENDPOINT, {
		pageNo: pageNo,
		pageSize: pageSize,
		sortBy: sortBy,
		sortOrder: order
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


/*
	Desc: Retrieve all the available users
*/
export async function getAdmins(pageNo, pageSize, sortBy, order, accept, fail) {
	let callback = accept;

	axios.post(url + GET_ADMINS_ENDPOINT, {
		pageNo: pageNo,
		pageSize: pageSize,
		sortBy: sortBy,
		sortOrder: order
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


export async function getUser(email, accept, fail) {
	let callback = accept;

	axios.post(url + 'user', {
		action: "getUser",
		email: email,
	})
		.then((response) => {
			if (!(response.status === 200))
				callback = fail;
			statusHandle(response, callback);
		})
		.catch((error) => {
			handleRequestFailure(error, fail);
		})
}

export async function updateSaveOAuth(email, saveOAuth, successCallback) {
	axios.post(url + 'user', {
		action: "updateSaveOAuth",
		email: email,
		saveOAuth: saveOAuth
	})
		.then((response) => {
			if (response.status === 200)
				successCallback();
		})
		.catch((error) => {
			handleRequestFailure(error);
			console.debug("Error encountered while updating the user.");
		});
}

/*
	Desc: Call the backend to save the OAuth Credentials when the user toggles
        the button in account preferences to save credentials
	input: Array of OAuth credentials
	accept: (successMessage:string){}
	fail: (errorMessage:string){}
*/

export async function saveOAuthCredentials(credentials, accept, fail) {
	let callback = accept;
	axios.post(url + 'cred/saveCredentials', credentials)
		.then((response) => {
			if (!(response.status === 200))
				callback = fail;
			statusHandle(response, callback);
		})
		.catch((error) => {
			handleRequestFailure(error, fail);
		});
}

export async function updateAdminRightsApiCall(email, isAdmin) {
	return axios.put(url + UPDATE_ADMIN_RIGHTS, {
		email: email,
		isAdmin: isAdmin
	})
		.then((response) => {
			if (!(response.status === 200))
				return false;
			else {
				return true;
			}
		})
		.catch((error) => {
			handleRequestFailure(error);
			console.debug("Error encountered while updating the user.");
		});
}

/*
	Desc: Change Password
*/
export async function changePassword(oldPassword, newPassword, confirmPassword, accept, fail) {
	let callback = accept;

	axios.post(UPDATE_PASSWD_ENDPOINT, {
		password: oldPassword,
		newPassword: newPassword,
		confirmPassword: confirmPassword

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

export async function cancelJob(jobID, accept, fail) {
	let callback = accept;
	fetch(url + 'cancel', {
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
			if (!response.ok)
				callback = fail;
			statusHandle(response, callback);
		})
		.catch((error) => {
			handleRequestFailure(error, fail);
		});
}

// export async function deleteCredentialFromServer(uri, accept, fail) {
// 		let callback = accept;

// 		axios.post(url + 'user', {
// 			action: "deleteCredential",
// 			uuid: uri
export async function deleteCredentialFromServer(cred, type, accept, fail) {
	let callback = accept;

	axios.patch(apiCredUrl + type.toLowerCase(), {
		credential: cred
	}).then((response) => {
			if (!(response.status === 200))
				callback = fail;
			statusHandle(response, callback);
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


export async function restartJob(jobID, accept, fail) {
	let callback = accept;
	axios.post(url + 'restart', {
		job_id: jobID
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

export async function deleteJob(jobID, accept, fail) {
	let callback = accept;
	axios.post(url + 'deleteJob', {
		job_id: jobID
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

/*
	Store user's view preference in the backend on toggle
	input: Email, viewPreference
	accept: (successMessage:string){}
	fail: (errorMessage:string){}
*/

export async function updateViewPreference(email, compactViewEnabled, accept, fail) {
	let callback = accept;
	axios.post(url + 'user', {
		action: 'updateViewPreference',
		email: email,
		compactViewEnabled: compactViewEnabled
	}).then((response) => {
		if (!(response.status === 200))
			callback = fail;
		statusHandle(response, callback);
	}).catch((error) => {
		handleRequestFailure(error, fail);
	});
}


export async function registerUser(requestBody, errorCallback) {
	return axios.post(REGISTRATION_ENDPOINT, requestBody)
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
					console.error("Error while registering user");
						errorCallback();
						return new Error({status: 500});
					}
				);
}


export async function verifyRegistraionCode(emailId, code) {
    return axios.post(EMAIL_VERIFICATION_ENDPOINT, {
    	    email : emailId,
    	    code : code
    	})
    	.then((response) => {
            return response;
    		//statusHandle(response, callback);
    	})
    	.catch((error) => {
          console.error("Error while verifying the registration code")
          return {status : 500}
        });
}
