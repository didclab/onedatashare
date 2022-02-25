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


import React, { Component } from 'react';
import PropTypes from "prop-types";
import {/*openDropboxOAuth, openGoogleDriveOAuth, openBoxOAuth,*/
		listFiles} from "../../APICalls/EndpointAPICalls";
import { globusFetchEndpoints, globusEndpointDetail, deleteEndpointId, globusEndpointActivateWeb } from "../../APICalls/globusAPICalls";
import {savedCredList, saveEndpointCred, deleteCredential } from "../../APICalls/APICalls";
import { generateURLFromPortNumber, generateURLForS3, showDisplay, s3Regions } from "../../constants";
import {showType, isOAuth} from "../../constants";
import {OAuthFunctions} from "../../APICalls/EndpointAPICalls";
import {store} from "../../App";

import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';

import Button from "@material-ui/core/Button";
import { ValidatorForm, TextValidator } from 'react-material-ui-form-validator';
import {cookies} from "../../model/reducers.js";


import Divider from '@material-ui/core/Divider';
import DataIcon from '@material-ui/icons/Laptop';
import AddIcon from '@material-ui/icons/AddToQueue';
import Modal from '@material-ui/core/Modal';
import {Dialog, DialogContent, DialogActions, DialogContentText, FormControlLabel, Grid, Accordion, AccordionSummary, AccordionDetails, MenuItem} from "@material-ui/core";

import {getCred} from "./initialize_dnd.js";

import {eventEmitter} from "../../App";

import GlobusEndpointListingComponent from "./GlobusEndpointListingComponent";
import ListItemSecondaryAction from '@material-ui/core/ListItemSecondaryAction';
import IconButton from '@material-ui/core/IconButton';
import DeleteIcon from '@material-ui/icons/Delete';



import {getType, getName, getDefaultPortFromUri, getTypeFromUri} from '../../constants.js';
import {styled} from "@material-ui/core/styles";
import Typography from "@material-ui/core/Typography";
import {CheckBox, ExpandMore} from "@material-ui/icons";



//PROGRESS: S3 can be accessed through both manual log in and clicking on the credential list. Deleting credentials of S3 also works
// FTP can only be accessed through manual log in, clicking on credential list does not work yet. Deleting credentials also does not work
// SFTP and HTTP currently cannot be accessed
// for notes on authenticating: line 490
// for notes on deleting credentials: line 465
// for notes on the credential list: line 415

export default class EndpointAuthenticateComponent extends Component {
	static propTypes = {
		loginSuccess : PropTypes.func,
		endpoint : PropTypes.object,
		history: PropTypes.array,
        credentials: PropTypes.array,
		type: PropTypes.string,
		back: PropTypes.func,
		setLoading : PropTypes.func,
		updateCredentials: PropTypes.func,
	}
	constructor(props){
		super(props);
		this.state={
			historyList: props.history,
			endpoint: props.endpoint,
			credList: props.credentials || {},
			endpointIdsList: {},
			settingAuth: false,
			settingAuthType: "",
			url: "",
			needPassword: false,
			username: "",
			password: "",
			endpointSelected: {},
			selectingEndpoint: false,
			portNum: -1,
			portNumField: true,
			//for SFTP
			rsa: "",
			pemFileName: "",
			pemFile: "",
			//
			//for deleting credential modal - NOT YET DEPLOYED
			openModal: false,
			deleteFunc: () => {}
		};

		let loginType = getType(props.endpoint)
		let endpointName = getName(props.endpoint)
		if(loginType === showType.gsiftp /*loginType === GRIDFTP_TYPE*/){
			this.endpointIdsListUpdateFromBackend();
		}else if(!isOAuth[loginType]/*loginType === FTP_TYPE || loginType === SFTP_TYPE || loginType === HTTP_TYPE*/){
		    this.historyListUpdateFromBackend(endpointName);
		}
		this.handleChange = this.handleChange.bind(this);
		this._handleError = this._handleError.bind(this);
		this.handleUrlChange = this.handleUrlChange.bind(this);
		this.getEndpointListComponentFromList = this.getEndpointListComponentFromList.bind(this);
		this.sftpFileUpload = this.sftpFileUpload.bind(this);
	}

