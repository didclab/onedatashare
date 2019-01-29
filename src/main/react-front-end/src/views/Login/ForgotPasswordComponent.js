import React, {Component} from 'react';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import CardActions from '@material-ui/core/CardActions';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';

import {spaceBetweenStyle} from '../../constants.js';
import {resetPasswordSendCode, resetPasswordVerifyCode, resetPassword} from '../../APICalls/APICalls.js';
import {eventEmitter} from '../../App';
const beforeCode = 0
const codeSent = 1
const codeVerified = 2

export default class ForgotPasswordComponent extends Component {

	static propTypes = {
		back: PropTypes.func,
		email: PropTypes.string
	}

	constructor(props){
	    super(props);
	    this.state = {
	    	password: "",
	    	confirmedPassword: "",
	    	state: beforeCode,
	    	code: "",
	    }
	}

	SetPassword = ()=>{

		const { confirmedPassword, password, code} = this.state;
		if(password !== confirmedPassword){
			eventEmitter.emit("errorOccured", "Password and confirmed password need to be the same.");
			return;
		}
		resetPassword(this.props.email,code,password, confirmedPassword, ()=>{
			this.props.back();
			eventEmitter.emit("errorOccured", "Set Password Succeed.");
		}, ()=>{
			//this.props.back();
			eventEmitter.emit("errorOccured", "Verify Code Failed.");
		});   	
	}

	VerifyCode = ()=>{
		resetPasswordVerifyCode(this.props.email,this.state.code, (response)=>{
			console.log(response)
			this.setState({state: codeVerified, code: response})
		}, (fail)=>{
			//this.setState({state: codeVerified})
			eventEmitter.emit("errorOccured", "Verify Code Failed.");
		});
	}

	SendCode = ()=>{
		resetPasswordSendCode(this.props.email, ()=>{
			this.setState({state: codeSent})
		}, ()=>{
			//this.setState({state: codeSent})
			eventEmitter.emit("errorOccured", "Send Code Failed.");
		});   	
	}
	render(){
		const {back, email} = this.props
		const {state, confirmedPassword, password} = this.state;
		const confirmed = (password !== confirmedPassword);
		const handleChange = name => event => {
		    this.setState({
		      [name]: event.target.value,
		    });
		  };
		return (
		<div className="enter-from-right slide-in">

			{state == beforeCode &&
				<div>
					<Typography style={{fontSize: "1.6em", marginBottom: "0.4em"}}>
			          Account recovery
			        </Typography>
			        <Button size="small" variant="outlined" color="primary" onClick={back} style={{marginBottom: "1em", borderRadius: '25px'}}>
			          {email}
			        </Button>
			        <Typography style={{fontSize: "1.0em", marginBottom: "0.4em"}}>
			          Get a verification code
			        </Typography>
			        <Typography style={{fontSize: "0.8em", marginBottom: "0.4em"}}>
			          Onedatashare will send a verification code to {email}
			        </Typography>
			        <CardActions style={spaceBetweenStyle}>
				        <Button size="medium" variant="outlined" color="primary" onClick={back}>
				          Back to Sign in
				        </Button>
				        <Button size="large" variant="contained" color="primary" style={{marginLeft: '4vw'}} onClick={this.SendCode}>
				          Next
				        </Button>
				    </CardActions>
				</div>
			}

			{state == codeSent &&
				<div>
			        
			        <Typography style={{fontSize: "0.8em", marginBottom: "0.4em"}}>
			          Verification code is sent to {email}
			        </Typography>
			        <Button size="small" variant="outlined" color="primary" onClick={this.SendCode} style={{marginBottom: "1em", borderRadius: '22px'}}>
			          Send Again?
			        </Button>
			        <Typography style={{fontSize: "1.0em", marginBottom: "0.4em"}}>
			          Input your code here
			        </Typography>
			        <TextField
			          style={{width: '100%', marginBottom: '20px'}}
			          id="outlined-email-input"
			          label="Code"
			          name="code"
			          margin="normal"
			          variant="outlined"
			          onChange={handleChange('code')}
			        />
			        <CardActions style={spaceBetweenStyle}>
				        <Button size="medium" variant="outlined" color="primary" onClick={back}>
				          Back to Sign in
				        </Button>
				        <Button size="large" variant="contained" color="primary" style={{marginLeft: '4vw'}} onClick={this.VerifyCode}>
				          Verify
				        </Button>
				    </CardActions>
				</div>
			}

			{state == codeVerified &&
				<div>
			        <Typography style={{fontSize: "1.0em", marginBottom: "0.4em"}}>
			          Code is Verified, Reset your password here:
			        </Typography>
			        <TextField
			          style={{width: '100%'}}
			          id="outlined-email-input"
			          label="Password"
			          name="code"
			          type="password"
			          margin="normal"
			          variant="outlined"
			          onChange={handleChange('password')}
			        />
			        <TextField
			          error={confirmed}
			          style={{width: '100%', marginBottom: '20px'}}
			          id="outlined-email-input"
			          label="Confirm Password"
			          name="code"
			          type="password"
			          margin="normal"
			          variant="outlined"
			          onChange={handleChange('confirmedPassword')}
			        />
			        <CardActions style={spaceBetweenStyle}>
				        <Button size="medium" variant="outlined" color="primary" onClick={back}>
				          Back to Sign in
				        </Button>
				        <Button size="large" variant="contained" color="primary" style={{marginLeft: '4vw'}} disabled={confirmed} onClick={this.SetPassword}>
				          Reset
				        </Button>
				    </CardActions>
				</div>
			}
		</div>);
	}
} 