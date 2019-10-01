import React, { Component } from "react";

import { Redirect } from "react-router-dom";
import {
  transferPageUrl,
  DROPBOX_TYPE,
  sideLeft,
  DROPBOX_NAME,
  GOOGLEDRIVE_NAME,
  GRIDFTP_NAME
} from "../constants";
import { eventEmitter } from "../App";
import { endpointLogin } from "../model/actions";
import { cookies } from "../model/reducers";

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
          "errorOccured",
          "Credential for the endpoint already Exists. Please logout from Dropbox and try again."
        );
      }, 500);
    } else if (tag === "uuid") {
      console.log(
        "User has opted to save auth tokens at ODS servers. UUID received"
      );
      let qs = this.props.location.search;
      let identifier = decodeURIComponent(qs.substring(qs.indexOf("=") + 1));
      endpointLogin(DROPBOX_TYPE, sideLeft, { uuid: identifier });
    } else {
      let qs = this.props.location.search;
      let qsObj = JSON.parse(
        decodeURIComponent(qs.substring(qs.indexOf("=") + 1))
      );

      if (tag === "dropbox") {
        console.log("Dropbox oAuth identifier received");
        this.updateLocalCredStore(DROPBOX_NAME, qsObj);
      } else if (tag === "googledrive") {
        console.log("Google drive oAuth identifier received");
        this.updateLocalCredStore(GOOGLEDRIVE_NAME, qsObj);
      } else if (tag === "gridftp") {
        console.log("GridFTP oAuth identifier received");
        this.updateLocalCredStore(GRIDFTP_NAME, qsObj);
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
        parsedJSON.push({ name: accountId, token: oAuthToken });
        cookies.set(protocolType, JSON.stringify(parsedJSON));
      }
    } else {
      cookies.set(
        protocolType,
        JSON.stringify([
          { name: qsObj.name.split(":+")[1], token: qsObj.token }
        ])
      );
    }
  }

  render() {
    return (
      <div>
        <Redirect to={transferPageUrl}></Redirect>
        <h1>Wait a second, You will be redirected.</h1>
      </div>
    );
  }
}