	fieldLabelStyle = () => styled(Typography)({
		fontSize: "12px"
	})

	credentialListUpdateFromBackend = (type) => {
		this.props.setLoading(true);

		savedCredList(type, (data) =>{
			this.setState({credList: data? data.list: {}})
			this.props.updateCredentials(data);
			this.props.setLoading(false);
			}, (error) =>{
				this._handleError(error);
				this.props.setLoading(false);
		});
	}

	deleteCredentialFromLocal(cred, type){
			this.props.setLoading(true);

			let parsedCredsArr = JSON.parse(cookies.get(type));
			let filteredCredsArr = parsedCredsArr.filter((curObj)=>{
				return curObj.name !== cred.name;
			});
			if(filteredCredsArr.length === 0){
				cookies.remove(type);
			}
			else{
				cookies.set(type, JSON.stringify(filteredCredsArr));
			}

			this.setState({credList: filteredCredsArr});

			this.props.setLoading(false);
	}

	endpointIdsListUpdateFromBackend = () => {
		this.props.setLoading(true);
		globusFetchEndpoints((data) => {
			this.setState({ endpointIdsList: data });
			this.props.setLoading(false);
		}, (error) => {
			this._handleError(error);
			this.props.setLoading(false);
		});
	}

	historyListUpdateFromBackend = (endpointType) => {
		savedCredList(endpointType, (data) =>{
			/*data.list.filter((v) => { return v.indexOf(this.props.endpoint.uri) === 0 })}*/
			this.setState({historyList: data.list});
			this.props.setLoading(false);
		}, (error) => {
			this._handleError("Unable to retrieve data from backend. Try log out or wait for few minutes.");
			this.props.setLoading(false);
		});
	}



	endpointListUpdateFromBackend = () => {
		this.props.setLoading(true);

	}

	_handleError = (msg) => {
    	eventEmitter.emit("errorOccured", msg);
	}

	handleChange = name => event => {
      this.setState({
        [name]: event.target.value
      });
	};

	handleUrlChange = event => {

		let url = event.target.value;
		console.log(url);
		let portNum = this.state.portNum;

		// Count the number of colons (2nd colon means the URL contains the portnumber)
		let colonCount = (url.match(/:/g) || []).length;
		//ignore if S3 type
		if(getType(this.state.endpoint) !== showType.s3){
			url = generateURLFromPortNumber(url, portNum, false);
		}


		this.setState({
			"portNumField": colonCount<2 || (getType(this.state.endpoint) === showType.s3 /*&& colonCount<3*/),
			"url" : url,
		});

	}

	handlePortNumChange = event => {
		let portNum = event.target.value;
		let url = this.state.url;

		//ignore if S3 type
		if(getType(this.state.endpoint) !== showType.s3) {
			url = generateURLFromPortNumber(url, portNum, true);
		}

		this.setState({
			"portNum" : portNum,
			"url" : url
		});
	}

	endpointCheckin=(url, portNum, credential, callback) => {
		const {endpoint} = this.state;
		const type = showDisplay[getName(endpoint).toLowerCase()].label;
		this.props.setLoading(true);

		console.log(`Url is ${url}`);
		// if((type == showDisplay.ftp.label || type == showDisplay.sftp.label) && (url.match(/:/g) || []).length < 2)
		// {
		// 	url+=`:${portNum}`
		// 	credential.uri+=`:${portNum}`
		// 	credential.credId+=`:${portNum}`
		// }
		let endpointSet = {
			uri: url,
			login: true,
			side: this.props.endpoint.side,
			credential: credential,
			portNumber: portNum
		}

		//Check for a valid endpoint
		if(getType(this.state.endpoint) !== showType.s3 && !getTypeFromUri(endpointSet.uri)){
			this._handleError("Protocol is not understood");
		}




		saveEndpointCred(type,
			{
				uri: credential.uri,
				username: credential.name,
				secret: credential.rsa || credential.pemFile || credential.password,
				accountId: credential.credId,
			},
			(response) => {
				console.log("saved endpoint cred")
				console.log("the type is " + type);
				listFiles(url, endpointSet, null, (succ) =>
					{
						this.props.loginSuccess(endpointSet);
					},
					(error) => {
						this.props.setLoading(false);
						callback(error);
					}
				)
			},
			(error) => {
				this._handleError(error);
			});
	}

