import React, {Component} from 'react';
import Typography from '@material-ui/core/Typography';
import CardActions from '@material-ui/core/CardActions';
import Button from '@material-ui/core/Button';
import Checkbox from '@material-ui/core/Checkbox';
import { ValidatorForm, TextValidator } from 'react-material-ui-form-validator';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import { Link } from 'react-router-dom';

import PropTypes from 'prop-types';

import { checkLogin} from '../../APICalls/APICalls.js';


import {spaceBetweenStyle} from '../../constants.js';
import {updateGAPageView} from "../../analytics/ga";


export default class NewLoginComponent extends Component {

	static propTypes = {
	  	email : PropTypes.string,
	  	createAccountPressed: PropTypes.func,
	  	lostValidationCodePressed: PropTypes.func,
	  	forgotPasswordPressed: PropTypes.func,
	  	isLoading: PropTypes.func,
	  	userLoggedIn: PropTypes.func
	}
	

	constructor(props){
	    super(props);
	    this.state = {
	    	email: "",
	    	password: "",
	    	emailChecked: false,
	    	error: false,
	    	errorMessage: null,
	    	remember: false,
	    	isAuthenticated: false
	    }
	    
	    this.emailValidated = false;
	    this.onEmailNextClicked = this.onEmailNextClicked.bind(this);
		this.onSignInClicked = this.onSignInClicked.bind(this);
		updateGAPageView();
	}
	componentDidMount(){
		if(this.props.email){
			this.setState({email: this.props.email});
		}
	}

	onEmailNextClicked(){
		const { isLoading } = this.props;
		isLoading(true);

	    checkLogin(this.state.email,
	    	(success)=>{
	    		isLoading(false);
	    		this.setState({emailChecked: true});
	    	},
	    	(error)=>{
	    		isLoading(false);
	    		this.setState({emailChecked: false, error: true, errorMessage: "Email not found or server error."});
	    	}
	    );
	}

	onSignInClicked(){
		const { isLoading, userLoggedIn } = this.props;
		var { email, password, remember } = this.state;
		isLoading(true);

		userLoggedIn(email, password, remember, (error)=>{
    		isLoading(false);
    		console.log("error message", error);
			  this.setState({error: true, errorMessage: "Wrong password or server error."});
		});
	}

	render(){
		const { lostValidationCodePressed, forgotPasswordPressed } = this.props; 
		const { emailChecked, email, password, error, errorMessage, remember } = this.state;
		const handleChange = name => event => {
		    this.setState({
		      error: false,
		      errorMessage: null,
		      [name]: event.target.value,
		    });
		};

		return (<div >
		{!emailChecked &&
			<div className="enter-from-right slide-in">

	      	<Typography style={{fontSize: "1.6em", marginBottom: "0.4em", textAlign: 'center'}}>
	          Sign In
	        </Typography>
	        <Typography style={{fontSize: "1em", textAlign: 'center'}} color="textSecondary">
	          Onedatashare
	        </Typography>

	        <ValidatorForm
                ref="email"
                onSubmit={this.onEmailNextClicked}>
            	<TextValidator
            		error = {error}
            		helperText = {errorMessage}
								label="Email"
								onChange={handleChange('email')}
								id="email"
								name="email"
								value={email}
								validators={['required', 'isEmail']}
								errorMessages={['Please put email here', 'Can not understand email format']}
		          	style={{width: "90%", margin: "5%"}}
              />
		        <CardActions style={spaceBetweenStyle}>
			        <Button size="large" variant="outlined" color="primary">
								<Link to="/account/register">
                   Create Account
                </Link>
							</Button>
			        <Button size="large" variant="contained" color="primary"  type="submit" >
			          Next
			        </Button>
			    </CardActions>

            	</ValidatorForm>
		    </div>
	    }
	    {emailChecked &&
	    	<div className="enter-from-right slide-in">
	    	<Typography style={{fontSize: "1.6em", marginBottom: "0.4em"}}>
	          Hi {email.substring(0, email.indexOf('@'))}!
	        </Typography>
	        <Button size="large" style={{borderRadius: '20px'}} variant="outlined" color="primary" onClick={()=>this.setState({emailChecked: false})}>
	          {email}
	        </Button>

	        <ValidatorForm
                ref="password"
                onSubmit={this.onSignInClicked}>
            <TextValidator
            		error = {error}
            		helperText = {errorMessage}
            		id="Password"
                    label="Enter Your Password"
                    onChange={handleChange('password')}
                    type="password"
                    name="password"
	          		value={password}
                    validators={['required']}
                    errorMessages={['Where is password?']}
		          	style={{width: "100%", marginTop: "5%", marginBottom: "5%"}}
            />
            {/* <FormControlLabel
			control={
	            <Checkbox checked={remember} value="remember"
	            onChange={(event)=>{
	            	this.setState({remember: event.target.checked});
	            }}
	            color="primary"
	            />
	        } label="Remember"/> */}

	        <CardActions style={{...spaceBetweenStyle, marginBottom: '20px'}}>
						<Button 
							size="small" 
							variant="outlined"
							color="primary"
		        	onClick={()=>forgotPasswordPressed(email)}>
		          Forgot Password?
		        </Button>
						<Button 
							size="small" 
							variant="outlined"
							color="primary"
		        	onClick={()=>lostValidationCodePressed(email)}>
		          Lost Validation Email?
		        </Button>
		    </CardActions>

		    <Button size="large" variant="contained" color="primary" type="submit" style={{width: '100%'}}>
	        	Next
	      </Button>

		    </ValidatorForm>
	        
		    </div>
		}
		</div>);
	}
} 