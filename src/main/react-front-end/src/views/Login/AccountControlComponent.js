import React, { Component } from 'react';
// ui import
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import LinearProgress from '@material-ui/core/LinearProgress';
// components
import NewLoginComponent from './NewLoginComponent';
import SavedLoginComponent from './SavedLoginComponent';
import CreateAccountComponent from './CreateAccountComponent';
import ValidateEmailComponent from './ValidateEmailComponent';
import ForgotPasswordComponent from './ForgotPasswordComponent';

import { Route, Switch, Redirect } from 'react-router-dom';

import { login } from '../../APICalls/APICalls.js';

import { transferPageUrl, signInUrl, registerPageUrl, forgotPasswordUrl , lostValidationCodeUrl } from "../../constants";
import { store } from '../../App.js';
import { loginAction } from '../../model/actions';
import {cookies} from '../../model/reducers';

export default class AccountControlComponent extends Component {

	constructor(props) {
		super(props);
    // redux login action
    this.unsubscribe = store.subscribe(() => {
    	this.setState({authenticated : store.getState().login});
    });


    const cookieSaved = cookies.get('SavedUsers') || 0;
	const rememberMeAccounts = cookieSaved === 0 ? {} : JSON.parse(cookieSaved);
	const currentRoute = this.props.location.pathname
    this.newLogin = <SavedLoginComponent
					accounts={rememberMeAccounts}
					login={(email) => {
						const user = JSON.parse(cookies.get('SavedUsers'))[email];
						this.userLogin(email, user.hash, false);
					}}
					removedAccount={(accounts) => {
						cookies.set('SavedUsers', JSON.stringify(accounts));
						this.setState({loading: false, accounts: accounts});
					}}
					useAnotherAccount={() => {
						this.setState({signIn: true});
					}}
					isLoading={(loading) => {
						this.setState({loading: loading});
					}}
				/>;
	
		this.state = {
			isSmall: window.innerWidth <= 640,
			password: "",
			loading: true,
			rememberMeAccounts: rememberMeAccounts,
			authenticated: store.getState().login,
			screen: this.newLogin,
			creatingAccount: false,
			loggingAccount: false,
			// When signIn is set, it launches /account/signIn url
			// In all back function's, that are sent as props to the child components, this flag is set to true
			// and the flag for the corresponding components is set to false
			// Eg: { signIn: true, creatingAccount: false } in props of 'CreateAccountComponent' component
			// If the user clicks register on navbar, then check the route and redirect to register page. So, the signIn
			// flag should be false
			signIn: false || (Object.keys(rememberMeAccounts).length === 0 && currentRoute !== registerPageUrl),
			forgotPasswordPressed: false,
			lostValidationCodePressed: false
		}
   	this.getInnerCard = this.getInnerCard.bind(this);
   	this.userLogin = this.userLogin.bind(this);
   	this.userSigningIn = this.userSigningIn.bind(this);
	}

	componentDidMount() {
		document.title = "OneDataShare - Account";
		window.addEventListener("resize", this.resize.bind(this));
		this.setState({ loading: false });
		this.resize();
	}

	static propTypes = {}

  // Called when user clicked login
  userLogin(email, hash, remember, saveOAuthTokens){
  	this.state.rememberMeAccounts[email] = { hash: hash };
	if(remember){
		cookies.set('SavedUsers', JSON.stringify(this.state.rememberMeAccounts));
	}

	store.dispatch(loginAction(email, hash, remember, saveOAuthTokens));
	//this.setState({authenticated : true});
  }
  
  componentWillUnmount(){
  	this.unsubscribe();
  }

  resize() {
		if (this.state.isSmall && window.innerWidth > 640) {
			this.setState({ isSmall: false });
		} else if (!this.state.isSmall && window.innerWidth <= 640) {
			this.setState({ isSmall: true });
		}
	}

	userSigningIn(email, password, remember, fail){
		login(email, password,
			(success) => {
				console.log("success account", success);
	    		this.userLogin(email, success.hash, remember, success.saveOAuthTokens);
	    	},
	    	(error) => {fail(error)}
	    );
	}
	// Switches to a route and renders a component based on the redirect set inside render method.
	getInnerCard() {
		return (
			<Switch>
				<Route exact path={'/account'}
					render={(props) => this.state.screen}>
				</Route>

				<Route exact path={registerPageUrl}
					render={(props) => <CreateAccountComponent {...props}
						create={(email, password) => {

						}}
						backToSignin={() => {
							this.setState({ signIn: true, creatingAccount: false });
						}}
					/>}>
				</Route>

				<Route exact path={lostValidationCodeUrl}
					render={(props) => <ValidateEmailComponent {...props}
						email={this.state.email}
						backToSignin={() => {
							this.setState({ loading: false, signIn: true, lostValidationCodePressed: false });
						}} />}>
				</Route>

				<Route exact path={forgotPasswordUrl}
					render={(props) => <ForgotPasswordComponent {...props} back={() => {
						this.setState({ loading: false, signIn:true, forgotPasswordPressed: false });
					}} email={this.state.email} />}>
				</Route>

				<Route exact path={signInUrl}
					render={(props) => <NewLoginComponent
								email={this.props.email}
								isLoading={(loading) => {
									this.setState({ loading: loading });
								}}
								createAccountPressed={() => {
									this.setState({ loading: false, creatingAccount: true, signIn: false });
								}}
								lostValidationCodePressed={(email) => {
									this.setState({
										loading: false,
										lostValidationCodePressed: true,
										signIn:false,
										email: email
									});
								}}
								forgotPasswordPressed={(email) => {
									this.setState({
										loading: false,
										signIn:false,
										email:email,
										forgotPasswordPressed: true
									});
								}}
								userLoggedIn={this.userSigningIn}
							/>

					}>
				</Route>
			</Switch>
		);
	}

	render() {

		const { isSmall, loading, creatingAccount, signIn, forgotPasswordPressed, lostValidationCodePressed } = this.state;
		const height = window.innerHeight + "px";
		const currentRoute = this.props.location.pathname
			return (

				<div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', width: '..', height: height }}>
					<div style={{ width: '450px', marginTop: '30px', marginLeft: '30px', marginRight: '30px', alignSelf: isSmall ? 'flex-start' : 'center' }}>
						{/* { console.log(store.getState().login + "-" + forgotPasswordPressed  + "-" + creatingAccount +"-"+lostValidationCodePressed+ "-" + signIn + "-" + Object.keys(rememberMeAccounts).length )}
						{console.log(currentRoute)} */}
						{/* At any point of time only one among below should be true */}
						{(currentRoute !== lostValidationCodeUrl && lostValidationCodePressed) && <Redirect to={lostValidationCodeUrl} />}
						{store.getState().login && <Redirect to={transferPageUrl} />}
						{(currentRoute !== registerPageUrl && creatingAccount) && <Redirect to={registerPageUrl} />}
						{(currentRoute !== forgotPasswordUrl && forgotPasswordPressed) && <Redirect to={forgotPasswordUrl} />}
						{(currentRoute !== signInUrl && signIn)  && <Redirect to={signInUrl} />}
						{loading && <LinearProgress />}


						{isSmall &&
							this.getInnerCard()
						}
						{!isSmall &&
							<Card>
								<CardContent style={{ padding: '3em' }}>
									{this.getInnerCard()}
								</CardContent>
							</Card>
						}
					</div>
				</div>

			);



	}
}