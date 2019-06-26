import React, {Component} from 'react';

import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import LinearProgress from '@material-ui/core/LinearProgress';

import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';


import Button from '@material-ui/core/Button';
import CardActions from '@material-ui/core/CardActions';

import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Divider from '@material-ui/core/Divider';

import  { Redirect } from 'react-router-dom';
import {transferPageUrl, userPageUrl} from "../../constants";

import { ValidatorForm, TextValidator } from 'react-material-ui-form-validator';
import {changePassword, getUser} from '../../APICalls/APICalls';
import {eventEmitter, store} from '../../App.js';

import { updateHashAction } from '../../model/actions';



export default class UserAccountComponent extends Component{

	constructor(){
		super();
		this.state = {
    		isSmall: window.innerWidth <= 640,
    		loading: true,
    		oldPassword: "",
    		newPassword: "",
    		conformNewPassword: "",
    	    userEmail: store.getState().email,
    	    userOrganization: "...",
    	    fName: "...",
    	    lName: "...",
    	    redirect: false
    	};
    	getUser(this.state.userEmail,  (resp) => {
            //success
            this.setState({
               userOrganization: resp.organization,
               fName: resp.firstName,
               lName: resp.lastName,
               loading: false
            });
            console.log(resp)
            }, (resp) => {
            //failed
            this.setState({loading: false})
            console.log('Error encountered in getUser request to API layer');
        });
   		this.getInnerCard = this.getInnerCard.bind(this);
   		this.onPasswordUpdate = this.onPasswordUpdate.bind(this);
   		this.accountDetails = this.accountDetails.bind(this);
	}

	onPasswordUpdate(oldPass, newPass, confPass){
		changePassword(oldPass, newPass,confPass, (hash)=>{
		    store.dispatch(updateHashAction(hash))
			this.setState({redirect:true});
			console.log(hash);
		}, (error)=>{
			if(error && error.response && error.response.data && error.response.data.message){
				eventEmitter.emit("errorOccured", error.response.data.message); 
			}else{
				eventEmitter.emit("errorOccured", "Unknown Error"); 
			}
		})
	}

	accountDetails() {
    		return(
                  <div>
                      <List>
    		          <Card style={{minWidth: 275}}>
                       <CardContent>
                       <Typography style={{fontSize: "1.6em", marginBottom: "0.6em"}}>
                          Account Details <br/>
                        </Typography>



                       <ListItem>
                       <ListItemText classes={{primary:"userDescThemeFont", secondary: "userDescValueFont"}}
                        primary="Email"
                        secondary= {this.state.userEmail}
                         />

                        <Divider/>
                        <ListItemText  classes={{primary:"userDescThemeFont", secondary: "userDescValueFont"}}
                        primary="First Name"
                        secondary= {this.state.fName} />
                        <Divider/>
                         <ListItemText  classes={{primary:"userDescThemeFont", secondary: "userDescValueFont"}}
                          primary="Last Name"
                          secondary= {this.state.lName} />
                        <Divider/>
                         <ListItemText  classes={{primary:"userDescThemeFont", secondary: "userDescValueFont"}}
                         primary="Organization"
                         secondary= {this.state.userOrganization} />
                        </ListItem>


    		           </CardContent>
    		          </Card>



                       </List>

                    <br/>

    		      </div>


    		);
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
		const {isSmall, loading, redirect} = this.state;
		const height = window.innerHeight+"px";
		return(<div style={{display: 'flex', justifyContent: 'center', alignItems: 'center', width: '..', height: height}}>
		    <div style={{width: '450px', marginTop: '30px', marginLeft: '30px',marginRight: '30px', alignSelf:  isSmall ? 'flex-start': 'center'}}>
		    
		    {loading && <LinearProgress/>}

            {this.accountDetails()}

		    {isSmall &&
		    	this.getInnerCard() 
		    }


		    {!isSmall &&
		      <Card>
		      	<CardContent style={{padding: '3em'}}>
		      		{this.getInnerCard()}
		      	</CardContent>
		      </Card>
		  	}

		  	{redirect && <Redirect from={userPageUrl} to={transferPageUrl}></Redirect>}

		    </div>
		</div>);

	}
}