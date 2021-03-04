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
import { deleteHistory, deleteCredentialFromServer, history, savedCredList, saveEndpointCred } from "../../APICalls/APICalls";
import {/*DROPBOX_TYPE,
				GOOGLEDRIVE_TYPE,
				BOX_TYPE,
				FTP_TYPE,
				SFTP_TYPE,
				GRIDFTP_TYPE,
				HTTP_TYPE,*/
				ODS_PUBLIC_KEY,
				generateURLFromPortNumber,
				generateURLForS3,
				showDisplay,
				s3Regions
			} from "../../constants";
import {showType, isOAuth} from "../../constants";
import {OAuthFunctions} from "../../APICalls/EndpointAPICalls";
import {store} from "../../App";

import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';

import Button from "@material-ui/core/Button";
import { ValidatorForm, TextValidator } from 'react-material-ui-form-validator';
import {cookies} from "../../model/reducers.js";

import JSEncrypt from 'jsencrypt';

import Divider from '@material-ui/core/Divider';
import DataIcon from '@material-ui/icons/Laptop';
import BackIcon from '@material-ui/icons/KeyboardArrowLeft'
import AddIcon from '@material-ui/icons/AddToQueue';
import Modal from '@material-ui/core/Modal';
import {Dialog, DialogContent, DialogActions, DialogContentText, FormControlLabel, Grid, Checkbox, Accordion, AccordionSummary, AccordionDetails, MenuItem} from "@material-ui/core";

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
export default class EndpointAuthenticateComponent extends Component {
	static propTypes = {
		loginSuccess : PropTypes.func,
		endpoint : PropTypes.object,
		history: PropTypes.array,
        credentials: PropTypes.object,
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
			rsa: "",
			pemFileName: "",
			pemFile: "",
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
		// if(window.confirm("Are you sure you want to delete this account from the list?")){
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
		// }
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
		console.log(endpointType);
		savedCredList(endpointType, (data) =>{
			console.log(data);
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
			/*"portNum": colonCount<2 && getType(this.state.endpoint) !== showType.s3 ?
							portNum : url.split(':')[2]*/
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

		let endpointSet = {
			uri: url,
			login: true,
			side: this.props.endpoint.side,
			credential: credential,
			portNumber: portNum
		}

		//Check for a valid endpoint
		if(! getTypeFromUri(endpointSet.uri) && getType(this.state.endpoint) !== showType.s3){
			this._handleError("Protocol is not understood");
		}



		// listFiles(url, endpointSet, null, (response) => {
		// 	saveEndpointCred(type,
		// 		{
		// 			uri: credential.url,
		// 			username: credential.name,
		// 			secret: credential.password,
		// 			accountId: credential.credId
		// 		},
		// 		 (suc) => {
		// 		 //console.log(suc)

		// 	}, (error) => {
		// 		this._handleError(error);
		// 	})
		// 	this.props.loginSuccess(endpointSet);
		// }, (error) => {
		// 	this.props.setLoading(false);
		// 	callback(error);
		// })

		let encryptedSecret = "";
		if(type === showDisplay.s3.label){
			encryptedSecret = credential.encryptedSecret;
		}
		console.log(credential.uri);
		saveEndpointCred(type,
			{
				uri: credential.uri,
				username: credential.name,
				secret: credential.password,
				accountId: credential.credId,
				// encryptedSecret: encryptedSecret,
				// rsa: credential.rsa,
				// pemFile: credential.pemFile
			},
			(response) => {
				console.log("saved endpoint cred")
				console.log("the type is " + type);
				listFiles(url, endpointSet,
					type === showDisplay.s3.label, null, (succ) =>
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

					// this.setState({
					// 		deleteFunc: deleteEndpointId(endpointIdsList[v].id, (accept) => {
					// 			this.endpointIdsListUpdateFromBackend();
					// 		}, (error) => {
					// 			this._handleError("Delete Credential Failed");
					// 		}),
					// 		openModal: true
					//
					// 	});

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

	getCredentialListComponentFromList(credList, type){
		const {endpoint} = this.state;
		const {loginSuccess} = this.props;
		
		if(store.getState().saveOAuthTokens){
			// If the user has opted to store tokens on ODS server
			// Note - Backend returns stored credentials as a nested JSON object
			return credList.filter(id => {
				return (!getCred().includes(id))})
				.map((v) =>
				<ListItem button key={v}
					onClick={() => {
						const endpointSet = {
							uri: endpoint.uri,
							login: true,
// 							credential: {uuid: v, name: credList[v].name, tokenSaved: true},
// 							side: endpoint.side,
							credential: {uuid: v, name: v, tokenSaved: true},
							side: endpoint.side
						}
						loginSuccess(endpointSet);
					}}
						  ContainerComponent="div"
				>
					<ListItemIcon>
						<DataIcon/>
					</ListItemIcon>
					<ListItemText primary={v} />
					<ListItemSecondaryAction>
						<IconButton aria-label="Delete" onClick={() => {
// 							deleteCredentialFromServer(v, (accept) => {
// 								this.credentialListUpdateFromBackend();
// 							this.setState({
// 								deleteFunc: () => {
// 									deleteCredentialFromServer(v, type, (accept) => {
// 										this.credentialListUpdateFromBackend(type);
// 									}, (error) => {
// 										this._handleError("Delete Credential Failed");
// 									})
// 								},
// 								openModal: true
// 							})
							deleteCredentialFromServer(v, type, (accept) => {
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
			<ListItem button onClick={() => {
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
						// this.setState({
						// 	deleteFunc: () => this.deleteCredentialFromLocal(cred, type),
						// 	openModal: true
						// })
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

	getHistoryListComponentFromList(historyList){
		return historyList.map((uri) =>
			<ListItem button key={uri} onClick={() => {
				if(showDisplay[getName(this.state.endpoint).toLowerCase()].label === showDisplay.s3.label){
					// let combinedUrl = generateURLForS3(url, this.state.portNum);
					// const credId = username+"@"+ combinedUrl.toString();
					// console.log(combinedUrl);
					// this.endpointCheckin(combinedUrl,
					// 	this.state.portNum,
					// 	{type: loginType, credId: credId, name: username, password: password, encryptedSecret: "", uri: combinedUrl},
					// 	() => {
					// 		this._handleError("Authentication Failed");
					// 	}
					// );
					const nameAndUrl = uri.split("@");
					const region = uri.split(":::")[1];


					this.endpointCheckin(nameAndUrl[1], region, {credId: uri, uri: nameAndUrl[1], name: nameAndUrl[0]}, (error) => {
						this._handleError("Please enter your credential.");
						this.setState({url: uri, authFunction : this.regularSignIn, settingAuth: true, needPassword: true, portNum: region});
					})
				}else{
					const url = new URL(uri);
					let portValue = url.port;
					if(url.port.length === 0){
						portValue = getDefaultPortFromUri(uri);
					}
					this.endpointCheckin(uri, portValue, {}, (error) => {
						this._handleError("Please enter your credential.");
						this.setState({url: uri, authFunction : this.regularSignIn, settingAuth: true, needPassword: true, portNum: portValue});
					})
				}

			}}>
			  <ListItemIcon>
		        <DataIcon/>
		      </ListItemIcon>
	          <ListItemText primary={uri}/>
	          <ListItemSecondaryAction>
	            <IconButton aria-label="Delete" onClick={() => {
	            	// this.setState({
					// 	deleteFunc: () =>
					// 		deleteHistory(uri, (accept) => {
					// 			this.historyListUpdateFromBackend();
					// 		}, (error) => {
					// 			this._handleError("Delete History Failed");
					// 		}),
					// 	openModal: true
					//
					// })
	            	deleteHistory(uri, (accept) => {
	            		this.historyListUpdateFromBackend(Object.keys(showType).find(key => showType[key] === this.state.endpoint.uri));
	            	}, (error) => {
	            		this._handleError("Delete History Failed");
	            	});
	            }}>
	              <DeleteIcon />
	            </IconButton>
	          </ListItemSecondaryAction>
	        </ListItem>
		);
	}

	regularSignIn = () => {
		const {url, username, password, needPassword, rsa, pemFileName} = this.state;
		const loginType = getType(this.state.endpoint);
		if((url.substr(url.length - 3) === '://' && loginType !== showType.s3) || (url.length < 1 && this.state.portNum < 1)) {
			loginType !== showType.s3 ? this._handleError("Please enter a valid URL") : this._handleError("Please enter a valid bucketname and region")
			return;
		}
	// if(!needPassword){
	// 	this.endpointCheckin(this.state.url, this.state.portNum, {}, () => {
	// 		this.setState({needPassword: true});
	// 	});
	// }
	// else{
		// User is expected to enter password to login

		if(username.length === 0 || password.length === 0) {
			this._handleError(loginType !== showType.s3 ? "Enter a username or password" : "Enter an access key or secret key");
			return;
		}

		if(loginType === showType.s3){
			let combinedUrl = generateURLForS3(url, this.state.portNum);
			const credId = username+"@"+ combinedUrl.toString();
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
		// let jsEncrypt = new JSEncrypt();
		// jsEncrypt.setPublicKey(ODS_PUBLIC_KEY);
		// let encryptedPwd = jsEncrypt.encrypt(this.state.password);
		const credId = username+"@"+ url.toString();

		// if(loginType === showType.sftp){
		// 	// let credId = "";
		// 	// let encryptedPwd = this.state.password;
		// 	// if((username.length === 0 || password.length === 0) && rsa.length === 0 && pemFileName.length === 0){
		// 	// 	this._handleError("Enter a username, password, or RSA Secret");
		// 	// 	return;
		// 	// }
		// 	// else if((username.length !== 0 || password.length !== 0)){
		// 	// 	let jsEncrypt = new JSEncrypt();
		// 	// 	jsEncrypt.setPublicKey(ODS_PUBLIC_KEY);
		// 	// 	encryptedPwd = jsEncrypt.encrypt(this.state.password);
		// 	// 	credId = username+"@"+ url.toString();
		// 	// }
		// 	// if( rsa.length !== 0){
		// 	// 	// encryptedPwd = this.state.rsa;
		// 	// 	// credId = username+"@"+ url.toString();
		// 	// }
		// 	// else if(pemFileName.length !== 0){
		// 	//
		// 	// }
		// 	this.endpointCheckin(url,
		// 		this.state.portNum,
		// 		{type: "userinfo", credId: credId, name: username, password: encryptedPwd,
		// 			rsa: this.state.rsa, pemFile: this.state.pemFile},
		// 		() => {
		// 			this._handleError("Authentication Failed");
		// 		}
		// 	);
		// 	// return;
		// }else{
		// 	this.endpointCheckin(url,
		// 	this.state.portNum,
		// 	{type: "userinfo", credId: credId, name: username, password: encryptedPwd},
		// 	() => {
		// 		this._handleError("Authentication Failed");
		// 		}
		// 	);
		// }

		this.endpointCheckin(url,
			this.state.portNum,
			{type: loginType, credId: credId, name: username, password: password,
				rsa: this.state.rsa, pemFile: this.state.pemFile},
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
				}, function() {console.log(this.state.pemFile)});
		}
		if(file.name.length > 0){
			reader.readAsText(file);
		}
	}

	// Globus has deprecated singing in with username and password and instead recommends using globus url
    // globusActivateSignin = () => {
    // 	const {endpointSelected} = this.state;
	// 	this.props.setLoading(true);
	// 	globusEndpointActivate(endpointSelected, this.state.username,  this.state.password, (msg) => {
	// 		this.props.setLoading(false);
	// 		endpointSelected.activated = true;
	// 		this.endpointModalLogin(endpointSelected);
	// 	}, (error) => {
	// 		this.props.setLoading(false);
	// 		this._handleError("Authentication Failed");
	// 	});
	// }

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



	// endpointModalClose = () => {this.setState({selectingEndpoint: false})}

	deleteConfirmationModal = () => {
		const handleClose = () => {
			this.setState({openModal: false, deleteFunc: ()=>{}});
		}
		const confirm = () => {
			// this.setState({deleteConfirm: true});
			this.state.deleteFunc();
			handleClose();
		}
		const deny = () => {
			// this.setState({deleteConfirm: false});
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
		// const BackButton = this.backButton();
		// console.log(type);



		return(
		<div >
			{/*{this.deleteConfirmationModal()}*/}
			{!settingAuth && <div className={"authenticationContainer"}>
		        {/*<ListItem button onClick={() =>{*/}
		        {/*	back()*/}
		        {/*}}>*/}
		        {/*  <ListItemIcon>*/}
		        {/*  	<BackIcon/>*/}
		        {/*  </ListItemIcon>*/}
		        {/*  <ListItemText primary="Back" />*/}
		        {/*</ListItem>*/}
				{/*<Button style={{width: "100%", textAlign: "left"}} onClick={() =>{*/}
				{/*	back()*/}
				{/*}}> <BackIcon/>Back</Button>*/}
				{/*<Divider/>*/}

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
				{(isOAuth[loginType] && loginType !== showType.gsiftp) && this.getCredentialListComponentFromList(credList, type)}
				{/* GridFTP OAuth handler */}
				{loginType === showType.gsiftp && this.getEndpointListComponentFromList(endpointIdsList)}
				{/* Other login handlers*/}
				{!isOAuth[loginType] &&
		        	this.getHistoryListComponentFromList(historyList)}
		        	<Grid container justify={"space-between"} spacing={2} style={{padding: "3%"}}>
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

		    	{/*<Button style={{width: "100%", textAlign: "left"}} onClick={() => {*/}
		    	{/*	if(needPassword){*/}
		    	{/*		this.setState({needPassword: false})*/}
				{/*	}else{*/}
				{/*		this.setState({settingAuth: false})}*/}
				{/*	}*/}

		    	{/*}> <BackIcon/>Back</Button>*/}
		    	{/*<Divider />*/}
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
								required
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
		    	<Grid container justify={"space-between"} spacing={2} style={{padding: "3%"}}>
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
