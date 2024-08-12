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
// UI import
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import LinearProgress from "@mui/material/LinearProgress";
// Components
import NewLoginComponent from "./NewLoginComponent";
import SavedLoginComponent from "./SavedLoginComponent";
import CreateAccountComponent from "./CreateAccountComponent";
import ValidateEmailComponent from "./ValidateEmailComponent";
import ForgotPasswordComponent from "./ForgotPasswordComponent";

import { Route, Routes, Navigate } from "react-router-dom";

import { login } from "../../APICalls/APICalls.js";

import "./AccountControlComponent.css";

import {
  signInUrl,
  registerPageUrl,
  forgotPasswordUrl,
  lostValidationCodeUrl,
  siteURLS,
} from "../../constants";
import { GREY } from "../../color";
import { store } from "../../App.js";
import { loginAction } from "../../model/actions";
import { cookies } from "../../model/reducers";

export default class AccountControlComponent extends Component {
  constructor(props) {
    super(props);
    // Redux login action
    this.unsubscribe = store.subscribe(() => {
      this.setState({ authenticated: store.getState().login });
    });

    const cookieSaved = cookies.get("SavedUsers") || 0;
    const rememberMeAccounts = cookieSaved === 0 ? {} : JSON.parse(cookieSaved);
    this.newLogin = (
      <SavedLoginComponent
        accounts={rememberMeAccounts}
        login={(email) => {
          const user = JSON.parse(cookies.get("SavedUsers"))[email];
          this.userLogin(email, user.hash, false);
        }}
        removedAccount={(accounts) => {
          cookies.set("SavedUsers", JSON.stringify(accounts));
          this.setState({ loading: false, accounts: accounts, signIn: true });
        }}
        useAnotherAccount={() => {
          this.setState({ signIn: true });
        }}
        isLoading={(loading) => {
          this.setState({ loading: loading });
        }}
      />
    );

    this.state = {
      isSmall: window.innerWidth <= 640,
      password: "",
      loading: true,
      rememberMeAccounts: rememberMeAccounts,
      authenticated: store.getState().login,
      screen: this.newLogin,
      creatingAccount: false,
      loggingAccount: false,
      redirectToSignIn: false,
      // When signIn is set, it launches /account/signIn url
      // In all back function's, that are sent as props to the child components, this flag is set to true
      // and the flag for the corresponding components is set to false
      // Eg: { signIn: true, creatingAccount: false } in props of 'CreateAccountComponent' component
      // If the user clicks register on navbar, then check the route and redirect to register page. So, the signIn
      // flag should be false
      signIn: false,
      forgotPasswordPressed: false,
      lostValidationCodePressed: false,
    };
    this.getInnerCard = this.getInnerCard.bind(this);
    this.userLogin = this.userLogin.bind(this);
    this.userSigningIn = this.userSigningIn.bind(this);
  }

  componentDidMount() {
    document.body.style.backgroundColor = GREY;
    document.title = "OneDataShare - Account";
    window.addEventListener("resize", this.resize.bind(this));
    this.setState({ loading: false });
    this.resize();
  }

  static propTypes = {};

  // Called when user clicked login
  userLogin(
    email,
    token,
    remember,
    saveOAuthTokens,
    compactViewEnabled,
    admin,
    expiresIn
  ) {
    let tempRememberMeAccounts = this.state.rememberMeAccounts;
    tempRememberMeAccounts[email] = { hash: token };
    this.setState({
      rememberMeAccounts: tempRememberMeAccounts,
    });
    if (remember) {
      cookies.set("SavedUsers", JSON.stringify(this.state.rememberMeAccounts));
    }
    store.dispatch(
      loginAction(
        email,
        token,
        remember,
        saveOAuthTokens,
        compactViewEnabled,
        admin,
        expiresIn
      )
    );
  }

  componentWillUnmount() {
    this.unsubscribe();

    // Reset the body style to prevent styling conflicts
    document.body.style.backgroundColor = null;
  }

  resize() {
    if (this.state.isSmall && window.innerWidth > 640) {
      this.setState({ isSmall: false });
    } else if (!this.state.isSmall && window.innerWidth <= 640) {
      this.setState({ isSmall: true });
    }
  }

