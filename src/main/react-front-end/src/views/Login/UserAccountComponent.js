import React, { Component } from "react";

import Card from "@material-ui/core/Card";
import CardContent from "@material-ui/core/CardContent";
import LinearProgress from "@material-ui/core/LinearProgress";

import Typography from "@material-ui/core/Typography";
import TextField from "@material-ui/core/TextField";

import {
	Dialog,
	DialogActions,
	DialogContent,
	DialogContentText,
	DialogTitle
} from "@material-ui/core";
import Button from "@material-ui/core/Button";
import CardActions from "@material-ui/core/CardActions";

import List from "@material-ui/core/List";
import ListItem from "@material-ui/core/ListItem";
import ListItemText from "@material-ui/core/ListItemText";
import Divider from "@material-ui/core/Divider";

import FormGroup from "@material-ui/core/FormGroup";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import Switch from "@material-ui/core/Switch";

import { Redirect } from "react-router-dom";
import { transferPageUrl, userPageUrl } from "../../constants";

import {
	changePassword,
	getUser,
	updateSaveOAuth,
	saveOAuthCredentials
} from "../../APICalls/APICalls";
import { eventEmitter, store } from "../../App.js";

import {
	updateHashAction,
	accountPreferenceToggledAction,
} from "../../model/actions";
import { cookies } from "../../model/reducers";
import { DROPBOX_NAME, GOOGLEDRIVE_NAME } from "../../constants";

import {updateGAPageView} from '../../analytics/ga'

