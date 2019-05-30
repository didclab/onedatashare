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