  userSigningIn(email, password, remember, fail) {
    login(
      email,
      password,
      (success) => {
        console.log("Successful login");
        this.userLogin(
          success.email,
          success.token,
          remember,
          success.saveOAuthTokens,
          success.compactViewEnabled,
          success.admin,
          success.expiresIn
        );
      },
      (error) => {
        fail(error);
      }
    );
  }

  getInnerCard() {
    return (
      <Routes>
        <Route path="/account" element={this.state.screen} />
        <Route
          path={siteURLS.registerPageUrl}
          element={
            <CreateAccountComponent
              backToSignin={() => {
                this.setState({ redirectToSignIn: true });
              }}
            />
          }
        />
        <Route
          path={siteURLS.lostValidationCodeUrl}
          element={
            <ValidateEmailComponent
              email={this.state.email}
              backToSignin={() => {
                this.setState({
                  loading: false,
                  redirectToSignIn: true,
                  lostValidationCodePressed: false,
                });
              }}
            />
          }
        />
        <Route
          path={siteURLS.forgotPasswordUrl}
          element={
            <ForgotPasswordComponent
              back={() => {
                this.props.location.pathname = siteURLS.signInPageUrl;
                this.setState({
                  loading: false,
                  redirectToSignIn: true,
                  forgotPasswordPressed: false,
                });
              }}
              email={this.state.email}
            />
          }
        />
        <Route
          path={siteURLS.signInPageUrl}
          element={
            <NewLoginComponent
              email={this.props.email}
              isLoading={(loading) => {
                this.setState({ loading: loading });
              }}
              createAccountPressed={() => {
                this.setState({
                  loading: false,
                  creatingAccount: true,
                  signIn: false,
                });
              }}
              lostValidationCodePressed={(email) => {
                this.setState({
                  loading: false,
                  lostValidationCodePressed: true,
                  signIn: false,
                  email: email,
                });
              }}
              forgotPasswordPressed={(email) => {
                this.setState({
                  loading: false,
                  signIn: false,
                  email: email,
                  forgotPasswordPressed: true,
                });
              }}
              userLoggedIn={this.userSigningIn}
            />
          }
        />
      </Routes>
    );
  }

  render() {
    const {
      isSmall,
      loading,
      creatingAccount,
      signIn,
      forgotPasswordPressed,
      lostValidationCodePressed,
      rememberMeAccounts,
      redirectToSignIn,
    } = this.state;
    const currentRoute = this.props.location.pathname;

    this.state.signIn =
      Object.keys(rememberMeAccounts).length === 0 &&
      currentRoute !== siteURLS.registerPageUrl;
    this.state.creatingAccount = false;
    this.state.lostValidationCodePressed = false;
    this.state.forgotPasswordPressed = false;
    this.state.redirectToSignIn = false;

    return (
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          width: "100%",
          paddingBottom: "5%",
        }}
      >
        <div
          className="accCardStyle"
          style={{
            width: "450px",
            alignSelf: isSmall ? "flex-start" : "center",
          }}
        >
          {currentRoute !== siteURLS.lostValidationCodeUrl &&
            lostValidationCodePressed && (
              <Navigate to={siteURLS.lostValidationCodeUrl} replace />
            )}
          {store.getState().login && (
            <Navigate to={siteURLS.transferPageUrl} replace />
          )}
          {currentRoute !== siteURLS.registerPageUrl && creatingAccount && (
            <Navigate to={siteURLS.registerPageUrl} replace />
          )}
          {currentRoute !== siteURLS.forgotPasswordUrl &&
            forgotPasswordPressed && (
              <Navigate to={siteURLS.forgotPasswordUrl} replace />
            )}
          {redirectToSignIn && <Navigate to={siteURLS.signInPageUrl} replace />}
          {currentRoute === siteURLS.accountPageUrl && signIn && (
            <Navigate
              from={siteURLS.accountPageUrl}
              to={siteURLS.signInPageUrl}
              replace
            />
          )}
          {loading && <LinearProgress />}

          <Card elevation={3}>
            <CardContent style={{ padding: "3em" }}>
              {this.getInnerCard()}
            </CardContent>
          </Card>
        </div>
      </div>
    );
  }
}