export default class UserAccountComponent extends Component {
	constructor() {
		super();
		this.state = {
    		isSmall: window.innerWidth <= 640,
    		loading: true,
    		oldPassword: "",
    		newPassword: "",
    		confirmNewPassword: "",
    	    userEmail: store.getState().email,
    	    userOrganization: "...",
    	    fName: "...",
    	    lName: "...",
    	    redirect: false,
			openAlertDialog: false,
			saveOAuthTokens: false
    	};
    	getUser(this.state.userEmail,  (resp) => {
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
   		this.getInnerCard = this.getInnerCard.bind(this);
   		this.onPasswordUpdate = this.onPasswordUpdate.bind(this);
		this.accountDetails = this.accountDetails.bind(this);
		this.handleAccountPreferenceToggle = this.handleAccountPreferenceToggle.bind(this);
		this.handleAlertClose = this.handleAlertClose.bind(this);
		this.handleAlertCloseYes = this.handleAlertCloseYes.bind(this);
		updateGAPageView();
	}

	componentDidMount(){
		document.title = "OneDataShare - Account";
		window.addEventListener("resize", this.resize.bind(this));
		this.resize();

	}

	onPasswordUpdate(oldPass, newPass, confPass){

		if(newPass.length < 5 || oldPass.length < 5 || confPass.length < 5){
		    eventEmitter.emit("errorOccured", "Password must have a minimum of 6 characters.");
		    }
	     else if(newPass === "" || oldPass === "" || confPass === ""){
			eventEmitter.emit("errorOccured", "Password fields cannot be empty");
			}
		else if(newPass !== confPass){
			eventEmitter.emit("errorOccured", "New Password and Confirmation do not match");
		}
		else{
			changePassword(oldPass, newPass,confPass, (hash)=>{
				store.dispatch(updateHashAction(hash));
				this.setState({redirect:true});

			}, (error)=>{

				if( error && error.response && error.response.data && error.response.data.message ){
					eventEmitter.emit("errorOccured", error.response.data.message);
				}else{
					eventEmitter.emit("errorOccured", "Unknown Error");
				}
			});
		}
	}

	handleAccountPreferenceToggle() {
		this.setState({ openAlertDialog: true });
	}

	handleAlertClose() {
		this.setState({ openAlertDialog: false });
	}

	handleAlertCloseYes() {
		this.handleAlertClose();
		let currentSaveStatus = this.state.saveOAuthTokens;

		// Toggle the change
		currentSaveStatus = !currentSaveStatus;
		updateSaveOAuth(this.state.email, currentSaveStatus, () => {
			store.dispatch(accountPreferenceToggledAction(currentSaveStatus));
			if (currentSaveStatus) {
				// if the user opted to switch from saving tokens on browser to
				// storing tokens on the server, we clear all saved tokens in the current browser session.
				let credentials = []
				if(!(typeof cookies.get(GOOGLEDRIVE_NAME) == "undefined")){
					var googleDriveCredentials = JSON.parse(cookies.get(GOOGLEDRIVE_NAME));
					googleDriveCredentials.forEach(function(element){
						element.name = "GoogleDrive: " + element.name;
					});
					credentials.push(...googleDriveCredentials);
				}
				if(!(typeof cookies.get(DROPBOX_NAME) == "undefined")){
					var dropBoxCredentials = JSON.parse(cookies.get(DROPBOX_NAME));
					dropBoxCredentials.forEach(function(element){
						element.name = "Dropbox: " + element.name;
					});
					credentials.push(...dropBoxCredentials);
				}
				saveOAuthCredentials(credentials, (success)=>{console.log("Credentials saved Successfully")}, (error)=>{
					console.log("Error in saving credentials", error);
					eventEmitter.emit("errorOccured", "Error in saving credentials. You might have to re-authenticate your accounts" );
				});
				cookies.remove(DROPBOX_NAME);
				cookies.remove(GOOGLEDRIVE_NAME);

			}
			//Update the variables
			this.setState({ saveOAuthTokens: currentSaveStatus });
		});
	}

	accountDetails() {
		return (
			<div>
				<List>
					<Card style={{ minWidth: 275 }}>
						<CardContent>
							<Typography style={{ fontSize: "1.6em", marginBottom: "0.6em" }}>
								Account Details <br />
							</Typography>

							<ListItem>
								<ListItemText
									classes={{
										primary: "userDescThemeFont",
										secondary: "userDescValueFont"
									}}
									primary="Email"
                        			id="UserEmail"
									secondary={this.state.userEmail}
								/>

								<Divider />
								<ListItemText
									classes={{
										primary: "userDescThemeFont",
										secondary: "userDescValueFont"
									}}
									primary="First Name"
                        			id="UserFirstName"
									secondary={this.state.fName}
								/>
								<Divider />
								<ListItemText
									classes={{
										primary: "userDescThemeFont",
										secondary: "userDescValueFont"
									}}
									primary="Last Name"
                          			id="UserLastName"
									secondary={this.state.lName}
								/>
								<Divider />
								<ListItemText
									classes={{
										primary: "userDescThemeFont",
										secondary: "userDescValueFont"
									}}
									primary="Organization"
									id="UserOrganization"
									secondary={this.state.userOrganization}
								/>
							</ListItem>
						</CardContent>
					</Card>
				</List>

				<br />

				<List>
					<Card style={{ minWidth: 275 }}>
						<CardContent>
							<Typography style={{ fontSize: "1.6em", marginBottom: "0.6em" }}>
								Account Preferences <br />
							</Typography>
							<FormGroup>
								<FormControlLabel
									value="new_source"
									control={
										<Switch
											checked={this.state.saveOAuthTokens}
											onClick={() => this.handleAccountPreferenceToggle()}
											value="saveOAuthTokenSwitch"
											color="primary"
										/>
									}
									label={"Save OAuth tokens"}
								/>
							</FormGroup>
						</CardContent>
					</Card>
				</List>
				<Dialog
					open={this.state.openAlertDialog}
					onClose={this.handleAlertClose}
					aria-labelledby="alert-dialog-title"
					aria-describedby="alert-dialog-description"
				>
					<DialogTitle id="alert-dialog-title">
						{"Change how OAuth tokens are saved?"}
					</DialogTitle>
					<DialogContent>
						<DialogContentText id="alert-dialog-description">
							Warning! This might delete all your existing credentials and may
							interrupt ongoing transfers. Are you sure?
            </DialogContentText>
					</DialogContent>
					<DialogActions>
						<Button onClick={this.handleAlertCloseYes} color="primary">
							Yes
            </Button>
						<Button onClick={this.handleAlertClose} color="primary" autoFocus>
							No
            </Button>
					</DialogActions>
				</Dialog>
			</div>
		);
	}

	getInnerCard() {
		const handleChange = name => event => {
			this.setState({
				[name]: event.target.value
			});
		};

		let confirmed = this.state.newPassword !== this.state.confirmNewPassword;
		return (
			<div>
				<Typography style={{ fontSize: "1.6em", marginBottom: "0.6em" }}>
					Change your Password
        </Typography>

				<TextField
					id="Email"
					label="Enter Your Old Password"
					type="password"
					value={this.state.oldPassword}
					style={{ width: "100%", marginBottom: "1em" }}
					onChange={handleChange("oldPassword")}
				/>
				<TextField
					label="Enter Your New Password"
					type="password"
					value={this.state.newPassword}
					style={{ width: "100%", marginBottom: "1em" }}
					onChange={handleChange("newPassword")}
				/>
				<TextField
					error={confirmed}
					id="Cpassword"
					type="password"
					label="Confirm Your New Password"
					value={this.state.confirmNewPassword}
					style={{ width: "100%", marginBottom: "2em" }}
					onChange={handleChange("confirmNewPassword")}
				/>

				<CardActions style={{ marginBottom: "0px" }}>
					<Button
						size="small"
						color="primary"
						style={{ width: "100%" }}
						onClick={() =>
							this.onPasswordUpdate(
								this.state.oldPassword,
								this.state.newPassword,
								this.state.confirmNewPassword
							)
						}
					>
						Proceed with password Change
          </Button>
				</CardActions>
			</div>
		);
	}

	resize() {
		if (this.state.isSmall && window.innerWidth > 640) {
			this.setState({ isSmall: false });
		} else if (!this.state.isSmall && window.innerWidth <= 640) {
			this.setState({ isSmall: true });
		}
	}

	render() {
		const { isSmall, loading, redirect } = this.state;
		const height = window.innerHeight + "px";
		return (
			<div
				style={{
					display: "flex",
					justifyContent: "center",
					alignItems: "center",
					width: "..",
					height: height
				}}
			>
				<div
					style={{
						width: "450px",
						marginTop: "30px",
						marginLeft: "30px",
						marginRight: "30px",
						alignSelf: isSmall ? "flex-start" : "center"
					}}
				>
					{loading && <LinearProgress />}

					{this.accountDetails()}

					{isSmall && this.getInnerCard()}

					{!isSmall && (
						<Card>
							<CardContent style={{ padding: "3em" }}>
								{this.getInnerCard()}
							</CardContent>
						</Card>
					)}

					{redirect && (
						<Redirect from={userPageUrl} to={transferPageUrl}></Redirect>
					)}
				</div>
			</div>
		);
	}
}
