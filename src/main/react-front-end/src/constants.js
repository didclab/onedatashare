export const spaceBetweenStyle = {display: 'flex', justifyContent:"space-between"};

export const isLocal = (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1");

// urls
export const url = "/api/stork/";
export const transferPageUrl = "/transfer";
export const queuePageUrl = "/queue";
export const accountPageUrl = "/account";
export const registerPageUrl = "/account/register";
export const userPageUrl = "/user";
export const userListPageUrl = "/clientsInfo";
export const historyPageUrl = "/history";
export const addAccountUrl = "/account/add";
export const dataPageUrl = "/data";
export const managementPageUrl = "/management";
export const oauthPreUrl = "/oauth/";

// module types
export const DROPBOX_TYPE = "dropbox:///";
export const GOOGLEDRIVE_TYPE = "googledrive:/";
export const FTP_TYPE = "ftp://";
export const SFTP_TYPE = "sftp://";
export const GRIDFTP_TYPE = "gsiftp://";
export const HTTP_TYPE = "http://";
export const SCP_TYPE = "scp://";


//side
export const sideLeft = "left";
export const sideRight = "right";

export const showText={
	dropbox: "DropBox",
	googledrive: "GoogleDrive",
	ftp : "FTP",
	sftp : "SFTP",
	http : "HTTP",
	gsiftp : "GridFTP",
	scp : "SCP"
}

export const showType={
	dropbox: DROPBOX_TYPE,
	googledrive: GOOGLEDRIVE_TYPE,
	ftp : FTP_TYPE,
	sftp : SFTP_TYPE,
	http : HTTP_TYPE,
	gsiftp : GRIDFTP_TYPE,
	scp : SCP_TYPE
}

export function getType(endpoint){
	return showType[endpoint.uri.split(":")[0].toLowerCase()]
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