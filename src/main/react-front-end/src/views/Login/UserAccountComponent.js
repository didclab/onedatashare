import React, {Component} from 'react';

import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import LinearProgress from '@material-ui/core/LinearProgress';

import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';


import Button from '@material-ui/core/Button';
import CardActions from '@material-ui/core/CardActions';


import { ValidatorForm, TextValidator } from 'react-material-ui-form-validator';
import {changePassword} from '../../APICalls/APICalls';
import {eventEmitter} from '../../App.js';
export default class UserAccountComponent extends Component{

	constructor(){
		super();
		this.state = {
    		isSmall: window.innerWidth <= 640,
    		loading: true,
    		oldPassword: "",
    		newPassword: "",
    		conformNewPassword: "",

    	};
   		this.getInnerCard = this.getInnerCard.bind(this);
   		this.onPasswordUpdate = this.onPasswordUpdate.bind(this);
	}

	onPasswordUpdate(oldPass, newPass, confPass){
		changePassword(oldPass, newPass,confPass, (response)=>{
			console.log(response);
		}, (error)=>{
			if(error && error.response && error.response.data && error.response.data.message){
				eventEmitter.emit("errorOccured", error.response.data.message); 
			}else{
				eventEmitter.emit("errorOccured", "Unknown Error"); 
			}
		})
	}
	getInnerCard() {
		const handleChange = name => event => {
		    this.setState({
		      [name]: event.target.value,
		    });
		};
		let confirmed = (this.state.newPassword !== this.state.conformNewPassword);
		return(
			<div>
				<Typography style={{fontSize: "1.6em", marginBottom: "0.6em"}}>
		          Change your Password
		        </Typography>

			        <TextField
			          id="Email"
			          label="Enter Your Old Password"
			          type="password"
			          value={this.state.oldPassword}
			          style={{width: '100%', marginBottom: '1em'}}
			          onChange={ handleChange('oldPassword') }
			        />
			        <TextField
			          id="Password"
			          label="Enter Your New Password"
			          type="password"
			          value={this.state.newPassword}
			          style={{width: '100%', marginBottom: '1em'}}
			          onChange={ handleChange('newPassword') }
			        />
			        <TextField
			          error={confirmed}
			          id="Cpassword"
			          type="password"
			          label="Confirm Your New Password"
			          value={this.state.conformNewPassword}
			          style={{width: '100%', marginBottom: '2em'}}
			          onChange={ handleChange('conformNewPassword') }
			        />
			    <CardActions style={{marginBottom: '0px'}}>
			        
			        <Button size="small" color="primary" style={{width: '100%'}}
			        	onClick={()=>this.onPasswordUpdate(this.state.oldPassword, this.state.newPassword, this.state.conformNewPassword)}>
			          Proceed with password Change
			        </Button>
			    </CardActions>
	        </div>
		);
	}

	componentDidMount(){
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

	render(){
		const {isSmall, loading} = this.state;
		const height = window.innerHeight+"px";
		return(<div style={{display: 'flex', justifyContent: 'center', alignItems: 'center', width: '..', height: height}}>
		    <div style={{width: '450px', marginTop: '30px', marginLeft: '30px',marginRight: '30px', alignSelf:  isSmall ? 'flex-start': 'center'}}>
		    
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
		</div>);
	}
}