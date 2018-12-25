import { url } from '../constants';
import {logoutAction} from "../model/actions.js";
import {store} from "../App.js";
import Axios from "axios";

const FETCH_TIMEOUT = 10000;

const axios = Axios.create({
  timeout: FETCH_TIMEOUT,
  headers: {
  	Accept: 'application/json',
	'Content-Type': 'application/json'
  }
});

function statusHandle(response, callback){
	console.log(response)
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

		if(response.name == "PermissionDenied" && store.getState().login){
			if (window.confirm('You have been logged out. Login again?'))
			{
				store.dispatch(logoutAction());
			}
		}
		if (response.status === 408 || response.code === 'ECONNABORTED') {
	      callback(`Timeout 10000ms`)
	      return;
	    }
		console.log(response)
		const errorText = JSON.stringify(response.response.data);
		callback(`500${response.response.statusText} ${errorText}`);
	}
}

/*
	Desc: Check if current email is a user
	input: Email
	accept: (successMessage:string){}
	fail: (errorMessage:string){}
*/

export async function checkLogin(email, accept, fail){
	var callback = accept;

	axios.post(url+'user', {
	    action: 'verifyUser',
	    email: email
	}).then((response) => {
		console.log("login response", response)
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

export async function history(uri, accept, fail){
	var callback = accept;

	axios.post(url+'user', {
	    action: 'history',
	    uri: uri
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
	Desc: List credentials for dropbox and googledrive
*/
export async function dropboxCredList(accept, fail){
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

export async function submit(src, dest, options,accept, fail){
	var callback = accept;
	var src0 = Object.assign({}, src);
	var dest0 = Object.assign({}, dest);
	if(Object.keys( src0.credential ).length == 0){

		delete src0["credential"];
	}
	if(Object.keys( dest0.credential ).length == 0){
		delete dest0["credential"];
	}

	axios.post(url+'submit', {
	    src: src0 ,
	    dest: dest0 ,
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

export async function listFiles(uri, credential, accept, fail){
	var body = JSON.stringify({
	    uri: uri,
	    depth: 1
	  });
	if(Object.keys(credential).length > 0){
	  body = JSON.stringify({
	    uri: uri,
	    credential: credential,
	    depth: 1
	  })
	}

	var callback = accept;

	axios.post(url+'ls', body)
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
export async function queue(accept, fail){
	var callback = accept;

	axios.post(url+'q', {
	    status: 'all'
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

export async function share(uri, credential, accept, fail){
	var callback = accept;

	axios.post(url+'share', {
	    credential: credential,
	    uri: uri
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

export async function mkdir(uri, credential, accept, fail){
	var callback = accept;
	
	axios.post(url+'mkdir', {
	    credential: credential,
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

export async function deleteCall(uri, credential, accept, fail){
	var callback = accept;

	axios.post(url+'delete', {
	    credential: credential,
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


export async function download(uri, credential){
	console.log(uri)
	var form = document.createElement('form');
	form.action = url+"get";
	form.method = 'POST';
	form.target = '_blank';

	var input = document.createElement('textarea');
	input.name = '$json';
	input.value = JSON.stringify({uri: encodeURI(uri), credential: credential});
	form.appendChild(input);

	form.style.display = 'none';
	document.body.appendChild(form);
	form.submit();

}

export async function upload(uri, credential, accept, fail){
	var callback = accept;

	axios.post(url+'share', {
	    credential: credential,
	    uri: uri
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
export async function getUsers(type, accept, fail){
	var callback = accept;

	axios.post(url+'user', {
	    action: "getUsers"
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
	Desc: Change Password
*/
export async function changePassword(oldPassword, newPassword, accept, fail){
	var callback = accept;

	axios.post(url+'user', {
		action: "password",
	    oldPassword: oldPassword, 
	    newPassword: newPassword
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
export async function deleteCredential(uri, accept, fail){
	var callback = accept;

	axios.post(url+'user', {
		action: "deleteCredential",
	    deleteUri: uri
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

export async function openDropboxOAuth(){
	openOAuth("/api/stork/oauth?type=dropbox");
}

export async function openGoogleDriveOAuth(){
	openOAuth("/api/stork/oauth?type=googledrive");
}

export async function openOAuth(url){
	window.open(url, 'oAuthWindow');
}
