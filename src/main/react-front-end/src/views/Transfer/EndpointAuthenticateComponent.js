import React, { Component } from 'react';
import PropTypes from "prop-types";
import {openDropboxOAuth, openGoogleDriveOAuth, openGridFtpOAuth, history, dropboxCredList, 
		listFiles, deleteCredential, deleteHistory, listEndpoints, globusEndpointIds, deleteEndpointId, 
		globusEndpointActivate, globusEndpointDetail} from "../../APICalls/APICalls";
import {DROPBOX_TYPE, GOOGLEDRIVE_TYPE, FTP_TYPE, SFTP_TYPE, GRIDFTP_TYPE, HTTP_TYPE, SCP_TYPE} from "../../constants";

import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Button from "@material-ui/core/Button";
import TextField from '@material-ui/core/TextField';

import Divider from '@material-ui/core/Divider';
import DataIcon from '@material-ui/icons/Laptop';
import BackIcon from '@material-ui/icons/KeyboardArrowLeft'
import AddIcon from '@material-ui/icons/AddToQueue';
import Modal from '@material-ui/core/Modal';

import {getCred} from "./initialize_dnd.js";

import {eventEmitter} from "../../App";

import GlobusEndpointListingComponent from "./GlobusEndpointListingComponent";
import ListItemSecondaryAction from '@material-ui/core/ListItemSecondaryAction';
import IconButton from '@material-ui/core/IconButton';
import DeleteIcon from '@material-ui/icons/Delete';

import {getType, getName} from '../../constants.js';
export default class EndpointAuthenticateComponent extends Component {
	static propTypes = {
		loginSuccess : PropTypes.func,
		endpoint : PropTypes.object,
		history: PropTypes.array,
        credentials: PropTypes.object,
		type: PropTypes.string,
		back: PropTypes.func,
		setLoading : PropTypes.func
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
		};

