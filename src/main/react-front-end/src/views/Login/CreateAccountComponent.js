
import React, {Component} from 'react';
import { Redirect } from 'react-router-dom'
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import CardActions from '@material-ui/core/CardActions';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';
import {spaceBetweenStyle} from '../../constants.js';
import {registerUser,verifyRegistraionCode,setPassword} from '../../APICalls/APICalls.js'


export default class CreateAccountComponent extends Component {
	static propTypes = {
	  	create : PropTypes.func,
	  	backToSignin: PropTypes.func,
	  	 
	}
	constructor(props){
	    super(props);
	    this.state = {
	    	email: "",
	    	password: "",
	    	cpassword: "",
	    	code : "",
            screen : "registration",
            verificationError : "",
            passwordError : ""
	    }

	    this.onNextClicked = this.onNextClicked.bind(this);
	    this.registerAccount = this.registerAccount.bind(this);
        this.verifyAccount = this.verifyAccount.bind(this);
        this.login = this.login.bind(this);
	}

	registerAccount() {
        let email = this.state.email;
        let self = this;
        if(email.trim().length == 0) {
            let state = self.state;
            state.emaildError = "Please Enter EmailId";
            self.setState({state});
        }
        else {
            registerUser(email).then((response)=>{
                if(response.status == 200 ){
                    let state = self.state;
                    state.screen = "verifyCode";
                    state.verificationError = "";   // clear any verification code
                    self.setState({state});
                }
                else if(response.status == 302) {
                    let state = self.state;
                    state.verificationError = "User with same Email Id already exists";
                    self.setState({state});
                }
            })
        }
    }

    verifyAccount() {
        let email = this.state.email;
        let self = this;
        let code = this.state.code;
        verifyRegistraionCode(email, code).then((response)=>{
            let state = self.state;
            if(response.status == 200 ) {
                state.screen = "setPassword";
                state.verificationError = "";
                self.setState({state});
            }
            else {
                state.verificationError = "Please Enter Valid Verification Code";
                self.setState({state});
            }
        });
    }

    login() {
        let email = this.state.email;
        let password = this.state.password;
        let confirmPassword = this.state.cpassword;
        let self = this;
        let code = this.state.code;
        let state = self.state;

        if(password!=confirmPassword){
            state.passwordError = "Password Doesn't Match";
            self.setState({state});
        }
        else{
            setPassword(email, code, password, confirmPassword).then((response)=>{

                //state.screen = "setPassword";
                //self.setState({state});
                //console.log("Helleo");
                self.props.history.push('/account')
            });
        }
    }

	shouldComponentUpdate(nextProps, nextState) {
		console.log("next ", nextState);
		if(nextState.email === "") return false;
    	//if (this.props.create === nextProps.create && this.props.backToSignin === nextProps.backToSignin) return false;
    	return true;
  	}

	onNextClicked(){
		const { isLoading, userLoggedIn } = this.props;
		var { email, password, remember } = this.state;
		isLoading(true);

		userLoggedIn(email, password, remember, (error)=>{
    		isLoading(false);
			this.setState({error: true, errorMessage: error});
		});
	}

