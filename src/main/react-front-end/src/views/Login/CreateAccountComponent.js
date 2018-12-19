
import React, {Component} from 'react';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import CardActions from '@material-ui/core/CardActions';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';
import {spaceBetweenStyle} from '../../constants.js';


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
	    	cpassword: ""
	    }

	    this.onNextClicked = this.onNextClicked.bind(this);
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
	          label="Confirm Password"
	          value={this.state.cpassword}
	          style={{width: '100%', marginBottom: '50px'}}
	          onChange={ handleChange('cpassword') }
	        />
	        <CardActions style={spaceBetweenStyle}>
		        <Button size="medium" variant="outlined" color="primary" onClick={backToSignin}>
		          Sign in Instead
		        </Button>
		        <Button size="large" variant="contained" color="primary" style={{marginLeft: '4vw'}} onClick={create}>
		          Next
		        </Button>
		    </CardActions>
		</div>);
	}
} 