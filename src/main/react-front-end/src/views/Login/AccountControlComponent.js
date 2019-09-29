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
			this.setState({ authenticated: store.getState().login });
		});


		const cookieSaved = cookies.get('SavedUsers') || 0;
		const rememberedAccounts = cookieSaved === 0 ? {} : JSON.parse(cookieSaved);
		this.newLogin = <SavedLoginComponent
			accounts={rememberedAccounts}
			login={(email) => {
				const user = JSON.parse(cookies.get('SavedUsers'))[email];
				this.userLogin(email, user.hash, user.publicKey, false);
			}}
			removedAccount={(accounts) => {
				cookies.set('SavedUsers', JSON.stringify(accounts));
				this.setState({ loading: false, accounts: accounts });
			}}
			useAnotherAccount={() => {
				this.setState({ signIn: true });
			}}
			isLoading={(loading) => {
				this.setState({ loading: loading });
			}}
		/>;
		this.state = {
			isSmall: window.innerWidth <= 640,
			password: "",
			loading: true,
			rememberedAccounts: rememberedAccounts,
			authenticated: store.getState().login,
			screen: this.newLogin,
			creatingAccount: false,
			loggingAccount: false,
			signIn: false || Object.keys(rememberedAccounts).length === 0,
			forgotPasswordPressed: false,
			validateEmailPressed: false
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
	userLogin(email, hash, publicKey, remember) {
		this.state.rememberedAccounts[email] = { hash: hash, publicKey: publicKey };
		if (remember) {
			cookies.set('SavedUsers', JSON.stringify(this.state.rememberedAccounts));
		}

		store.dispatch(loginAction(email, hash, publicKey, remember));
		//this.setState({authenticated : true});
	}
	componentWillUnmount() {
		this.unsubscribe();
	}

	resize() {
		if (this.state.isSmall && window.innerWidth > 640) {
			this.setState({ isSmall: false });
		} else if (!this.state.isSmall && window.innerWidth <= 640) {
			this.setState({ isSmall: true });
		}
	}
	userSigningIn(email, password, remember, fail) {
		login(email, password,
			(success) => {
				console.log("success account", success);
				this.userLogin(email, success.hash, success.publicKey, remember);
			},
			(error) => { fail(error) }
		);
	}
	getInnerCard() {
		return (
			<Switch>
				<Route exact path={'/account'}
					render={(props) => this.state.screen}>
				</Route>
				<Route exact path={'/account/register'}
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
							this.setState({ loading: false, signIn: true, validateEmailPressed: false });
						}} />}>
				</Route>
				<Route exact path={signInUrl}
					render={(props) =>
							<NewLoginComponent email={this.props.email}
								isLoading={(loading) => {
									this.setState({ loading: loading });
								}}

								createAccountPressed={() => {
									this.setState({ loading: false, creatingAccount: true, signIn: false });
								}}

								validateEmailPressed={(email) => {
									this.setState({
										loading: false,
										validateEmailPressed: true,
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
				
				<Route exact path={forgotPasswordUrl}
					render={(props) => <ForgotPasswordComponent {...props} back={() => {
						this.setState({ loading: false, screen: this.newLogin, signIn:true, forgotPasswordPressed: false });
					}} email={this.state.email} />}>
				</Route>
			</Switch>
		);
	}

	render() {

		const { isSmall, loading, creatingAccount, loggingAccount, signIn, forgotPasswordPressed, validateEmailPressed, rememberedAccounts } = this.state;
		const height = window.innerHeight + "px";
		const currentRoute = this.props.location.pathname
			return (

				<div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', width: '..', height: height }}>
					<div style={{ width: '450px', marginTop: '30px', marginLeft: '30px', marginRight: '30px', alignSelf: isSmall ? 'flex-start' : 'center' }}>
						{/* { console.log(store.getState().login + "-" + forgotPasswordPressed  + "-" + creatingAccount +"-"+validateEmailPressed+ "-" + signIn + "-" + Object.keys(rememberedAccounts).length )} */}
						{(currentRoute !== lostValidationCodeUrl && validateEmailPressed) && <Redirect to={lostValidationCodeUrl} />}
						{store.getState().login && <Redirect to={transferPageUrl} />}
						{(currentRoute !== registerPageUrl && creatingAccount) && <Redirect to={registerPageUrl} />}
						{(currentRoute !== forgotPasswordUrl && forgotPasswordPressed) && <Redirect to={forgotPasswordUrl} />}
						{/* {loggingAccount && <Redirect to={"/account"} />} */}
						{(currentRoute !== signInUrl && signIn)  && <Redirect to={signInUrl} />}
						{loading && <LinearProgress />}

						{/* {console.log("executing after redirect. The route is.. " + this.props.location.pathname)} */}

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