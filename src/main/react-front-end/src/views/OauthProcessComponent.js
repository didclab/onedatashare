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


import React, { Component } from "react";

import {
  DROPBOX_TYPE,
  sideLeft,
  DROPBOX_NAME,
  GOOGLEDRIVE_NAME,
  BOX_NAME,
  siteURLS,
} from "../constants";
import { eventEmitter } from "../App";
import { endpointLogin } from "../model/actions";
import { cookies } from "../model/reducers";
import Redirect from "react-router/es/Redirect";

export default class OauthProcessComponent extends Component {
  constructor(props) {
    super(props);
    const { tag } = this.props.match.params;
    this.processOAuth(tag);
  }

  processOAuth(tag) {
    if (tag === "ExistingCredGoogleDrive") {
      setTimeout(() => {
        eventEmitter.emit(
          "errorOccured",
          "Credential for the endpoint already Exists. Please logout from Google Drive and try again."
        );
      }, 500);
    } else if (tag === "ExistingCredDropbox") {
      setTimeout(() => {
        eventEmitter.emit(
          "errorOccurred",
          "Credential for that endpoint already exists. Please logout from Dropbox and try again."
        );
      }, 500);
    } else if (tag === "ExistingCredBox") {
      setTimeout(() => {
        eventEmitter.emit(
          "errorOccured",
          "Credential for the endpoint already Exists. Please logout from Box and try again."
        );
      }, 500);
    } else if (tag === "uuid") {
      console.log(
        "User has opted to save auth tokens at ODS servers. UUID received"
      );
      let qs = this.props.location.search;
      let identifier = decodeURIComponent(qs.substring(qs.indexOf("=") + 1));
      endpointLogin(DROPBOX_TYPE, sideLeft, { uuid: identifier });
    } /*else if(isOAuth.hasOwnProperty(tag) && isOAuth[tag]){
      console.log(tag + " oAuth identifier received");
      this.updateLocalCredStore(showText[tag], qsObj);
    }*/
    else {
      let qs = this.props.location.search;
      let qsObj = JSON.parse(
        decodeURIComponent(qs.substring(qs.indexOf("=") + 1))
      );

      // if(isOAuth.hasOwnProperty(tag) && isOAuth[tag]){
      //   console.log(tag + "oAuth identifier received");
      //   this.updateLocalCredStore(showType[tag], qsObj);
      // }


      if (tag === "dropbox") {
        console.log("Dropbox oAuth identifier received");
        this.updateLocalCredStore(DROPBOX_NAME, qsObj);
      } else if (tag === "googledrive") {
        console.log("Google drive oAuth identifier received");
        this.updateLocalCredStore(GOOGLEDRIVE_NAME, qsObj);
      } else if (tag === "box") {
        console.log("Box oAuth identifier received");
        this.updateLocalCredStore(BOX_NAME, qsObj);
      }
    }
  }

  updateLocalCredStore(protocolType, qsObj) {
    let creds = cookies.get(protocolType) || 0;
    if (creds !== 0) {
      let parsedJSON = JSON.parse(creds);
      let accountId = qsObj.name.split(":+")[1];
      let oAuthToken = qsObj.token;

      let existingToken = parsedJSON.some(obj => obj.name === accountId);
      if (existingToken) {
        console.log(
          "Auth token for " + accountId + " already exists in session."
        );
      } else {
        parsedJSON.push({ name: accountId, token: oAuthToken, refreshToken: qsObj.refreshToken, expiredTime: qsObj.expiredTime });
        cookies.set(protocolType, JSON.stringify(parsedJSON));
      }
    } else {
      cookies.set(
        protocolType,
        JSON.stringify([
          { name: qsObj.name.split(":+")[1], token: qsObj.token, refreshToken: qsObj.refreshToken, expiredTime: qsObj.expiredTime }
        ])
      );
    }
  }


  render() {
    return (
      <div>
        <Redirect to={siteURLS.transferPageUrl} />
        <h1>Wait a second, You will be redirected.</h1>
      </div>
    );
  }
}
