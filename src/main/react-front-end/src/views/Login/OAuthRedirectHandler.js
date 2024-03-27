import React, { Component } from "react";
import { Redirect } from "react-router-dom";
import { signInUrl } from "../../constants.js";
class OAuth2RedirectHandler extends Component {
  getUrlParameter(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)");
    var results = regex.exec(this.props.location.search);
    return results === null
      ? ""
      : decodeURIComponent(results[1].replace(/\+/g, " "));
  }

  render() {
    const token = this.getUrlParameter("ATOKEN");
    const email = this.getUrlParameter("email");
    const error = this.getUrlParameter("error");
    if (token && email) {
      localStorage.setItem("token", token);
      this.props.oauthUserSigningIn(email, token, true);
    } else {
      if (error) {
        console.error("OAuth2 error:", error);
      } else {
        console.error("OAuth2 error: Unknown error occurred");
      }
      <Redirect
        to={{
          pathname: signInUrl,
        }}
      />;
    }
  }
}

export default OAuth2RedirectHandler;
