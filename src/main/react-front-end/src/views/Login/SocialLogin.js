import React, { Component } from "react";
import {
  GOOGLE_AUTH_URL,
  CILOGON_AUTH_URL,
  GITHUB_AUTH_URL,
} from "../../constants.js";
import googleLogo from "./images/google-logo.png";
import githubLogo from "./images/github-logo.png";
import cilogonLogo from "./images/cilogon-logo.png";
import "./SocialLogin.css";

export default class SocialLogin extends Component {
  render() {
    return (
      <div className="social-login">
        <a
          className="btn-social btn-block social-btn google"
          href={GOOGLE_AUTH_URL}
        >
          <img src={googleLogo} alt="Google" /> Log in with Google
        </a>
        <a
          className="btn-social btn-block social-btn github"
          href={GITHUB_AUTH_URL}
        >
          <img src={githubLogo} alt="Github" /> Log in with Github
        </a>
        <a
          className="btn-social btn-block social-btn cilogon"
          href={CILOGON_AUTH_URL}
        >
          <img src={cilogonLogo} alt="Cilogon" /> Log in with Cilogon
        </a>
      </div>
    );
  }
}
