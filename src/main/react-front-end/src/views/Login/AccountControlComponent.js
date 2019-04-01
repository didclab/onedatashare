import React, { Component } from 'react';
import PropTypes from 'prop-types';
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

import { TransitionGroup, CSSTransition } from "react-transition-group";

import  { Route, Switch, Redirect } from 'react-router-dom';

import { login, isAdmin } from '../../APICalls/APICalls.js';

import {addAccountUrl, transferPageUrl} from "../../constants";
import {store} from '../../App.js';
import {loginAction, isAdminAction} from '../../model/actions';
import {cookies} from '../../model/reducers';

export default class AccountControlComponent extends Component {

  static propTypes = {}
  // Called when user clicked login
  userLogin(email, hash, publicKey, remember){
  	this.state.accounts[email] = {hash: hash, publicKey: publicKey};
	if(remember){
		cookies.set('SavedUsers', JSON.stringify(this.state.accounts));
	}
	
	isAdmin(email, hash, (status) => {
		store.dispatch(isAdminAction());
	}, (fail) => {
		console.log("Not Admin", fail);
	})
	
	store.dispatch(loginAction(email, hash, publicKey, remember));
	//this.setState({authenticated : true});
  }
  componentWillUnmount(){
  	this.unsubscribe();
  }

  constructor(props){
    super(props);
    // redux login action
    this.unsubscribe = store.subscribe(() => {
    	this.setState({authenticated : store.getState().login});
    });


    const cookieSaved = cookies.get('SavedUsers') || 0;
    const accounts = cookieSaved == 0 ? {} : JSON.parse(cookieSaved);
    this.newLogin = <SavedLoginComponent 
					accounts={accounts} 
					login={(email) => {
						const user = JSON.parse(cookies.get('SavedUsers'))[email];
						this.userLogin(email, user.hash, user.publicKey, false);
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
    	accounts: accounts,
    	authenticated: store.getState().login,
    	screen: this.newLogin,
    	creatingAccount: false,
    	loggingAccount: false,
    	signIn: false,
    	forgotPasswordPressed: false,
    	validateEmailPressed: false
    }
   	this.getInnerCard = this.getInnerCard.bind(this);
   	this.userLogin = this.userLogin.bind(this);
   	this.userSigningIn = this.userSigningIn.bind(this);
  }
  componentWillMount(){

  }
	componentDidMount() {
		window.addEventListener("resize", this.resize.bind(this));
		this.setState({loading: false});
		this.resize();
	}
	resize() {
		if(this.state.isSmall && window.innerWidth > 640){
			this.setState({isSmall: false});
		}else if(!this.state.isSmall && window.innerWidth <= 640){
			this.setState({isSmall: true});
		}
	}
	userSigningIn(email, password, remember, fail){
		login(email, password,
	    	(success) => {
				console.log("success account", success);
	    		this.userLogin(email, success.hash, success.publicKey, remember);
	    	},
	    	(error) => {fail(error)}
	    );
	}
	getInnerCard() {
		return(
		<Switch>
				<Route exact path={'/account'}  
					render={(props) => this.state.screen}>
				</Route>
				<Route exact path={'/account/register'}  
					render={(props) => <CreateAccountComponent {...props}
						create={(email, password) => {

						}}
						backToSignin={() => {
							console.log("click");
							this.setState({loggingAccount: true});
						}}
					/>}>
				</Route>
				<Route exact path={'/account/signIn'} 
					render={(props) =>
						<div>
							{(this.state.forgotPasswordPressed || this.state.validateEmailPressed) && 
								<Redirect to='/account'/>
							}
							<NewLoginComponent email={this.props.email} 
								isLoading={(loading) => {
									this.setState({loading: loading});
								}}

								createAccountPressed={() => {
									this.setState({loading: false, createAccount: true, creatingAccount: true});
								}}

								validateEmailPressed={(email) => {
									this.setState({loading: false, screen:	
										<ValidateEmailComponent {...props} back={() => {
											this.setState({loading: false, screen: this.newLogin, validateEmailPressed: false});
										}} email={email}/>,
										validateEmailPressed: true
									});
								}}

								forgotPasswordPressed={(email) => {
									this.setState({loading: false, screen: 
										<ForgotPasswordComponent back={() => {
											this.setState({loading: false, screen: this.newLogin, forgotPasswordPressed: false});
										}} email={email}/>,
										forgotPasswordPressed: true
									});
								}}
								
								userLoggedIn={this.userSigningIn}
							/>
						</div>
					}>
				</Route>
			</Switch>
			);
	}

  	render() {

	    const {isSmall, loading, accounts, screen, authenticated, creatingAccount, loggingAccount, signIn, forgotPasswordPressed} = this.state;
	    console.log(forgotPasswordPressed)
	    const isNewUser = Object.keys(accounts).length == 0;
	    const handleChange = name => event => {
		    this.setState({
		      [name]: event.target.value,
		    });
		  };

		console.log(addAccountUrl);
		this.state.creatingAccount = false;
		this.state.loggingAccount = false;
		this.state.signIn = false;

  		const height = window.innerHeight+"px";
	    return (

  		  <div style={{display: 'flex', justifyContent: 'center', alignItems: 'center', width: '..', height: height}}>
		    <div style={{width: '450px', marginTop: '30px', marginLeft: '30px',marginRight: '30px', alignSelf:  isSmall ? 'flex-start': 'center'}}>
		    
		    {store.getState().login && <Redirect to={transferPageUrl}/>}
		    {creatingAccount && <Redirect to={"/account/register"}/>}
		    {loggingAccount && <Redirect to={"/account"}/>}
		    {signIn && <Redirect to={"/account/signIn"}/>}
		    {loading && <LinearProgress  />}

		    {isSmall &&
		    	this.getInnerCard() 
		    }
		    {!isSmall &&
		      <Card>
		      	<CardContent style={{padding: '3em'}}>
		      		{this.getInnerCard() }
		      	</CardContent>
		      </Card>
		  	}
		    </div>
		    </div>

	    );
  	}
}