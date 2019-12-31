import React, { Component } from "react";

import Card from "@material-ui/core/Card";
import CardContent from "@material-ui/core/CardContent";
import LinearProgress from "@material-ui/core/LinearProgress";
import PasswordRequirementsComponent from '../Login/PasswordRequirementsComponent'
import Typography from "@material-ui/core/Typography";
import TextField from "@material-ui/core/TextField";

import './UserAccountComponent.css';

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

import { validPassword } from "../../constants";

import {
	changePassword,
	getUser,
	updateSaveOAuth,
	saveOAuthCredentials
} from "../../APICalls/APICalls";
import { eventEmitter, store } from "../../App.js";

import {
	logoutAction,
	accountPreferenceToggledAction,
} from "../../model/actions";
import { cookies } from "../../model/reducers";
import { DROPBOX_NAME, GOOGLEDRIVE_NAME } from "../../constants";

import { updateGAPageView } from '../../analytics/ga'

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
			userEmail: store.getState().email,
			userOrganization: "...",
			fName: "...",
			lName: "...",
			openAlertDialog: false,
			saveOAuthTokens: false,
			canSubmit: false
		};
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
		this.getInnerCard = this.getInnerCard.bind(this);
		this.onPasswordUpdate = this.onPasswordUpdate.bind(this);
		this.accountDetails = this.accountDetails.bind(this);
		this.handleAccountPreferenceToggle = this.handleAccountPreferenceToggle.bind(this);
		this.handleAlertClose = this.handleAlertClose.bind(this);
		this.handleAlertCloseYes = this.handleAlertCloseYes.bind(this);
		updateGAPageView();
	}

	componentDidMount() {
		document.title = "OneDataShare - Account";
		window.addEventListener("resize", this.resize.bind(this));
		this.resize();

	}

	onPasswordUpdate(oldPass, newPass, confPass) {
		if (newPass === "" || oldPass === "" || confPass === "") {
			eventEmitter.emit("errorOccured", "Password fields cannot be empty");
		} else if (oldPass === newPass) {
			eventEmitter.emit("errorOccured", "Old and New Passwords cant be same");
		}
		else {
			changePassword(oldPass, newPass, confPass, (hash) => {
				store.dispatch(logoutAction());
			}, (error) => {
				if (error && error.response && error.response.data && error.response.data.message) {
					eventEmitter.emit("errorOccured", error.response.data.message);
				} else {
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
				if (!(typeof cookies.get(GOOGLEDRIVE_NAME) == "undefined")) {
					var googleDriveCredentials = JSON.parse(cookies.get(GOOGLEDRIVE_NAME));
					googleDriveCredentials.forEach(function (element) {
						element.name = "GoogleDrive: " + element.name;
					});
					credentials.push(...googleDriveCredentials);
				}
				if (!(typeof cookies.get(DROPBOX_NAME) == "undefined")) {
					var dropBoxCredentials = JSON.parse(cookies.get(DROPBOX_NAME));
					dropBoxCredentials.forEach(function (element) {
						element.name = "Dropbox: " + element.name;
					});
					credentials.push(...dropBoxCredentials);
				}
				saveOAuthCredentials(credentials, (success) => { console.log("Credentials saved Successfully") }, (error) => {
					console.log("Error in saving credentials", error);
					eventEmitter.emit("errorOccured", "Error in saving credentials. You might have to re-authenticate your accounts");
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
					<Card className="userAccCardStyle">
						<CardContent>
							<Typography style={{ fontSize: "1.6em", marginBottom: "0.6em", textAlign: "center" }}>
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

				<List>
					<Card className="userAccCardStyle" style={{ paddingLeft: '2em', paddingRight: '2em' }}>
						<CardContent>
							<Typography style={{ fontSize: "1.6em", marginBottom: "0.6em", textAlign: "center" }}>
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
									label={"Save endpoint authentication tokens with OneDataShare"}
								/>
							</FormGroup>
						</CardContent>
					</Card>
				</List>

				<Dialog
					open={this.state.openAlertDialog}
					onClose={this.handleAlertClose}
					aria-labelledby="alert-dialog-title"
					aria-describedby="alert-dialog-description">

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

		const checkPassword = name => event => {
			const validObj = validPassword(name, event.target.value, this.state.confirmNewpassword);
			this.setState({ [name]: event.target.value, isValidNewPassword: validObj.isValid, errorMsg: validObj.errormsg });
		}

		const checkConfirmPassword = name => event => {
			const validObj = validPassword(name, this.state.newPassword, event.target.value);
			this.setState({ [name]: event.target.value, isValidConfirmPassword: validObj.isValid, errorMsg: validObj.errormsg });
		}

		return (
			<div>
				<Typography style={{ fontSize: "1.6em", marginBottom: "0.6em", textAlign: 'center' }}>
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
					error={!this.state.isValidNewPassword}
					label="Enter Your New Password"
					type="password"
					value={this.state.newPassword}
					style={{ width: "100%", marginBottom: "1em" }}
					onChange={checkPassword("newPassword")}
				/>
				<TextField
					error={!this.state.isValidConfirmPassword}
					id="Cpassword"
					type="password"
					label="Confirm Your New Password"
					value={this.state.confirmNewPassword}
					style={{ width: "100%", marginBottom: "1em" }}
					onChange={checkConfirmPassword("confirmNewPassword")}
				/>
				<PasswordRequirementsComponent
					showList={(!this.state.isValidNewPassword) || (!this.state.isValidConfirmPassword)}
					errorMsg={this.state.errorMsg} />
				<CardActions style={{ marginBottom: "0px" }}>
					<Button
						size="small"
						color="primary"
						style={{ width: "100%" }}
						disabled={!(this.state.isValidNewPassword && this.state.isValidConfirmPassword && this.state.newPassword && this.state.confirmNewPassword)}
						variant="contained"
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
		const { isSmall, loading } = this.state;
		const height = window.innerHeight + "px";
		return (
			<div
				style={{
					display: "flex",
					justifyContent: "center",
					alignItems: "center",
					width: "..",
					height: height,
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

					{this.accountDetails()}

					<Card className="userAccCardStyle resetPasswordCard">
						{this.getInnerCard()}
					</Card>
				</div>
			</div>
		);
	}
}
