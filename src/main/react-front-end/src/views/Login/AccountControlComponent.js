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
// ui import
import Card from "@material-ui/core/Card";
import CardContent from "@material-ui/core/CardContent";
import LinearProgress from "@material-ui/core/LinearProgress";
// components
import NewLoginComponent from "./NewLoginComponent";
import SavedLoginComponent from "./SavedLoginComponent";
import CreateAccountComponent from "./CreateAccountComponent";
import ValidateEmailComponent from "./ValidateEmailComponent";
import ForgotPasswordComponent from "./ForgotPasswordComponent";

import { Route, Switch, Redirect } from "react-router-dom";

import { login } from "../../APICalls/APICalls.js";

import "./AccountControlComponent.css";

import {
  transferPageUrl,
  signInUrl,
  registerPageUrl,
  forgotPasswordUrl,
  lostValidationCodeUrl,
  accountPageUrl,
} from "../../constants";
import { GREY } from "../../color";
import { store } from "../../App.js";
import { loginAction } from "../../model/actions";
import { cookies } from "../../model/reducers";

export default class AccountControlComponent extends Component {
  constructor(props) {
    super(props);
    // redux login action
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
  componentWillMount() {
    //Change body background color to grey
    //Doing this separately to prevent styling conflicts
    document.body.style.backgroundColor = GREY;
  }

  componentDidMount() {
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
    //this.setState({authenticated : true});
  }

  componentWillUnmount() {
    this.unsubscribe();

    //Reset the body style to prevent styling conflicts
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
        console.log("SuccessFull login");
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
  // Switches to a route and renders a component based on the redirect set inside render method.
  getInnerCard() {
    return (
      <Switch>
        <Route
          exact
          path={"/account"}
          render={(props) => this.state.screen}
        ></Route>

        <Route
          exact
          path={registerPageUrl}
          render={(props) => (
            <CreateAccountComponent
              {...props}
              backToSignin={() => {
                this.setState({ redirectToSignIn: true });
              }}
            />
          )}
        ></Route>

        <Route
          exact
          path={lostValidationCodeUrl}
          render={(props) => (
            <ValidateEmailComponent
              {...props}
              email={this.state.email}
              backToSignin={() => {
                this.setState({
                  loading: false,
                  redirectToSignIn: true,
                  lostValidationCodePressed: false,
                });
              }}
            />
          )}
        ></Route>

        <Route
          exact
          path={forgotPasswordUrl}
          render={(props) => (
            <ForgotPasswordComponent
              {...props}
              back={() => {
                this.props.location.pathname = signInUrl;
                this.setState({
                  loading: false,
                  redirectToSignIn: true,
                  forgotPasswordPressed: false,
                });
              }}
              email={this.state.email}
            />
          )}
        ></Route>

        <Route
          exact
          path={signInUrl}
          render={(props) => (
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
          )}
        ></Route>
      </Switch>
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
    this.setState.signIn =
      Object.keys(rememberMeAccounts).length === 0 &&
      currentRoute !== registerPageUrl;
    this.setState.creatingAccount = false;
    this.setState.lostValidationCodePressed = false;
    this.setStateforgotPasswordPressed = false;
    this.setState.redirectToSignIn = false;
    console.log(this.setState);

    return (
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          width: "..",
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
          {/* { console.log(store.getState().login + "-" + forgotPasswordPressed  + "-" + creatingAccount +"-"+lostValidationCodePressed+ "-" + signIn + "-" + Object.keys(rememberMeAccounts).length )}
						{console.log(currentRoute)} */}
          {/* At any point of time only one among below should be true */}
          {currentRoute !== lostValidationCodeUrl &&
            lostValidationCodePressed && (
              <Redirect to={lostValidationCodeUrl} />
            )}
          {store.getState().login && <Redirect to={transferPageUrl} />}
          {currentRoute !== registerPageUrl && creatingAccount && (
            <Redirect to={registerPageUrl} />
          )}
          {currentRoute !== forgotPasswordUrl && forgotPasswordPressed && (
            <Redirect to={forgotPasswordUrl} />
          )}
          {redirectToSignIn && <Redirect to={signInUrl} />}
          {currentRoute === accountPageUrl && signIn && (
            <Redirect from={accountPageUrl} to={signInUrl} />
          )}
          {loading && <LinearProgress />}

          {isSmall && this.getInnerCard()}
          {!isSmall && (
            <Card elevation="3">
              <CardContent style={{ padding: "3em" }}>
                {this.getInnerCard()}
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    );
  }
}
