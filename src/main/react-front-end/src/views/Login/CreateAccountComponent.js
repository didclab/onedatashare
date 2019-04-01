
import React, {Component} from 'react';
import { Redirect } from 'react-router-dom'
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import CardActions from '@material-ui/core/CardActions';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';
import {spaceBetweenStyle} from '../../constants.js';
import {registerUser,verifyRegistraionCode,setPassword} from '../../APICalls/APICalls.js'
import LinearProgress from '@material-ui/core/LinearProgress';
import ValidateEmailComponent from '../Login/ValidateEmailComponent'


export default class CreateAccountComponent extends Component {
	static propTypes = {
	  	create : PropTypes.func,
	  	backToSignin: PropTypes.func,
	}
	constructor(props){
      super(props);
      
	    this.state = {
	    	email: props.email != "" ? props.email: "",
	    	password: "",
	    	cpassword: "",
	    	code : "",
        screen : props.loadVerifyCode ? "verifyCode":"registration",
        verificationError : "",
        passwordError : "",
        firstName:"",
        lastName:"",
        organization:"",
        loading: false,
        isStorkAccount: props.loadVerifyCode
      }
      this.firstNameValidationMsg = "Please Enter Your First Name"
      this.lastNameValidationMsg = "Please Enter Your Last Name"
      this.emailValidationMsg = "Please Enter EmailId"

	    this.onNextClicked = this.onNextClicked.bind(this);
	    this.registerAccount = this.registerAccount.bind(this);
        this.verifyAccount = this.verifyAccount.bind(this);
        this.login = this.login.bind(this);
	}

	registerAccount() {

        let email = this.state.email;
        let firstName = this.state.firstName;
        let lastName = this.state.lastName;
        let organization = this.state.organization;
        let self = this;

        if(email.trim().length == 0) {
            let state = self.state;
            state.emaildError = this.emailValidationMsg;
            self.setState({state});
        }
        else if(firstName.trim().length == 0) {
          let state = self.state;
          state.firstNameValidation = this.firstNameValidationMsg;
          self.setState({state});
        }
        else if(lastName.trim().length == 0) {
          let state = self.state;
          state.lastNameValidation = this.lastNameValidationMsg;
          self.setState({state});
        }
        else {
          this.state.loading = true;
          self.setState(this.state)
            registerUser(email, firstName, lastName, organization).then((response)=>{
              self.state.loading = false;
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
        verifyRegistraionCode(email, code).then((response) =>{
            let state = self.state;
            if(response.status == 200 ) {
                state.screen = "setPassword";
                state.code = response.data;
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
            setPassword(email, code, password, confirmPassword).then((response) =>{

                //state.screen = "setPassword";
                //self.setState({state});
                //console.log("Helleo");
                //self.props.history.push('/account/signIn')
            });
        }
    }

	shouldComponentUpdate(nextProps, nextState) {
		  console.log("next ", nextState);
    	return true;
  	}

	onNextClicked(){
		const { isLoading, userLoggedIn } = this.props;
		var { email, password, remember } = this.state;
		isLoading(true);

		userLoggedIn(email, password, remember, (error) =>{
    		isLoading(false);
			this.setState({error: true, errorMessage: error});
		});
	}

	render(){
		const { create, backToSignin, loadVerifyCode } = this.props;
		const handleChange = name => event => {
      if(name === 'firstName'){
        this.state.firstNameValidation = ""
        this.setState(this.state)
      }
      if(name === 'lastName'){
        this.state.lastNameValidation = ""
        this.setState(this.state)
      }
      if(name === 'email'){
        this.state.emaildError = ""
        this.setState(this.state)
      }
		    this.setState({
		      [name]: event.target.value,
		    });
    };
    
  var screen = this.state.screen;
  // if(loadVerifyCode){
  //   screen =  "verifyCode";
  // }
    const showLoader = this.state.loading;
            // if(screen === "signIn"){
            //   return(
            //     <Redirect to='/account/signIn'/>
            //   )
            // }
            if(screen === "validateEmail"){
              return(
                <div className="enter-from-right slide-in">
                  <ValidateEmailComponent email = {this.state.email}></ValidateEmailComponent>
                </div>
              )
            }
        		if(screen === "registration"){
        		    return (
                    		<div className="enter-from-right slide-in">
                              <div>{showLoader && <LinearProgress></LinearProgress>}</div>
                    	      	<Typography style={{fontSize: "1.6em", marginBottom: "0.4em"}}>
                    	          Create your OneDataShare Account
                    	        </Typography>
                    	        <TextField
                    	          id="Email"
                    	          label={this.state.emaildError === this.emailValidationMsg ? this.emailValidationMsg: "Email*"}
                    	          value={this.state.email}
                    	          style={{width: '100%', marginBottom: '50px'}}
                    	          onChange={ handleChange('email') }
                    	          error = {this.state.emaildError === this.emailValidationMsg }
                    	        />
                              <TextField
                    	          id="FirstName"
                    	          label={this.state.firstNameValidation === this.firstNameValidationMsg ? this.firstNameValidationMsg: "First Name*"}
                    	          value={this.state.firstName}
                    	          style={{width: '100%', marginBottom: '50px'}}
                    	          onChange={ handleChange('firstName') }
                    	          error = {this.state.firstNameValidation === this.firstNameValidationMsg}
                    	        />
                              <TextField
                    	          id="LastName"
                    	          label={this.state.lastNameValidation === this.lastNameValidationMsg ? this.lastNameValidationMsg: "Last Name*"}
                    	          value={this.state.lastName}
                    	          style={{width: '100%', marginBottom: '50px'}}
                    	          onChange={ handleChange('lastName') }
                    	          error = {this.state.lastNameValidation === this.lastNameValidationMsg}
                    	        />
                              <TextField
                    	          id="Organization"
                    	          label={"Organization"}
                    	          value={this.state.organization}
                    	          style={{width: '100%', marginBottom: '50px'}}
                    	          onChange={ handleChange('organization') }
                    	        />

                    	        <CardActions style={spaceBetweenStyle, {float:'right'}}>
                    		        <Button size="medium" variant="outlined" color="primary" onClick={backToSignin}>
                    		          Sign in Instead
                    		        </Button>
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
                              Please check {this.state.email} for authorization code
                            </Typography>
                            <TextField
                                id="code"
                                label={this.state.verificationError=="" ? "Enter Verification Code": "Please Enter Valid Verification Code"}
                                value={this.state.code}
                                style={{width: '100%', marginBottom: '50px'}}
                                onChange={ handleChange('code') }
                                error = {this.state.verificationError=="Please Enter Valid Verification Code"}
                            />

                            <CardActions style={spaceBetweenStyle,{float:'right'}}>
                                <Button size="medium" variant="outlined" color="primary" onClick={() =>{
                                  if(this.state.isStorkAccount){
                                   this.setState({screen:"validateEmail"})
                                  }
                                  else{
                                    this.setState({screen: "registration"});
                                  }                                  
                                }}>
                                  Back
                                </Button>
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
                              Code Verified! Please set password for your account
                            </Typography>

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

                            <CardActions style={spaceBetweenStyle, {float:'right'}}>
                                <Button size="medium" variant="outlined" color="primary" onClick={() =>{
                                  this.setState({screen: "verifyCode"});
                                }}>
                                  Back
                                </Button>
                                <Button size="large" variant="contained" color="primary" style={{marginLeft: '4vw'}} onClick={this.login}>
                                  Next
                                </Button>
                            </CardActions>
                        </div>);
                }
                }

} 