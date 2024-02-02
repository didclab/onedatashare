/**
 * ##**************************************************************
 * ##
 * ## Copyright (C) 2018-2020, OneDataShare Team,
 * ## Department of Computer Science and Engineering,
 * ## University at Buffalo, Buffalo, NY, 14260.
 * ##
 * ## Licensed under the Apache License, Version 2.0 (the "License"); you
 * ## may not use this file except in compliance with the License.  You may
 * ## obtain a copy of the License at
 * ##
 * ##    http://www.apache.org/licenses/LICENSE-2.0
 * ##
 * ## Unless required by applicable law or agreed to in writing, software
 * ## distributed under the License is distributed on an "AS IS" BASIS,
 * ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * ## See the License for the specific language governing permissions and
 * ## limitations under the License.
 * ##
 * ##**************************************************************
 */


package org.onedatashare.server.model.core;

import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.HashSet;
import java.util.Set;

public class ODSConstants {

    public static final String DROPBOX_URI_SCHEME = "dropbox://";
    public static final String GDRIVE_URI_SCHEME = "googledrive:/";
    public static final String TOKEN_COOKIE_NAME = "ATOKEN";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String[] ODS_URIS_ARR = new String[]{"/", "/transfer", "/terms", "/policy", "/user", "/queue",
            "/clientsInfo", "/history", "/account", "/account/signIn", "/account/register", "/oauth",
            "/support", "/get-started", "/oauth/uuid",
            "/oauth/googledrive", "/oauth/dropbox", "/oauth/gridftp", "/oauth/box",
            "/oauth/ExistingCredDropbox", "/oauth/ExistingCredGoogleDrive", "/oauth/ExistingCredBox"};
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
    public static final int TOKEN_TIMEOUT_IN_MINUTES = 3;
    public static final long JWT_TOKEN_EXPIRES_IN = 86400 * 100;
    public static final int MAX_FILES_TRANSFERRABLE = 1000;
}