
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

import { ValidatorForm, TextValidator } from 'react-material-ui-form-validator';
import { checkLogin } from '../../APICalls/APICalls.js';

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
        isLostVerifyCode: props.loadVerifyCode,
        emailError: false,
        firstNameError: false,
        lastNameError: false,
        emailErrorMessage: null,
        firstNameErrorMessage: null,
        lastNameErrorMessage: null,

      }
      this.firstNameValidationMsg = "Please Enter Your First Name"
      this.lastNameValidationMsg = "Please Enter Your Last Name"
      this.emailValidationMsg = "Please Enter EmailId"
      this.onEmailNextClicked = this.onEmailNextClicked.bind(this);

      this.registerAccount = this.registerAccount.bind(this);
      this.verifyAccount = this.verifyAccount.bind(this);
      this.login = this.login.bind(this);
	}

	registerAccount() {
        let email = this.state.email;
        let firstName = this.state.firstName;
        let lastName = this.state.lastName;
        let organization = this.state.organization;

        registerUser(email, firstName, lastName, organization).then((response)=>{
            if(response.status == 200 ){
              this.setState({screen: "verifyCode", verificationError: "", loading: false});
            }
            else if(response.status == 302) {
              this.setState({emaildError: "User with same Email Id already exists",
                verificationError: "User with same Email Id already exists",
                loading: false
              });
          }
       })
    }

    verifyAccount() {
        let email = this.state.email;
        let self = this;
        let verificationCode = this.state.verificationCode;
        verifyRegistraionCode(email, verificationCode).then((response) =>{
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
          self.props.history.push('/account/signIn')
        });
      }
    }

  onEmailNextClicked(){
  		var { email } = this.state;

      checkLogin(this.state.email,
        (success)=>{
          this.setState({error: true, errorMessage: "Email is already there on the server."});
        },
        (error)=>{
          this.registerAccount();
        }
      );
  	}

	render(){
    const { create, backToSignin, loadVerifyCode} = this.props;
    const {emailError, emailErrorMessage,  email, firstNameError, firstNameErrorMessage, lastNameError, lastNameErrorMessage} = this.state;
    const properties = this.props
		const handleChange = name => event => {
		   this.setState({
		   emailError: false,
		   emailErrorMessage: null,
        [name]: event.target.value,
      });
    };
    var screen = this.state.screen;
    const showLoader = this.state.loading;
      if(screen === "validateEmail"){

        return(
          <div className="enter-from-right slide-in">
            <ValidateEmailComponent {...properties} email = {this.state.email}></ValidateEmailComponent>
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

            <ValidatorForm
              ref="email"
              onSubmit={this.onEmailNextClicked}>
              <TextValidator
                error = {emailError}
                helperText = {emailErrorMessage}
                label="Email"
                onChange={handleChange('email')}
                name="email"
                value={email}
                validators={['required', 'isEmail']}
                errorMessages={['Please put email here', 'Can not understand email format']}
                style={{width: '100%', marginBottom: '50px'}}
             />

            <TextValidator
              error = {firstNameError}
              helperText = {firstNameErrorMessage}
              label="FirstName"
              name="firstName"
              value={this.state.firstName}
              id="FirstName"
              validators={['required']}
              errorMessages={['Please Enter Your First Name']}
              style={{width: '100%', marginBottom: '50px'}}
              onChange={ handleChange('firstName') }
            />

            <TextValidator
              error = {lastNameError}
              helperText = {lastNameErrorMessage}
              label="LastName"
              name="lastName"
              value={this.state.lastName}
              id="LastName"
              validators={['required']}
              errorMessages={['Please Enter Your Last Name']}
              style={{width: '100%', marginBottom: '50px'}}
              onChange={ handleChange('lastName') }
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
                  <Button size="large" variant="contained" color="primary" style={{marginLeft: '4vw'}} type="submit" onClick={this.registerAccount}>
                    Next
                  </Button>
              </CardActions>

             </ValidatorForm>
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
                          value={this.state.verificationCode}
                          style={{width: '100%', marginBottom: '50px'}}
                          onChange={ handleChange('verificationCode') }
                          error = {this.state.verificationError=="Please Enter Valid Verification Code"}
                      />

                      <CardActions style={spaceBetweenStyle,{float:'right'}}>
                          <Button size="medium" variant="outlined" color="primary" onClick={() =>{
                            if(this.state.isLostVerifyCode){
                             this.setState({screen:"validateEmail"})
                            }
                            else{
                              this.setState({screen: "registration"});
                            }
                          }}>
                            Back
                          </Button>
                          <Button size="large" variant="contained" color="primary" type="submit" style={{marginLeft: '4vw'}} onClick={this.verifyAccount}>
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