import React, {Component} from 'react';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import CardActions from '@material-ui/core/CardActions';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';

import {spaceBetweenStyle} from '../../constants.js';

export default class ForgotPasswordComponent extends Component {

	static propTypes = {
		back: PropTypes.func,
		email: PropTypes.string
	}

	constructor(props){
	    super(props);
	    this.state = {
	    	password: ""
	    }
	}

	render(){
		const {back, email} = this.props;
		return (
		<div className="enter-from-right slide-in">
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
		        <Button size="large" variant="contained" color="primary" style={{marginLeft: '4vw'}}>
		          Next
		        </Button>
		    </CardActions>
		</div>);
	}
} 