	render(){
		const { create, backToSignin } = this.props;
		const handleChange = name => event => {
		    this.setState({
		      [name]: event.target.value,
		    });
		};
		const screen = this.state.screen;
        		if(screen === "registration"){
        		    return (
                    		<div className="enter-from-right slide-in">
                    	      	<Typography style={{fontSize: "1.6em", marginBottom: "0.4em"}}>
                    	          Create your OneDataShare Account
                    	        </Typography>
                    	        <TextField
                    	          id="Email"
                    	          label={this.state.verificationError === "User with same Email Id already exists" ? "User with same Email Id already exists" : "Enter your email id"}
                    	          value={this.state.email}
                    	          style={{width: '100%', marginBottom: '50px'}}
                    	          onChange={ handleChange('email') }
                    	          error = {this.state.verificationError === "User with same Email Id already exists"}
                    	        />
                    	        {/*<TextField
                    	          id="Password"
                    	          label="Password"
                    	          type="password"
                    	          value={this.state.password}
                    	          style={{width: '100%', marginBottom: '50px'}}
                    	          onChange={ handleChange('password') }
                    	        />
                    	        <TextField
                    	          id="Cpassword"
                    	          type="password"
                    	          label="Confirm Password"
                    	          value={this.state.cpassword}
                    	          style={{width: '100%', marginBottom: '50px'}}
                    	          onChange={ handleChange('cpassword') }
                    	        />*/}
                    	        <CardActions style={spaceBetweenStyle, {float:'right'}}>
                    		        {/*<Button size="medium" variant="outlined" color="primary" onClick={backToSignin}>
                    		          Sign in Instead
                    		        </Button>*/}
                    		        <Button size="large" variant="contained" color="primary" style={{marginLeft: '4vw'}} onClick={this.registerAccount}>
                    		          Next
                    		        </Button>
                    		    </CardActions>
                    		</div>);
        		}
                if(screen === "verifyCode") {
                    return (
                        <div className="enter-from-right slide-in">
                            <Typography style={{fontSize: "1.6em", marginBottom: "0.4em"}}>
                              Create your OneDataShare Account
                            </Typography>
                            <TextField
                              id="Email"
                              label="Enter Your Email"
                              value={this.state.email}
                              style={{width: '100%', marginBottom: '50px'}}
                              onChange={ handleChange('email') }
                            />
                            <TextField
                                  id="code"
                                  label={this.state.verificationError=="" ? "Enter Verification Code": "Please Enter Valid Verification Code"}
                                  value={this.state.code}
                                  style={{width: '100%', marginBottom: '50px'}}
                                  onChange={ handleChange('code') }
                                  error = {this.state.verificationError=="Please Enter Valid Verification Code"}
                            />
                            {/*<TextField
                              id="Password"
                              label="Password"
                              type="password"
                              value={this.state.password}
                              style={{width: '100%', marginBottom: '50px'}}
                              onChange={ handleChange('password') }
                            />
                            <TextField
                              id="Cpassword"
                              type="password"
                              label="Confirm Password"
                              value={this.state.cpassword}
                              style={{width: '100%', marginBottom: '50px'}}
                              onChange={ handleChange('cpassword') }
                            />*/}
                            <CardActions style={spaceBetweenStyle,{float:'right'}}>
                                {/*<Button size="medium" variant="outlined" color="primary" onClick={backToSignin}>
                                  Sign in Instead
                                </Button>*/}
                                <Button size="large" variant="contained" color="primary" style={{marginLeft: '4vw'}} onClick={this.verifyAccount}>
                                  Next
                                </Button>
                            </CardActions>
                        </div>);
                }
                if(screen === "setPassword") {
                    return (
                        <div className="enter-from-right slide-in">
                            <Typography style={{fontSize: "1.6em", marginBottom: "0.4em"}}>
                              Create your OneDataShare Account
                            </Typography>
                            <TextField
                              id="Email"
                              label="Enter Your Email"
                              value={this.state.email}
                              style={{width: '100%', marginBottom: '50px'}}
                              onChange={ handleChange('email') }

                            />
                            <TextField
                              id="Password"
                              label="Password"
                              type="password"
                              value={this.state.password}
                              style={{width: '100%', marginBottom: '50px'}}
                              onChange={ handleChange('password') }

                            />
                            <TextField
                              id="Cpassword"
                              type="password"
                              label={this.state.passwordError === "Password Doesn't Match" ? "Password Doesn't Match" : "Confirm Password"}
                              value={this.state.cpassword}
                              style={{width: '100%', marginBottom: '50px'}}
                              onChange={ handleChange('cpassword') }
                              error = {this.state.passwordError === "Password Doesn't Match"}
                            />
                            <TextField
                              id="code"
                              label="Enter Verification Code"
                              value={this.state.code}
                              style={{width: '100%', marginBottom: '50px'}}
                              onChange={ handleChange('code') }
                            />
                            <CardActions style={spaceBetweenStyle, {float:'right'}}>
                                {/*<Button size="medium" variant="outlined" color="primary" onClick={backToSignin}>
                                  Sign in Instead
                                </Button>*/}
                                <Button size="large" variant="contained" color="primary" style={{marginLeft: '4vw'}} onClick={this.login}>
                                  Next
                                </Button>
                            </CardActions>
                        </div>);
                }
                }

} 