		let loginType = getType(props.endpoint);
		if(loginType === GRIDFTP_TYPE){
			this.endpointIdsListUpdateFromBackend();
		}else if(loginType === FTP_TYPE || loginType === SFTP_TYPE){
		    this.historyListUpdateFromBackend();
		}
		this.handleChange = this.handleChange.bind(this);
		this._handleError = this._handleError.bind(this);
		this.getEndpointListComponentFromList = this.getEndpointListComponentFromList.bind(this);
	}

	credentialListUpdateFromBackend = () => {
		this.props.setLoading(true);

		dropboxCredList((data) =>{
			this.setState({credList: data});
			this.props.setLoading(false);
		}, (error) =>{
			this._handleError(error);
			this.props.setLoading(false);
		});
	}

	endpointIdsListUpdateFromBackend = () => {
		this.props.setLoading(true);
		globusEndpointIds({},(data) =>{
			this.setState({endpointIdsList: data});
			this.props.setLoading(false);
		}, (error) =>{
			this._handleError(error);
			this.props.setLoading(false);
		});
	}

	historyListUpdateFromBackend = () => {
		this.props.setLoading(true);
		history("", (data) =>{
			this.setState({historyList: data.filter((v) => { return v.indexOf(this.props.endpoint.uri) == 0 })});
			this.props.setLoading(false);
		}, (error) => {
			this._handleError(error);
			this.props.setLoading(false);
		});
	}

	_handleError = (msg) => {
    	eventEmitter.emit("errorOccured", msg);
	}

	handleChange = name => event => {
      this.setState({
        [name]: event.target.value,
      });
    };

	endpointCheckin=(url, credential, callback) => {
		this.props.setLoading(true);
		const endpointSet = {
			uri: url,
			login: true,
			side: this.props.endpoint.side,
			credential: credential
		}
		listFiles(url, endpointSet, null, (response) => {
			history(url, (suc) => {
				console.log(suc)
			}, (error) => {
				this._handleError(error);
			})
			this.props.loginSuccess(endpointSet);
		}, (error) => {
			this.props.setLoading(false);
			callback(error);
		})
	}

	getEndpointListComponentFromList(endpointIdsList){
		const {endpoint} = this.state;
		const {loginSuccess} = this.props;
		return Object.keys(endpointIdsList)
			.map((v) =>
			<ListItem button key={v} onClick={() => {
				globusEndpointDetail(endpointIdsList[v], (resp) => {
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
	            	deleteEndpointId(endpointIdsList[v], (accept) => {
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
		return Object.keys(credList).filter(id => {
			return (credList[id].name.toLowerCase().indexOf(type.toLowerCase()) != -1 
						&& !getCred().includes(id))})
			.map((v) =>
			<ListItem button key={v} onClick={() => {
				const endpointSet = {
					uri: endpoint.uri,
					login: true,
					credential: {uuid: v},
					side: endpoint.side
				}
				//addCred(v, endpoint);
				loginSuccess(endpointSet);
			}}>
			  <ListItemIcon>
		        <DataIcon/>
		      </ListItemIcon>
	          <ListItemText primary={credList[v].name} />
	          <ListItemSecondaryAction>
	            <IconButton aria-label="Delete" onClick={() => {
	            	deleteCredential(v, (accept) => {
	            		this.credentialListUpdateFromBackend();
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
	getHistoryListComponentFromList(historyList){
		return historyList.map((v) =>
			<ListItem button key={v} onClick={() => {
				this.endpointCheckin(v, {}, (error) => {
					this._handleError("Please enter your credential.");
					this.setState({url: v, authFunction : this.regularSignIn, settingAuth: true, needPassword: true});
				})
			}}>
			  <ListItemIcon>
		        <DataIcon/>
		      </ListItemIcon>
	          <ListItemText primary={v} />
	          <ListItemSecondaryAction>
	            <IconButton aria-label="Delete" onClick={() => {
	            	deleteHistory(v, (accept) => {
	            		this.historyListUpdateFromBackend();
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
		const { needPassword} = this.state;
		
		if(!needPassword){
    		this.endpointCheckin(this.state.url, {}, () => {
    			this.setState({needPassword: true});
    		});
    	}else{
    		this.endpointCheckin(this.state.url, {type: "userinfo", username: this.state.username, password: this.state.password}, (msg) => {
    			this._handleError("Authentication Failed");
    		});
    	}
	}

    globusActivateSignin = () => {
    	const {endpointSelected} = this.state;
		globusEndpointActivate(endpointSelected, this.state.username,  this.state.password, (msg) => {
			this.endpointModalLogin(endpointSelected);
		}, (error) => {
			this._handleError("Authentication Failed");
		});
	}

	endpointModalAdd = (endpoint) => {
		this.props.setLoading(true);
		globusEndpointIds({},(data) =>{
			this.state.endpointIdsList = data;
			this.endpointModalLogin(endpoint);
			this.props.setLoading(false);
		}, (error) =>{
			this._handleError(error);
			this.props.setLoading(false);
		});
		
	};

	endpointModalLogin = (endpoint) => {
		if(endpoint.activated === "false"){

			this.setState({settingAuth: true, authFunction : this.globusActivateSignin, needPassword: true, endpointSelected: endpoint, selectingEndpoint: false});
		}else{
			this.setState({selectingEndpoint: false});
			this.endpointCheckin("gsiftp:///", {type: "globus", globusEndpoint: endpoint}, (msg) => {
    			this._handleError("Authentication Failed");
    		});
		}
	}
	
	render(){
		const {historyList, endpoint, credList, settingAuth, authFunction, needPassword, endpointIdsList, selectingEndpoint} = this.state;
		const { back, loginSuccess, setLoading} = this.props;
		const {uri} = endpoint;
		
		const type = getName(endpoint);
		const loginType = getType(endpoint);
		const histList = this.getHistoryListComponentFromList(historyList);

		const cloudList = this.getCredentialListComponentFromList(credList, type)
		const endpointsList = this.getEndpointListComponentFromList(endpointIdsList);
		const endpointModalClose = () => {this.setState({selectingEndpoint: false})};

		return(
		<div > 
			{!settingAuth && <List component="nav" style={{overflow: 'auto'}}>
		        <ListItem button onClick={() =>{
		        	back()
		        }}>
		          <ListItemIcon>
		          	<BackIcon/>
		          </ListItemIcon>
		          <ListItemText primary="Back" />
		        </ListItem>
		        <ListItem button onClick={() => {
		        	if(loginType == DROPBOX_TYPE){
		        		openDropboxOAuth();
		        	}else if(loginType == GOOGLEDRIVE_TYPE){
		        		openGoogleDriveOAuth();
		        	}else if(loginType == FTP_TYPE){
		        		this.setState({settingAuth: true, authFunction : this.regularSignIn, needPassword: false, url: "ftp://"});
		        	}else if(loginType == SFTP_TYPE){
		        		this.setState({settingAuth: true, authFunction : this.regularSignIn, needPassword: true, url: "sftp://"});
		        	}else if(loginType == HTTP_TYPE){
		        		this.setState({settingAuth: true, authFunction : this.regularSignIn, needPassword: false, url: "http://"});
		        	}else if(loginType == SCP_TYPE){
		        		this.setState({settingAuth: true, authFunction : this.regularSignIn, needPassword: false, url: "scp://"});
		        	}else if(loginType == GRIDFTP_TYPE){
		        		this.setState({selectingEndpoint: true});
		        	}
		        }}>
		          <ListItemIcon>
		          	<AddIcon/>
		          </ListItemIcon>
		          <ListItemText primary={"Add New " + type} />
		        </ListItem>
		        <Divider />
		        {(loginType == DROPBOX_TYPE || loginType == GOOGLEDRIVE_TYPE) && cloudList}

		        {loginType == GRIDFTP_TYPE && endpointsList}
		        {loginType != DROPBOX_TYPE && loginType != GOOGLEDRIVE_TYPE && loginType != GRIDFTP_TYPE && 
		        	histList}
		    </List>}
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

		    	<div style={{flexGrow: 1, flexDirection: "column",}}>
		    	<Button style={{width: "100%", textAlign: "left"}} onClick={() => {
		    		this.setState({settingAuth: false})}
		    	}> <BackIcon/>Back</Button>
		    	<Divider />
		    	{loginType !== GRIDFTP_TYPE && 
			    	<TextField
			    	  style={{width: "80%"}}
			          id="outlined-name"
			          label="Url"
			          value={this.state.url}
			          onChange={this.handleChange('url')}
			          margin="normal"
			          variant="outlined"
			        />
		    	}

		        
		        {needPassword &&
		        	<div>
			        <TextField
			    	  style={{width: "80%"}}
			          id="outlined-name"
			          label="Username"
			          value={this.state.username}
			          onChange={this.handleChange('username')}
			          margin="normal"
			          variant="outlined"
			        />
			        <TextField
			    	  style={{width: "80%"}}
			          id="outlined-name"
			          label="Password"
			          type="password"
			          value={this.state.password}
			          onChange={this.handleChange('password')}
			          margin="normal"
			          variant="outlined"
			        />
			        </div>
		    	}
		    	<Button style={{width: "100%", textAlign: "left"}} onClick={authFunction}>Next</Button>
		    	</div>
		    }
      	</div>);
	}
}