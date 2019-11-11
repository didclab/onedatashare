package org.onedatashare.server.model.core;

import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.HashSet;
import java.util.Set;

public class ODSConstants {

    public static final String DROPBOX_URI_SCHEME = "dropbox:///";
    public static final String DRIVE_URI_SCHEME = "googledrive:/";
    public static final String SFTP_URI_SCHEME = "sftp://";
    public static final String FTP_URI_SCHEME = "ftp://";
    public static final String SCP_URI_SCHEME = "scp://";
    public static final String GRIDFTP_URI_SCHEME = "gsiftp://";
    public static final String HTTP_URI_SCHEME = "http://";
    public static final String HTTPS_URI_SCHEME = "https://";

    public static final String DROPBOX_CLIENT_IDENTIFIER = "OneDataShare-DIDCLab";
    public static final String UPLOAD_IDENTIFIER = "Upload";

    public static final String COOKIE = "cookie";

    public static final String[] ODS_URIS_ARR = new String[]{"/", "/transfer", "/terms", "/policy", "/user", "/queue",
            "/clientsInfo", "/history", "/account", "/account/signIn", "/account/register", "/oauth",
            "/support", "/get-started", "/oauth/uuid",
            "/oauth/googledrive", "/oauth/dropbox", "/oauth/gridftp", "/oauth/box", "/oauth/ExistingCredDropbox" ,"/oauth/ExistingCredGoogleDrive", "/oauth/ExistingCredBox"};

    public static final Set<String> ODS_URIS_SET = new HashSet<String>(Arrays.asList(ODS_URIS_ARR));

    public static final long TRANSFER_SLICE_SIZE = 1<<20;

}