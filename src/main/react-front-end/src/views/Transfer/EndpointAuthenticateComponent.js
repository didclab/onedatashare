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

import {getType, getName, getDefaultPortFromUri, getTypeFromUri} from '../../constants.js';
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
			portNum: -1,
			portNumField: true
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
		history("",-1, (data) =>{
			this.setState({historyList: data.filter((v) => { return v.indexOf(this.props.endpoint.uri) == 0 })});
			this.props.setLoading(false);
		}, (error) => {
			this._handleError("Unable to retrieve data from backend. Try log out or wait for few minutes.");
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
	
	handleUrlChange = name => event => {
		let url = event.target.value;

		// Count the number of colons (2nd colon means the URL contains the portnumber)
		let colonCount = 0;
		for(let i=0; i < url.length; colonCount+=+(':'===url[i++]));

		this.setState({
			"portNumField": colonCount>=2 ? false : true,
			[name] : event.target.value
		})
		
	}

	endpointCheckin=(url, portNum, credential, callback) => {
		this.props.setLoading(true);
		
		// Adding Port number to the URL to ensure that the backend remembers the endpoint URL
		
		let colonCount = 0;
		for(let i=0; i < url.length; colonCount+=+(':'===url[i++]));
		// If the Url already doesn't contain the portnumber append it else no change
		if(colonCount==1)
			url = url + ":" + portNum;

		let endpointSet = {
			uri: url,
			login: true,
			side: this.props.endpoint.side,
			credential: credential,
			portNumber: portNum
		}

		// scp protocol is set into a sftp automatically
		if(getTypeFromUri(endpointSet.uri)){
			if(endpointSet.uri.startsWith("scp://")){
				endpointSet.uri = "sftp://" + endpointSet.uri.substring(6);
				url = endpointSet.uri;
			}
		}else{
			this._handleError("Protocol is not understood");
		}
		
		listFiles(url, endpointSet, null, (response) => {
			history(url, portNum, (suc) => {
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
					credential: {uuid: v, name: credList[v].name},
					side: endpoint.side
				}
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
		return historyList.map((uri) =>
			<ListItem button key={uri} onClick={() => {
				const url = new URL(uri);
				let portValue = url.port;
				if(url.port.length === 0){
					portValue = getDefaultPortFromUri(uri);
				}
				this.endpointCheckin(uri, portValue, {}, (error) => {
					this._handleError("Please enter your credential.");
					this.setState({url: uri, authFunction : this.regularSignIn, settingAuth: true, needPassword: true, portNum: portValue});
				})
			}}>
			  <ListItemIcon>
		        <DataIcon/>
		      </ListItemIcon>
	          <ListItemText primary={uri}/>
	          <ListItemSecondaryAction>
	            <IconButton aria-label="Delete" onClick={() => {
	            	deleteHistory(uri, (accept) => {
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
    		this.endpointCheckin(this.state.url, this.state.portNum, {}, () => {
    			this.setState({needPassword: true});
    		});
    	}else{
    		this.endpointCheckin(this.state.url, this.state.portNum,{type: "userinfo", username: this.state.username, password: this.state.password}, (msg) => {
    			this._handleError("Authentication Failed");
    		});
    	}
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

    globusActivateSignin = () => {
    	const {endpointSelected} = this.state;
    	this.props.setLoading(true);
		globusEndpointActivate(endpointSelected, this.state.username,  this.state.password, (msg) => {
			this.props.setLoading(false);
			endpointSelected.activated = true;
			this.endpointModalLogin(endpointSelected);
		}, (error) => {
			this.props.setLoading(false);
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
			eventEmitter.emit("messageOccured", "Please activate your globus endpoint using credential.");
			this.setState({settingAuth: true, authFunction : this.globusActivateSignin, needPassword: true, endpointSelected: endpoint, selectingEndpoint: false});
		}else{
			
			this.setState({selectingEndpoint: false});
			this.endpointCheckin("gsiftp:///", this.state.portNum, {type: "globus", globusEndpoint: endpoint}, (msg) => {
				
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
		        		let loginUri = "ftp://";
		        		this.setState({settingAuth: true, authFunction : this.regularSignIn, 
		        			needPassword: false, url: loginUri, portNum: getDefaultPortFromUri(loginUri)});
		        	}else if(loginType == SFTP_TYPE){
		        		let loginUri = "sftp://";
		        		this.setState({settingAuth: true, authFunction : this.regularSignIn, 
		        			needPassword: true, url: loginUri, portNum: getDefaultPortFromUri(loginUri)});
		        	}else if(loginType == HTTP_TYPE){
		        		let loginUri = "http://";
		        		this.setState({settingAuth: true, authFunction : this.regularSignIn, 
		        			needPassword: false, url: loginUri, portNum: getDefaultPortFromUri(loginUri)});
		        	}else if(loginType == SCP_TYPE){
		        		let loginUri = "scp://";
		        		this.setState({settingAuth: true, authFunction : this.regularSignIn, 
		        			needPassword: false, url: loginUri, portNum: getDefaultPortFromUri(loginUri)});
		        	}else if(loginType == GRIDFTP_TYPE){
		        		this.setState({selectingEndpoint: true, authFunction : this.globusSignIn});
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
		    		<div>
			    	<TextField
			    	  style={{width: "60%"}}
			          id="outlined-name"
			          label="Url"
			          value={this.state.url}
			          onChange={this.handleUrlChange('url')}
			          margin="normal"
			          variant="outlined"
			        />
			        <TextField
			    	  style={{width: "20%", background: this.state.portNumField? "white" : "#D3D3D3"}}
					  id="outlined-pnum"
					  disabled = {!this.state.portNumField}
			          label="Port Number"
			          value={this.state.portNumField? this.state.portNum : "-"} 
			          onChange={this.handleChange('portNum')}
			          margin="normal"
			          variant="outlined"
			        />
			        </div>
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