	getEndpointListComponentFromList(endpointIdsList){
		return Object.keys(endpointIdsList)
			.map((v) =>
			<ListItem button key={v} onClick={() => {
				globusEndpointDetail(endpointIdsList[v].id, (resp) => {
					this.endpointModalLogin(resp);
				}, (error) => {
					this._handleError("Unable to get detail of this endpoint");
				})
			}}>
			  <ListItemIcon>
		        <DataIcon/>
		      </ListItemIcon>
	          <ListItemText primary={endpointIdsList[v].name} secondary={endpointIdsList[v].canonical_name}/>
	          <ListItemSecondaryAction>
	            <IconButton aria-label="Delete" onClick={() => {


	            	deleteEndpointId(endpointIdsList[v].id, (accept) => {
	            		this.endpointIdsListUpdateFromBackend();
	            	}, (error) => {
	            		this._handleError("Delete Credential Failed");
	            	});
	            }}>
	              <DeleteIcon />
	            </IconButton>
	          </ListItemSecondaryAction>
	        </ListItem>
		);
	}

	getCredentialListComponentFromList(credList, type, loginType){
		const {endpoint} = this.state;
		const {loginSuccess} = this.props;
		
		if(store.getState().saveOAuthTokens){
			// If the user has opted to store tokens on ODS server
			// Note - Backend returns stored credentials as a nested JSON object
			return credList.filter(id => {
				return (!getCred().includes(`${loginType}${id}`))})
				.map((v) =>
				<ListItem button key={v}
					ContainerComponent="div"
					onClick={() => {
						const endpointSet = {
							uri: endpoint.uri,
							login: true,
							credential: {uuid: v, name: v, tokenSaved: true},
							side: endpoint.side
						}
						loginSuccess(endpointSet);
					}}
				>
					<ListItemIcon>
						<DataIcon/>
					</ListItemIcon>
					<ListItemText primary={v} />
					<ListItemSecondaryAction>
						<IconButton aria-label="Delete" onClick={() => {
							let endPointType = Object.keys(showType).find(key => showType[key] === this.state.endpoint.uri)
							deleteCredential(endPointType, v,(accept) => {
								this.credentialListUpdateFromBackend(type);
							}, (error) => {
								this._handleError("Delete Credential Failed");
							});
						}}>
							<DeleteIcon />
						</IconButton>
					</ListItemSecondaryAction>
				</ListItem>
			);
		}
		else{
			// If the user has opted not to store tokens on ODS server
			// Note - Local storage returns credentials as array of objects
			return credList.map((cred) =>
			<ListItem button ContainerComponent="div" onClick={() => {
					const endpointSet = {
						uri: endpoint.uri,
						login: true,
						credential: {name: cred.name, tokenSaved: false, token: cred.token},
						side: endpoint.side,
						oauth: true
					}
					loginSuccess(endpointSet);
				}}>
				<ListItemIcon>
					<DataIcon/>
				</ListItemIcon>
				<ListItemText primary={cred.name} />
				<ListItemSecondaryAction>
					<IconButton aria-label="Delete" onClick={() => {

						this.deleteCredentialFromLocal(cred, type)
						}
					}
					>
						<DeleteIcon />
					</IconButton>
				</ListItemSecondaryAction>
				</ListItem>
			);
		}

	}


