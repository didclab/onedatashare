export const spaceBetweenStyle = {display: 'flex', justifyContent:"space-between"};

export const isLocal = (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1");

// urls
export const url = "/api/stork/";
export const transferPageUrl = "/transfer";
export const queuePageUrl = "/queue";
export const accountPageUrl = "/account";
export const registerPageUrl = "/account/register";
export const signInUrl = "/account/signIn";
export const forgotPasswordUrl = "/account/forgotPassword";
export const lostValidationCodeUrl = "/account/lostValidationCode";
export const userPageUrl = "/user";
export const userListPageUrl = "/clientsInfo";
export const historyPageUrl = "/history";
export const addAccountUrl = "/account/add";
export const dataPageUrl = "/data";
export const managementPageUrl = "/management";
export const oauthPreUrl = "/oauth/";
export const termsUrl = "/terms";
export const policyUrl = "/policy"

// module types
export const DROPBOX_TYPE = "dropbox:///";
export const GOOGLEDRIVE_TYPE = "googledrive:/";
export const FTP_TYPE = "ftp://";
export const SFTP_TYPE = "sftp://";
export const GRIDFTP_TYPE = "gsiftp://";
export const HTTP_TYPE = "http://";
export const HTTPS_TYPE = "https://";
export const SCP_TYPE = "scp://";

export const DROPBOX_NAME = "DropBox";
export const GOOGLEDRIVE_NAME = "GoogleDrive";
export const FTP_NAME = "FTP";
export const SFTP_NAME = "SFTP";
export const HTTP_NAME = "HTTP";
export const GRIDFTP_NAME = "GridFTP";
export const SCP_NAME = "SCP";

export const DROPBOX = "dropbox";
export const GOOGLEDRIVE = "googledrive";
export const FTP = "ftp";
export const SFTP = "sftp";
export const HTTP = "http";
export const GRIDFTP = "gsiftp";
export const SCP = "scp";
//side
export const sideLeft = "left";
export const sideRight = "right";

export const validPasswordLength = 6;

//images
export const fastImage = 'https://ods-static-assets.s3.us-east-2.amazonaws.com/fast.png';
export const easyImage = 'https://ods-static-assets.s3.us-east-2.amazonaws.com/easy.png';
export const eteImage = 'https://ods-static-assets.s3.us-east-2.amazonaws.com/endtoend.png';
export const precImage = 'https://ods-static-assets.s3.us-east-2.amazonaws.com/precise.png';
export const intopImage = 'https://ods-static-assets.s3.us-east-2.amazonaws.com/interoperation.png';
export const nsfImage = 'https://ods-static-assets.s3.us-east-2.amazonaws.com/NSF_Logo.png';
export const ubImage = 'https://ods-static-assets.s3.us-east-2.amazonaws.com/ub.png';

// export const bgTexture = "https://ods-static-assets.s3.us-east-2.amazonaws.com/background-texture.webp";
export const bgTexture = "https://ods-static-assets.s3.us-east-2.amazonaws.com/background-texture.png";
export const gs1 = "https://ods-static-assets.s3.us-east-2.amazonaws.com/gs1.png";
export const gs2 = "https://ods-static-assets.s3.us-east-2.amazonaws.com/gs2.png";
export const gs3 = "https://ods-static-assets.s3.us-east-2.amazonaws.com/gs3.png";
export const gs4 = "https://ods-static-assets.s3.us-east-2.amazonaws.com/gs4.png";
export const gs5 = "https://ods-static-assets.s3.us-east-2.amazonaws.com/gs5.gif";
export const gs6 = "https://ods-static-assets.s3.us-east-2.amazonaws.com/gs6.gif";
export const gs7 = "https://ods-static-assets.s3.us-east-2.amazonaws.com/gs7.png";

// RSA key for encryption
export const ODS_PUBLIC_KEY = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKL7349KYl9jec3Ee+UamFduBmwo9jZU7Ud5tOolcAhDd5gHMiNzJcooZf5vO2cm6+NvebfoYk9gJD9hA3amHrUCAwEAAQ==";

//Status
export const completeStatus = "complete";

export const showText={
	dropbox: DROPBOX_NAME,
	googledrive: GOOGLEDRIVE_NAME,
	ftp : FTP_NAME,
	sftp : SFTP_NAME,
	http : HTTP_NAME,
	gsiftp : GRIDFTP_NAME,
	scp : SCP_NAME,
	https : HTTP_NAME
}

export const showType={
	dropbox: DROPBOX_TYPE,
	googledrive: GOOGLEDRIVE_TYPE,
	ftp : FTP_TYPE,
	sftp : SFTP_TYPE,
	http : HTTP_TYPE,
	gsiftp : GRIDFTP_TYPE,
	scp : SCP_TYPE,
	https : HTTP_TYPE
}

export const defaultPort={
	dropbox: -1,
	googledrive: -1,
	ftp : 21,
	sftp : 22,
	http : 80,
	gsiftp : -1,
	scp : 22,
	https : 443
}

export const maxCookieAge = 7;


export function getType(endpoint){
	return getTypeFromUri(endpoint.uri)
}

export function getDefaultPortFromUri(uri){
	return defaultPort[uri.split(":")[0].toLowerCase()]
}

export function getTypeFromUri(uri){
	return showType[uri.split(":")[0].toLowerCase()]
}

export function getName(endpoint){
	return showText[endpoint.uri.split(":")[0].toLowerCase()]
}

export function getNameFromUri(uri){
	return showText[uri.split(":")[0].toLowerCase()]
}

export function validateField(regex, valueToEvaluate, messageToDisplay, validationArray){
	if(!(regex.test(valueToEvaluate))){
		validationArray.push({containsError : true, msg: messageToDisplay});
	}else{
		validationArray.push({containsError : false, msg: messageToDisplay});
	}
}


export  function validatePassword(password,confirmPassword)
{
	let validations = []
	validateField(/[a-z]/, password, "One lower character", validations);
	validateField(/[A-Z]/, password, "One upper character", validations);
	validateField(/[0-9]/, password, "One digit", validations);
	validateField(/\W/, password, "One special character", validations);

	if(password.length<validPasswordLength){
		validations.push({containsError : true, msg: "Minimum "+ validPasswordLength.toString() + " characters"});
	}
	else {
		validations.push({containsError : false, msg: "Minimum "+ validPasswordLength.toString() + " characters"});
	}

	if (password === confirmPassword) {
		validations.push({containsError : false, msg: "Passwords Match"});
	}else{
		validations.push({containsError : true, msg: "Passwords Match"});
	}
	
	return validations;
}
