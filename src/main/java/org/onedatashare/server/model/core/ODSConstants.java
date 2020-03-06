package org.onedatashare.server.model.core;

import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.HashSet;
import java.util.Set;

public class ODSConstants {

    public static final String DROPBOX_URI_SCHEME = "dropbox:///";
    public static final String DRIVE_URI_SCHEME = "googledrive:/";
    public static final String BOX_URI_SCHEME = "box:///";
    public static final String SFTP_URI_SCHEME = "sftp://";
    public static final String FTP_URI_SCHEME = "ftp://";
    public static final String SCP_URI_SCHEME = "scp://";
    public static final String GRIDFTP_URI_SCHEME = "gsiftp://";
    public static final String HTTP_URI_SCHEME = "http://";
    public static final String HTTPS_URI_SCHEME = "https://";

    public static final String DROPBOX_CLIENT_IDENTIFIER = "OneDataShare-DIDCLab";
    public static final String UPLOAD_IDENTIFIER = "Upload";

    public static final String COOKIE = "cookie";
    public static final String TOKEN_COOKIE_NAME = "ATOKEN";

    public static final String[] ODS_URIS_ARR = new String[]{"/", "/transfer", "/terms", "/policy", "/user", "/queue",
            "/clientsInfo", "/history", "/account", "/account/signIn", "/account/register", "/oauth",
            "/support", "/get-started", "/oauth/uuid",
            "/oauth/googledrive", "/oauth/dropbox", "/oauth/gridftp", "/oauth/box",
            "/oauth/ExistingCredDropbox" ,"/oauth/ExistingCredGoogleDrive", "/oauth/ExistingCredBox"};

    public static final Set<String> ODS_URIS_SET = new HashSet<String>(Arrays.asList(ODS_URIS_ARR));

    public static final String AUTH_ENDPOINT = "/authenticate";
    public static final String LOGOUT_ENDPOINT = "/deauthenticate";
    public static final String RESET_PASSWD_ENDPOINT = "/reset-password";
    public static final String UPDATE_PASSWD_ENDPOINT = "/api/stork/update-password";
    public static final String IS_REGISTERED_EMAIL_ENDPOINT = "/is-email-registered";
    public static final String SEND_PASSWD_RST_CODE_ENDPOINT = "/send-passwd-rst-code";

    public static final String REGISTRATION_ENDPOINT = "/register";
    public static final String EMAIL_VERIFICATION_ENDPOINT = "/verify-email";
    public static final String RESEND_ACC_ACT_CODE_ENDPOINT = "/resend-acc-act-code";

    public static final String[] OPEN_ENDPOINTS = new String[]{
            AUTH_ENDPOINT, RESET_PASSWD_ENDPOINT, REGISTRATION_ENDPOINT, EMAIL_VERIFICATION_ENDPOINT, RESEND_ACC_ACT_CODE_ENDPOINT
    };

    public static final int TOKEN_TIMEOUT_IN_MINUTES = 3;

    public static final long TRANSFER_SLICE_SIZE = 1<<20;

    public static final long JWT_TOKEN_EXPIRES_IN = 28800;

}