   //for credential list for ftp,sftp,http, and S3. Currently only S3 is fully functional. Combination of conditionals may be possible in the future.
	getHistoryListComponentFromList(historyList){
		return historyList.map((uri) =>
			<ListItem button key={uri} ContainerComponent="div" onClick={() => {
				if(showDisplay[getName(this.state.endpoint).toLowerCase()].label === showDisplay.s3.label){

					const region = uri.split(":::")[1];

					let endpointSet = {
						uri: uri,
						login: true,
						side: this.props.endpoint.side,
						credential: {name: "\"\"", credId: uri, type: showType.s3},
						portNumber: region
					}

					listFiles(uri, endpointSet, null, (succ) =>
						{
							this.props.loginSuccess(endpointSet);
						},
						(error) => {
							this.props.setLoading(false);
							this._handleError("Please enter your credential.");
							this.setState({url: uri, authFunction : this.regularSignIn, settingAuth: true, needPassword: true, portNum: region});
						}
					)


				} else if (showDisplay[getName(this.state.endpoint).toLowerCase()].label === showDisplay.vfs.label) {
					this.vfsTypeHandler({ uri })
				} else{
					
					let portValue = getDefaultPortFromUri(uri);
					let myPoint = this.state.endpoint

					let endpointSet = {
						uri: myPoint.uri,
						login: true,
						side: this.props.endpoint.side,
						credential: {name: "\"\"", credId: uri, type: getName(this.state.endpoint).toLowerCase()},
						portNumber: portValue
					}

					listFiles(uri, endpointSet, null, (succ) =>
						{
							this.props.loginSuccess(endpointSet);
						},
						(error) => {
							this.props.setLoading(false);
							this._handleError("Please enter your credential.");
							this.setState({url: uri, authFunction : this.regularSignIn, settingAuth: true, needPassword: true, portNum: portValue});
						}
					)

				}

			}}>
			  <ListItemIcon>
		        <DataIcon/>
		      </ListItemIcon>
	          <ListItemText primary={uri}/>
	          <ListItemSecondaryAction>
	            <IconButton aria-label="Delete" onClick={() => {


	            	//Currently there is separate conditionals for S3 and non-S3 services, but it is possible that they can be combined in the future.
					// Work has not yet started on testing credential deletion on non-S3 services
					let endPointType = Object.keys(showType).find(key => showType[key] === this.state.endpoint.uri)
					if(showDisplay[getName(this.state.endpoint).toLowerCase()].label === showDisplay.s3.label){
						deleteCredential(endPointType, uri, true, (accept) => {
							this.historyListUpdateFromBackend(endPointType);
						}, (error) => {
							this._handleError("Delete History Failed");
						});
					}else{
						deleteCredential(endPointType, uri, false, (accept) => {
							this.historyListUpdateFromBackend(endPointType);
						}, (error) => {
							this._handleError("Delete History Failed");
						});
					}

	            }}>
	              <DeleteIcon />
	            </IconButton>
	          </ListItemSecondaryAction>
	        </ListItem>
		);
	}


	//For signing in for FTP, SFTP, HTTP, and S3
	//NOTE: S3 is fully functional with signing in using manual login and signing in through history credential list
	// FTP only functional through signing in using the manual login.
	// SFTP and HTTP not functional yet
	regularSignIn = () => {
		const {url, username, password} = this.state;
		const loginType = getType(this.state.endpoint);
		if((url.substr(url.length - 3) === '://' && loginType !== showType.s3) || (url.length < 1 && this.state.portNum < 1)) {
			loginType !== showType.s3 ? this._handleError("Please enter a valid URL") : this._handleError("Please enter a valid bucketname and region")
			return;
		}
		// User is expected to enter password to login
		if(username.length === 0 || (loginType !== showType.sftp && password.length === 0)) {
			this._handleError(loginType !== showType.s3 ? "Enter a username or password" : "Enter an access key or secret key");
			return;
		}


		//S3 has different URL combination. It will be <region>:::<bucketname>
		if(loginType === showType.s3){
			let combinedUrl = generateURLForS3(url, this.state.portNum);
			const credId = combinedUrl.toString();
			console.log(combinedUrl);
			this.endpointCheckin(combinedUrl,
				this.state.portNum,
				{type: loginType, credId: credId, name: username, password: password, encryptedSecret: "", uri: combinedUrl},
				() => {
					this._handleError("Authentication Failed");
				}
			);


			return;
		}

		// Encrypting user password
		const credId = username+"@"+ url.toString().split("://")[1];


		this.endpointCheckin(url,
			this.state.portNum,
			{type: loginType, credId: credId, name: username, password: password,
				rsa: this.state.rsa, pemFile: this.state.pemFile, uri: url.toString()},
			() => {
				this._handleError("Authentication Failed");
			}
		);
		
	// }
	}

