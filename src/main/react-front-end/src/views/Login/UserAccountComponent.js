/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


import React, { Component } from "react";

import Card from "@material-ui/core/Card";
import CardContent from "@material-ui/core/CardContent";
import LinearProgress from "@material-ui/core/LinearProgress";
import PasswordRequirementsComponent from '../Login/PasswordRequirementsComponent'
import Typography from "@material-ui/core/Typography";
import TextField from "@material-ui/core/TextField";

import './UserAccountComponent.css';

import Button from "@material-ui/core/Button";
import CardActions from "@material-ui/core/CardActions";

import List from "@material-ui/core/List";
import ListItem from "@material-ui/core/ListItem";
import ListItemText from "@material-ui/core/ListItemText";
import Divider from "@material-ui/core/Divider";
import Dialog from '@material-ui/core/Dialog';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';

import { validPassword, spaceBetweenStyle } from "../../constants";
import AccountAttribute from "./EditUserAccountAttributes";

import {
	changePassword,
	getUser
} from "../../APICalls/APICalls";
import { eventEmitter } from "../../App.js";


import { updateGAPageView } from '../../analytics/ga'
import IconButton from '@material-ui/core/IconButton';

import VisibilityOutlinedIcon from '@material-ui/icons/VisibilityOutlined';
import VisibilityOffOutlinedIcon from '@material-ui/icons/VisibilityOffOutlined';
import InputAdornment from '@material-ui/core/InputAdornment';

import {GREY} from './../../color.js';

export default class UserAccountComponent extends Component {
	constructor() {
		super();
		this.state = {
			isSmall: window.innerWidth <= 640,
			loading: true,
			oldPassword: "",
			newPassword: "",
			confirmNewPassword: "",
			isValidNewPassword: true,
			isValidConfirmPassword: true,
			errorMsg: null,
			//userEmail: store.getState().email,
			userEmail:"placeholder@gmail.com",
			userOrganization: "placeholder",
			fName: "placeholder",
			lName: "placeholder",
			openAlertDialog: false,
			saveOAuthTokens: false,
			canSubmit: false,
			isOldPwdVisible: false,
			isNewPwdVisible: false,
			isConfirmPwdVisible: false,
			modalIsOpen: false

		};
		this.displayText = "When enabled, all your endpoint authentication tokens will be saved by OneDataShare. \
		OneDataShare does not store any passwords. On disabling this feature, your endpoint authentication tokens \
		will be saved in your browser session for a limited time and you may have to authenticate your accounts at \
		regular intervals."
		getUser(this.state.userEmail, (resp) => {
			//success
			this.setState({
				userOrganization: resp.organization,
				fName: resp.firstName,
				lName: resp.lastName,
				saveOAuthTokens: resp.saveOAuthTokens,
				loading: false
			});
		}, (resp) => {
			//failed
			this.setState({ loading: false });
			console.log('Error encountered in getUser request to API layer');
		});
		updateGAPageView();
	}

	componentDidMount() {
		document.title = "OneDataShare - Account";
		window.addEventListener("resize", this.resize.bind(this));
		this.resize();
		document.body.style.backgroundColor = GREY;

	}

	componentWillUnmount(){
		document.body.style.backgroundColor = null;
	}

	onPasswordUpdate = (oldPass, newPass, confPass) => {
		if (newPass === "" || oldPass === "" || confPass === "") {
			eventEmitter.emit("errorOccured", "Password fields cannot be empty");
		} else if (oldPass === newPass) {
			eventEmitter.emit("errorOccured", "Old and New Passwords cant be same");
		}
		else {
			changePassword(oldPass, newPass, confPass, (hash) => {
				console.debug(`Password change successful`);
			}, (error) => {
				if (error && error.response && error.response.data && error.response.data.message) {
					eventEmitter.emit("errorOccured", error.response.data.message);
				} else {
					eventEmitter.emit("errorOccured", "Unknown Error");
				}
			});
		}
	}
	

	accountDetails = () => {
		return (
			
		
		<React.Fragment>
			<Typography style={{ fontSize: "1.6em", marginBottom: "0.6em", textAlign: "center" }}>
				Account Details <br />
			</Typography>
			<List>
				<ListItem alignItems="flex-start">
					<ListItemText
						primary="Email"
						secondary={
							<AccountAttribute textDisplayed={this.state.userEmail} />
						}
					/>
				</ListItem>
				<Divider variant="middle" />
				<ListItem alignItems="flex-start">
					<ListItemText
						primary="First Name"
						secondary={
							<AccountAttribute textDisplayed={this.state.fName} editable />
						}
					/>
				</ListItem>
				<Divider variant="middle" />
				<ListItem alignItems="flex-start">
					<ListItemText
						primary="Last Name"
						secondary={
							<AccountAttribute textDisplayed={this.state.lName} editable />
						}
					/>
				</ListItem>
				<Divider variant="middle" />
				<ListItem alignItems="flex-start">
					<ListItemText
						primary="Organization"
						secondary={
							<AccountAttribute textDisplayed={this.state.userOrganization} editable />
						}
					/>
				</ListItem>
				<Divider variant="middle" />
			</List>
		</React.Fragment>);
	}


