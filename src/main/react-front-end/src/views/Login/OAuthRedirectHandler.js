import React, { Component } from "react";
import { siteURLS } from "../../constants.js";
import { Redirect } from "react-router-dom";
import { store } from "../../App.js";
import { loginAction } from "../../model/actions";
import { cookies } from "../../model/reducers";
import SavedLoginComponent from "./SavedLoginComponent.js";

class OAuth2RedirectHandler extends Component {
  constructor(props) {
    super(props);
    this.unsubscribe = store.subscribe(() => {
      this.setState({ authenticated: store.getState().login });
    });

    const cookieSaved = cookies.get("SavedUsers") || 0;
    const rememberMeAccounts = cookieSaved === 0 ? {} : cookieSaved;
    this.newLogin = (
      <SavedLoginComponent
        accounts={rememberMeAccounts}
        login={(email) => {
          const user = cookies.get("SavedUsers")[email];
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
      signIn: false,
      forgotPasswordPressed: false,
      lostValidationCodePressed: false
    };
    this.userLogin = this.userLogin.bind(this);
    this.oauthUserSigningIn = this.oauthUserSigningIn.bind(this);
  }

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

  componentDidMount() {
    document.body.style.backgroundColor = "#f2f2f2";
    document.title = "OneDataShare - Account";
    window.addEventListener("resize", this.resize.bind(this));
    this.setState({ loading: false });
    this.resize();
  }

  resize() {
    if (this.state.isSmall && window.innerWidth > 640) {
      this.setState({ isSmall: false });
    } else if (!this.state.isSmall && window.innerWidth <= 640) {
      this.setState({ isSmall: true });
    }
  }

  componentWillUnmount() {
    this.unsubscribe();
    document.body.style.backgroundColor = null;
  }

  oauthUserSigningIn(email, token, remember) {
    this.userLogin(email, token, remember, true, false, false, 8640000);
  }

  render() {
    const token = cookies.get("ATOKEN");
    const email = cookies.get("email");
    if (token && email) {
      this.oauthUserSigningIn(email, token, false);
      return <Redirect to={siteURLS.transferPageUrl} />;
    } else {
      const errorMessage = new URLSearchParams(window.location.search).get("error") ? new URLSearchParams(window.location.search).get("error") : "OAuth2 error: Unknown error occurred";
      console.error(errorMessage);
      cookies.remove("SavedUsers");
      return <Redirect
          to={{
            pathname: siteURLS.signInPageUrl,
            state: { error: true, errorMessage: errorMessage },
          }}
      />;
    }
  }
}

export default OAuth2RedirectHandler;