	globusSignIn = () => {
		const { needPassword } = this.state;
		
		if(!needPassword){
    		this.endpointCheckin(this.state.url, this.state.portNum, {}, () => {
    			this.setState({needPassword: true});
    		});
    	}else{
    		this.endpointCheckin(this.state.url, this.state.portNum,{type: "userinfo", username: this.state.username, password: this.state.password}, (msg) => {
    			this._handleError("Authentication Failed");
    		});
    	}
	}

	sftpFileUpload = (event) => {
		const file = event.target.files[0];
		const reader = new FileReader();
		let fileContents = "";
		reader.onload = (event) => {
			fileContents = event.target.result;
			this.setState({
					pemFile: fileContents,
					pemFileName: file.name
				});
		}
		if(file.name.length > 0){
			reader.readAsText(file);
		}
	}


	endpointModalAdd = (endpoint) => {
		this.props.setLoading(true);
		globusFetchEndpoints((data) => {
			this.setState({ endpointIdsList: data });
			this.endpointModalLogin(endpoint);
			this.props.setLoading(false);
		}, (error) => {
			this._handleError(error);
			this.props.setLoading(false);
		});
		
	};

	endpointModalLogin = (endpoint) => {
		if(endpoint.activated === "false"){
			eventEmitter.emit("messageOccured", "Please activate your globus endpoint using credential on the new tab");
			globusEndpointActivateWeb(endpoint.id);
			// this.setState({settingAuth: true, authFunction : this.globusActivateSignin, needPassword: true, endpointSelected: endpoint, selectingEndpoint: false});
		}else{
			this.setState({selectingEndpoint: false});
			this.endpointCheckin("gsiftp:///", this.state.portNum, {type: "globus", globusEndpoint: endpoint}, (msg) => {
				
    			this._handleError("Authentication Failed");
    		});
		}
	}

	handleClick = (e) => {
		this.inputElement.click();
	}

	stepButton = () => styled(Button)({
		width: "100%",
	})

	vfsTypeHandler = ({ uri }) => {
		let portValue = getDefaultPortFromUri(uri);
		let myPoint = this.state.endpoint
		let endpointSet = {
			uri: myPoint.uri,
			login: true,
			side: this.props.endpoint.side,
			credential: {name: "\"\"", credId: uri, type: getName(this.state.endpoint).toLowerCase()},
			portNumber: portValue
		}
		this.props.loginSuccess(endpointSet);
	}




	//delete confirmation modal for deleting a credential - NOT YET DEPLOYED

	deleteConfirmationModal = () => {
		const handleClose = () => {
			this.setState({openModal: false, deleteFunc: ()=>{}});
		}
		const confirm = () => {
			this.state.deleteFunc();
			handleClose();
		}
		const deny = () => {
			handleClose();
		}
		const savePref = () => {
			localStorage.setItem('hideConfirm', "true");
		}

		return(
			<Dialog
			open={this.state.openModal}
			onClose={handleClose}
			>
				<DialogContent>
					<DialogContentText>
						Are you sure you want to delete this account from the list?
					</DialogContentText>
				</DialogContent>
				<DialogActions>
					<FormControlLabel
						control={<CheckBox
							onChange={savePref}
							checked={Boolean(localStorage.getItem('hideConfirm'))}
						/>}
						label={"Don't show again"}
					/>
					<Button onClick={deny}>
						Cancel
					</Button>
					<Button onClick={confirm}>
						Delete
					</Button>
				</DialogActions>
			</Dialog>
		);
	}


