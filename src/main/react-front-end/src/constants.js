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


export const spaceBetweenStyle = { display: 'flex', justifyContent: "space-between" };

export const isLocal = (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1");

// urls
export const ENDPOINT_OP_URL = "/api"
export const LIST_OP_URL = "/ls"
export const DEL_OP_URL = "/rm"
export const MKDIR_OP_URL = "/mkdir"
export const DOWNLOAD_OP_URL = "/download"
export const UPLOAD_OP_URL = "/upload"
export const SHARE_OP_URL = "/share"
export const SFTP_DOWNLOAD_URL = "download/file"
export const OAUTH_URL = "download/file"
export const LOGOUT_ENDPOINT = "/deauthenticate";

export const url = "/api/stork/";
export const transferPageUrl = "/transfer";
export const queuePageUrl = "/queue";
export const endpoint_db = "/endpoint_db"
export const accountPageUrl = "/account";
export const registerPageUrl = "/account/register";
export const signInUrl = "/account/signIn";
export const forgotPasswordUrl = "/account/forgotPassword";
export const lostValidationCodeUrl = "/account/lostValidationCode";
export const userPageUrl = "/user";
export const userListPageUrl = "/clientsInfo";
export const historyPageUrl = "/history";
export const notificationPageUrl = "/sendNotifications";
export const newNotifications = "/newNotifications";
export const addAccountUrl = "/account/add";
export const dataPageUrl = "/data";
export const managementPageUrl = "/management";
export const oauthPreUrl = "/oauth/";
export const termsUrl = "/terms";
export const policyUrl = "/policy";

export const AUTH_ENDPOINT = "/authenticate";
export const RESET_PASSWD_ENDPOINT = "/reset-password";
export const IS_REGISTERED_EMAIL_ENDPOINT = "/is-email-registered";
export const SEND_PASSWD_RST_CODE_ENDPOINT = "/send-passwd-rst-code";
export const UPDATE_PASSWD_ENDPOINT = "/api/stork/update-password";

export const REGISTRATION_ENDPOINT = "/register";
export const EMAIL_VERIFICATION_ENDPOINT = "/verify-email";
export const RESEND_ACC_ACT_CODE_ENDPOINT = "/resend-acc-act-code";

export const GET_USER_JOBS_ENDPOINT = "q/user-jobs";
export const GET_ADMIN_JOBS_ENDPOINT = "q/admin-jobs";
export const GET_USER_UPDATES_ENDPOINT = "q/update-user-jobs";
export const GET_ADMIN_UPDATES_ENDPOINT = "q/update-admin-jobs";

export const GET_SEARCH_JOBS_ENDPOINT = "q/search-jobs";

export const GET_ADMINS_ENDPOINT = "admin/get-admins";
export const GET_USERS_ENDPOINT = "admin/get-users";
export const UPDATE_ADMIN_RIGHTS = "admin/change-role";

// module types
export const DROPBOX_TYPE = "dropbox:///";
export const GOOGLEDRIVE_TYPE = "googledrive:/";
export const BOX_TYPE = "box:///";
export const FTP_TYPE = "ftp://";
export const SFTP_TYPE = "sftp://";
export const GRIDFTP_TYPE = "gsiftp://";
export const HTTP_TYPE = "http://";
export const HTTPS_TYPE = "https://";

export const DROPBOX_NAME = "DropBox";
export const GOOGLEDRIVE_NAME = "GoogleDrive";
export const BOX_NAME = "Box";
export const FTP_NAME = "FTP";
export const SFTP_NAME = "SFTP";
export const HTTP_NAME = "HTTP";
export const GRIDFTP_NAME = "GridFTP";

export const DROPBOX = "dropbox";
export const GOOGLEDRIVE = "googledrive";
export const BOX = "box";
export const FTP = "ftp";
export const SFTP = "sftp";
export const HTTP = "http";
export const GRIDFTP = "gsiftp";

//side
export const sideLeft = "left";
export const sideRight = "right";

export const validPasswordLength = 6;

export const ODS_S3_BUCKET = "https://ods-static-assets.s3.us-east-2.amazonaws.com/";

//images
// export const fastImage = ODS_S3_BUCKET + 'fast.png';
// export const easyImage = ODS_S3_BUCKET + 'easy.png';
// export const eteImage = ODS_S3_BUCKET + 'endtoend.png';
// export const precImage = ODS_S3_BUCKET + 'precise.png';
// export const intopImage = ODS_S3_BUCKET + 'interoperation.png';
export const nsfImage = ODS_S3_BUCKET + 'NSF_Logo.png';
export const ubImage = ODS_S3_BUCKET + 'ub.png';
export const gs1 = ODS_S3_BUCKET + "gs1.png";
export const gs2 = ODS_S3_BUCKET + "gs2.png";
export const gs3 = ODS_S3_BUCKET + "gs3.png";
export const gs4 = ODS_S3_BUCKET + "gs4.png";
export const gs5 = ODS_S3_BUCKET + "gs5.gif";
export const gs6 = ODS_S3_BUCKET + "gs6.gif";
export const gs7 = ODS_S3_BUCKET + "gs7.png";

// RSA key for encryption
export const ODS_PUBLIC_KEY = process.env.REACT_APP_ODS_RSA_PUBLIC_KEY;

//Status
export const completeStatus = "complete";

export const showText = {
	dropbox: DROPBOX_NAME,
	googledrive: GOOGLEDRIVE_NAME,
	box: BOX_NAME,
	ftp: FTP_NAME,
	sftp: SFTP_NAME,
	http: HTTP_NAME,
	gsiftp: GRIDFTP_NAME,
	https: HTTP_NAME
}

export const showType = {
	dropbox: DROPBOX_TYPE,
	googledrive: GOOGLEDRIVE_TYPE,
	box: BOX_TYPE,
	ftp: FTP_TYPE,
	sftp: SFTP_TYPE,
	http: HTTP_TYPE,
	gsiftp: GRIDFTP_TYPE,
	https: HTTP_TYPE
}

export const SERVICES = {
	OAuth: [
		[DROPBOX_TYPE, DROPBOX_NAME, DROPBOX],
		[GOOGLEDRIVE_TYPE, GOOGLEDRIVE_NAME, GOOGLEDRIVE],
		[BOX_TYPE, BOX_NAME, BOX],
		[GRIDFTP_TYPE, GRIDFTP_NAME, GRIDFTP],
	],
	Login: [
		[FTP_TYPE, FTP_NAME, FTP],
		[HTTP_TYPE, HTTP_NAME, HTTP],
		[SFTP_TYPE, SFTP_NAME, SFTP],
	]
};

export const defaultPort = {
	dropbox: -1,
	googledrive: -1,
	ftp: 21,
	sftp: 22,
	http: 80,
	gsiftp: -1,
	https: 443
}

//Seconds for which the cookie is valid
export const maxCookieAge = 3600;

export const jobStatus = {
	COMPLETED: 'completed',
	TRANSFERRING: 'transferring',
	SCHEDULED: 'scheduled',
	FAILED: 'failed'
}


//screen sizes for mobile/desktop switch

//using grid column
export const gridFullWidth = 12;
export const gridHalfWidth = 6;
export const gridThirdWidth = 4;
export const gridQuarterWidth = 3;

//grid column/row
export const gridRow = "row";
export const gridCol = "column";



//using width sizing
export const FullWidth = 1;
export const HalfWidth = 1/2;
export const ThirdWidth = 1/3;
export const QuarterWidth = 1/4;



export function getType(endpoint) {
	return getTypeFromUri(endpoint.uri)
}

export function getDefaultPortFromUri(uri) {
	return defaultPort[uri.split(":")[0].toLowerCase()]
}

export function getTypeFromUri(uri) {
	return showType[uri.split(":")[0].toLowerCase()]
}

export function getName(endpoint) {
	return showText[endpoint.uri.split(":")[0].toLowerCase()]
}

export function getNameFromUri(uri) {
	return showText[uri.split(":")[0].toLowerCase()]
}

export function validateField(regex, valueToEvaluate, messageToDisplay, validationArray) {
	if (!(regex.test(valueToEvaluate))) {
		validationArray.push({ containsError: true, msg: messageToDisplay });
	} else {
		validationArray.push({ containsError: false, msg: messageToDisplay });
	}
}

export function validPassword(name, password, confirmPassword) {
	let isValid = true;
	if (name === 'newPassword') {
		let errormsg = "Your password must contain ";
		if (isNotValid(/[a-z]/, password)) {
			errormsg += 'one Lower case letter';
			isValid = false;
		}
		if (isNotValid(/[A-Z]/, password)) {
			errormsg += isValid ? 'one Upper case letter' : ', one Upper case letter';
			isValid = false;
		}
		if (isNotValid(/[0-9]/, password)) {
			errormsg += isValid ? 'one Digit' : ', one Digit';
			isValid = false;
		}
		if (isNotValid(/\W/, password)) {
			errormsg += isValid ? 'one Special character' : ', one Special character';
			isValid = false;
		}
		if (password.length < validPasswordLength) {
			errormsg += isValid ? `Minimum ${validPasswordLength.toString()} characters.` : `, Minimum ${validPasswordLength.toString()} characters.`;
			isValid = false;
		}
		return isValid ? { isValid: true, errormsg: null } : { isValid: false, errormsg: errormsg };
	} else {
		return password === confirmPassword ? { isValid: true, errormsg: null } : { isValid: false, errormsg: "Passwords should match" };
	}
}

export function isNotValid(regex, valueToEvaluate, messageToDisplay) {
	if (!(regex.test(valueToEvaluate))) {
		return true;
	} else {
		return false;
	}
}


export function validatePassword(password, confirmPassword) {
	let validations = []
	validateField(/[a-z]/, password, "One lower character", validations);
	validateField(/[A-Z]/, password, "One upper character", validations);
	validateField(/[0-9]/, password, "One digit", validations);
	validateField(/\W/, password, "One special character", validations);

	if (password.length < validPasswordLength) {
		validations.push({ containsError: true, msg: "Minimum " + validPasswordLength.toString() + " characters" });
	}
	else {
		validations.push({ containsError: false, msg: "Minimum " + validPasswordLength.toString() + " characters" });
	}

	if (password === confirmPassword) {
		validations.push({ containsError: false, msg: "Passwords Match" });
	} else {
		validations.push({ containsError: true, msg: "Passwords Match" });
	}

	return validations;
}

export function generateURLFromPortNumber(url, portNum) {
	// Adding Port number to the URL to ensure that the backend remembers the endpoint URL
	let finalUrl = url;

	// Find if the port is a standard port
	let standardPort = portNum === getDefaultPortFromUri(url);

	//Special condition for HTTP
	if(getTypeFromUri(url) === HTTP_TYPE){
		standardPort = portNum === getDefaultPortFromUri(HTTP_TYPE) || portNum === getDefaultPortFromUri(HTTPS_TYPE);
	}


	// If the Url already doesn't contain the portnumber and portNumber isn't standard it else no change
	if(!standardPort){
		try{
			let temp = new URL(url);
			temp.port = portNum;
			finalUrl = temp.toString();
		} catch(e){
			//Do nothing when URL is invalid
		}
	}

	return finalUrl;
}