	changePasswordForm = () => {
		const handleChange = name => event => {
			this.setState({
				[name]: event.target.value
			});
		};

		const checkPassword = name => event => {
			const validObj = validPassword(name, event.target.value, this.state.confirmNewpassword);
			this.setState({ [name]: event.target.value, isValidNewPassword: validObj.isValid, errorMsg: validObj.errormsg });
		}

		const checkConfirmPassword = name => event => {
			const validObj = validPassword(name, this.state.newPassword, event.target.value);
			this.setState({ [name]: event.target.value, isValidConfirmPassword: validObj.isValid, errorMsg: validObj.errormsg });
		}

		return (
			<React.Fragment>
				<TextField
					id="Email"
					label="Enter Your Old Password"
					type={this.state.isOldPwdVisible? "text":"password"}
					value={this.state.oldPassword}
					style={{ width: "100%", marginBottom: "1em" }}
					onChange={handleChange("oldPassword")}
					InputProps={{
						endAdornment:   <React.Fragment>
											<InputAdornment position="end">
												<IconButton
													aria-label="toggle password visibility"
													onClick={()=>{this.togglePwdVisibility('old')}}
												>
												{this.state.isOldPwdVisible ? <VisibilityOutlinedIcon /> : <VisibilityOffOutlinedIcon />}
												</IconButton>
											</InputAdornment>
										</React.Fragment>
					}}
				/>
				<TextField
					error={!this.state.isValidNewPassword}
					label="Enter Your New Password"
					type={this.state.isNewPwdVisible? "text":"password"}
					value={this.state.newPassword}
					style={{ width: "100%", marginBottom: "1em" }}
					onChange={checkPassword("newPassword")}
					InputProps={{
						endAdornment: <React.Fragment>
							<InputAdornment position="end">
								<IconButton
									aria-label="toggle password visibility"
									onClick={()=>{this.togglePwdVisibility('new')}}
								>
								{this.state.isNewPwdVisible ? <VisibilityOutlinedIcon /> : <VisibilityOffOutlinedIcon />}
								</IconButton>
							</InputAdornment>
							</React.Fragment>
					}}
				/>
				<TextField
					error={!this.state.isValidConfirmPassword}
					id="Cpassword"
					type={this.state.isConfirmPwdVisible? "text":"password"}
					label="Confirm Your New Password"
					value={this.state.confirmNewPassword}
					style={{ width: "100%", marginBottom: "1em" }}
					onChange={checkConfirmPassword("confirmNewPassword")}
					InputProps={{
						endAdornment: <React.Fragment>
							<InputAdornment position="end">
								<IconButton
									aria-label="toggle password visibility"
									onClick={()=>{this.togglePwdVisibility('confirm')}}
								>
								{this.state.isConfirmPwdVisible ? <VisibilityOutlinedIcon /> : <VisibilityOffOutlinedIcon />}
								</IconButton>
							</InputAdornment>
							</React.Fragment>
					}}
				/>
				<PasswordRequirementsComponent
					showList={(!this.state.isValidNewPassword) || (!this.state.isValidConfirmPassword)}
					errorMsg={this.state.errorMsg} />
				<CardActions style={{ marginBottom: "0px" }}>
					<Button
						size="small"
						color="primary"
						onClick={this.toggleModalClose}
					>
						Cancel
					</Button>
					<Button
						size="small"
						color="primary"
						disabled={!(this.state.isValidNewPassword && this.state.isValidConfirmPassword && this.state.newPassword && this.state.confirmNewPassword)}
						onClick={() =>
							this.onPasswordUpdate(
								this.state.oldPassword,
								this.state.newPassword,
								this.state.confirmNewPassword
							)
						}
					>
						Update Password
         			</Button>
				</CardActions>
			</React.Fragment>
		);
	}

	resize() {
		if (this.state.isSmall && window.innerWidth > 640) {
			this.setState({ isSmall: false });
		} else if (!this.state.isSmall && window.innerWidth <= 640) {
			this.setState({ isSmall: true });
		}
	}

	
	togglePwdVisibility = (field) => {
		const {isOldPwdVisible, isNewPwdVisible, isConfirmPwdVisible} = this.state
		switch(field){
			case 'old':
				this.setState({isOldPwdVisible: !isOldPwdVisible});
				break;
			case 'new':
				this.setState({isNewPwdVisible:!isNewPwdVisible});
				break;
			case'confirm':
				this.setState({isConfirmPwdVisible:!isConfirmPwdVisible});
				break;
			default:
				break;
		}
	}

	toggleModalOpen = () => {
		this.setState({modalIsOpen: true});
	}

	toggleModalClose = () => {
		this.setState({modalIsOpen:false});
	}

	render() {
		const { isSmall, loading, modalIsOpen } = this.state;
		return (
			<div
				style={{
					display: "flex",
					justifyContent: "center",
					alignItems: "center",
					marginBottom: '5%'
				}}
			>
				<div
					style={{
						width: "450px",
						alignSelf: isSmall ? "flex-start" : "center"
					}}
				>
					{loading && <LinearProgress />}

					<Card className="userAccCardStyle" elevation="3">
						<CardContent>
							{this.accountDetails()}
							<div style={{width:"100%", display:"flex", justifyContent:"center"}}>
								<Button color="primary" onClick={this.toggleModalOpen}>
									Change Password
								</Button>
							</div>
						</CardContent>
					</Card>	
					<Dialog open={modalIsOpen} onClose={this.toggleModalClose} >
						<DialogTitle style={{display:"flex", justifyContent:"center"}}>Change Password</DialogTitle>
						<DialogContent>
							{this.changePasswordForm()}
						</DialogContent>
					</Dialog>
				</div>
			</div>
		);
	}
}