	render(){
		const { historyList, endpoint, credList, settingAuth, authFunction, needPassword, endpointIdsList, selectingEndpoint } = this.state;
		const { back } = this.props;
		
		const type = getName(endpoint);
		const loginType = getType(endpoint);

		const endpointModalClose = () => {this.setState({selectingEndpoint: false})};
		const StepButton = this.stepButton();



		return(
		<div >
			{!settingAuth && <div className={"authenticationContainer"}>


		        <ListItem id={endpoint.side+"Add"} button onClick={() => {
					if(isOAuth[loginType] && loginType !== showType.gsiftp){ //check if OAuth protocol
						OAuthFunctions[loginType]();
					}else if(loginType === showType.gsiftp){ //check if globus protocol
						this.setState({selectingEndpoint: true, authFunction : this.globusSignIn});
					}else{
						let loginUri = getType(endpoint) === showType.s3 ? "" : loginType;
						this.setState({settingAuth: true, authFunction : this.regularSignIn,
							needPassword: false, url: loginUri, portNum: getDefaultPortFromUri(loginUri)});
					}

		        }}>
		          <ListItemIcon>
		          	<AddIcon/>
		          </ListItemIcon>
		          <ListItemText primary={"Add New " + showDisplay[type.toLowerCase()].label} />
		        </ListItem>
		        <Divider />
				{/* Google Drive, Dropbox, Box login handler */}
				{(isOAuth[loginType] && loginType !== showType.gsiftp) && this.getCredentialListComponentFromList(credList, type, loginType)}
				{/* GridFTP OAuth handler */}
				{loginType === showType.gsiftp && this.getEndpointListComponentFromList(endpointIdsList)}
				{/* Other login handlers*/}
				{!isOAuth[loginType] && historyList &&
		        	this.getHistoryListComponentFromList(historyList)}
		        	<Grid container justifyContent={"space-between"} spacing={2} style={{padding: "3%"}}>
						<Grid item md={6} xs={12}>
							<StepButton
								id={endpoint.side + "LoginAuth"}
								ref={input => this.inputElement = input}
								// style={{marginTop: "1.5%"}}
								onClick={back}
								color="primary"
								variant="contained">
								Back
							</StepButton>
						</Grid>

					</Grid>

		    </div>

			}
	    	<Modal
	    	  aria-labelledby="simple-modal-title"
	          aria-describedby="To Select globus endpoints"
	          open={selectingEndpoint}
	          onClose={endpointModalClose}
	          style={{display: "flex", alignItems: "center", justifyContent: "center", alignSelf: "center"}}
	    	>
		    	<GlobusEndpointListingComponent close={endpointModalClose} endpointAdded={this.endpointModalAdd}/>
        	</Modal>
		    {settingAuth &&

		    	<div className={"authenticationContainer"}>


					<div style={{ paddingLeft: '3%', paddingRight: '3%' }}>

						<ValidatorForm
							ref="form"
							onError={errors => console.log(errors)}>

							<TextValidator
								required
								style={{width: "100%"}}
								id={endpoint.side+"LoginUsername"}
								label={loginType === showType.s3 ? "AWS ACCESS KEY" : "Username"}
								value={this.state.username}
								onChange={this.handleChange('username')}
								margin="normal"
								variant="outlined"
								autoFocus={(this.state.url !== 'sftp://') }
								onKeyPress={(e) => {
									if (e.key === 'Enter') {
										this.handleClick()
									}
								}}
							/>

							<TextValidator
								required={loginType !== showType.sftp}
								style={{width: "100%"}}
								id={endpoint.side+"LoginPassword"}
								label={loginType === showType.s3 ? "AWS SECRET KEY" : "Password"}
								type="password"
								value={this.state.password}
								onChange={this.handleChange('password')}
								margin="normal"
								variant="outlined"
								onKeyPress={(e) => {
									if (e.key === 'Enter') {
										this.handleClick()
									}
								}}
							/>

						</ValidatorForm>
					</div>


					{
						loginType === showType.sftp &&
							<Accordion>
								<AccordionSummary
									expandIcon={<ExpandMore />}
									aria-controls="panel1a-content"
									id="panel1a-header"
								>
									Enter RSA or DSA Key (Optional)
								</AccordionSummary>
								<AccordionDetails>
								<div style={{ paddingLeft: '3%', paddingRight: '3%', width: "100%" }}>
									<ValidatorForm
										ref="form"
										onError={errors => console.log(errors)}>
										<TextValidator
											style={{width: "100%"}}
											id={endpoint.side+"SFTP_RSA"}
											label="RSA or DSA Secret"
											value={this.state.rsa}
											onChange={this.handleChange('rsa')}
											margin="normal"
											variant="outlined"
											onKeyPress={(e) => {
												if (e.key === 'Enter') {
													this.handleClick()
												}
											}}
										/>
									</ValidatorForm>

									<span>
										<Button
											variant={"contained"}
											component={"label"}
											style={{marginBottom: "1%"}}
										>
										Upload PEM File
										<input
											type={"file"}
											accept={".pem"}
											style={{display: "none"}}
											onChange={(e)=>this.sftpFileUpload(e)}
										/>
										</Button>
										<span style={{marginLeft: "1%"}}>
											{this.state.pemFileName}
										</span>
									</span>

								</div>
								</AccordionDetails>
							</Accordion>

					}

		    	{loginType !== showType.gsiftp &&
		    		<div style={{ paddingLeft: '3%', paddingRight: '3%' }}>
							<ValidatorForm
								ref="form"
								onSubmit={authFunction}
								onError={errors => console.log(errors)}>

			    		<TextValidator
								required
					  		style={{width: "80%"}}
			          id={endpoint.side+"LoginURI"}
					  disabled = {needPassword}
			          label={ loginType === showType.s3 ? "Bucketname" : "Url"}
			          value={this.state.url}
			          onChange={this.handleUrlChange}
			          margin="normal"
								InputProps={{
									disableUnderline: true
								}}
					  		variant="outlined"
					  		autoFocus={true}
					  		onKeyPress={(e) => {
									if (e.key === 'Enter') {
										// authFunction()
										this.handleClick()
						  		}
					  		}}
			        />

			        <TextValidator
								required
			    	  	style={{width: "20%", background: this.state.portNumField? "white" : "#D3D3D3"}}
								select={loginType === showType.s3}
					  		id={endpoint.side+"LoginPort"}
					  		disabled = {!this.state.portNumField || needPassword}
			          label={ loginType === showType.s3 ? "Region" : "Port Num."}
			          value={this.state.portNumField? this.state.portNum : "-"}
			          onChange={this.handlePortNumChange}
			          margin="normal"
					  		variant="outlined"
					  		onKeyPress={(e) => {
								if (e.key === 'Enter') {
									this.handleClick()
									}
								}}
					>{loginType === showType.s3 &&
					s3Regions.map((region) => {
						return(<MenuItem value={region}>{region}</MenuItem>)
						})
					}</TextValidator>
							</ValidatorForm>
			        </div>
		    	}
		    	<Grid container justifyContent={"space-between"} spacing={2} style={{padding: "3%"}}>
					<Grid item md={6} xs={12}>
					<StepButton
						id={endpoint.side + "LoginAuth"}
						ref={input => this.inputElement = input}
						onClick={() => {
							if(needPassword){
								this.setState({needPassword: false})
							}else{
								this.setState({settingAuth: false})}
						}

						}
						color="primary"
						variant="contained">
						Back
					</StepButton>
					</Grid>

					<Grid item md={6} xs={12}>
					<StepButton
						id={endpoint.side + "LoginAuth"}
						ref={input => this.inputElement = input}
						onClick={authFunction}
						color="primary"
						variant="contained">
						Next
					</StepButton>
					</Grid>
				</Grid>
		    	</div>

		    }
      	</div>);
	}
}
