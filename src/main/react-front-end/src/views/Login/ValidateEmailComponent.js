import React, {Component} from 'react';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import CardActions from '@material-ui/core/CardActions';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';
import CreateAccountComponent from './CreateAccountComponent';
import  { Route, BrowserRouter, Switch, Redirect } from 'react-router-dom';
import {resendVerificationCode} from '../../APICalls/APICalls'
import LinearProgress from '@material-ui/core/LinearProgress';
import {eventEmitter} from '../../App';

export default class ValidateEmailComponent extends Component {
	static propTypes = {
		back: PropTypes.func,
		email: PropTypes.string
	}
	constructor(props){
	    super(props);
	    this.state = {
				email: props.email,
				loadVerifyCode:false,
        loading: false,
			}
			this.next = this.next.bind(this);
	}
	next(){
		this.setState({loading:true})
		resendVerificationCode(this.state.email).then((response) =>{
			if(response.data.status === 200){
			this.setState({loading:false, loadVerifyCode:true})
			}
			else{
				this.setState({loading:false, loadVerifyCode:false})
				eventEmitter.emit("errorOccured", response.data.response);
			}	
		});
	}

	render(){
		const { back } = this.props;
		const properties = this.props
		const { email } = this.state;
		const loadVerifyCode = this.state.loadVerifyCode;
		const handleChange = name => event => {
		    this.setState({
		      [name]: event.target.value,
		    });
		};
		
		if(loadVerifyCode){		
			return(
				<CreateAccountComponent {...properties} loadVerifyCode = {this.state.loadVerifyCode} email = {this.state.email} ></CreateAccountComponent>
			);
		}
		else{
			return (	
				<div className="enter-from-right slide-in">
					{this.state.loading && <LinearProgress></LinearProgress>}
					<Typography style={{fontSize: "1.6em", marginBottom: "0.4em"}}>
						Find your Stork Account
					</Typography>
					<TextField
						id="Email"
						label="Enter Your Email"
						value={email}
						style={{width: '100%', marginBottom: '50px', borderRadius: '25px'}}
						onChange={ handleChange('email') }
					/>
					<CardActions className="flexSpaceBetween">
						{"\n"}
						
						<Button size="small" variant="outlined" color="primary" onClick={back}>
							Back to Sign in Page
						</Button>
						<Button size="large" variant="contained" color="primary" onClick={() => this.next()} style={{marginLeft: '4vw'}}>
							Next
						</Button>
					</CardActions>
				</div>
			);
		}
	}
} 