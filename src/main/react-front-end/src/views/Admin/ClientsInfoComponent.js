import React, {Component} from 'react';
import { getUsers, enableOrDisableAdmin } from '../../APICalls/APICalls';

import Paper from '@material-ui/core/Paper';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';

import Person from '@material-ui/icons/Person';
import People from '@material-ui/icons/People';
import Done from '@material-ui/icons/Done'
import Clear from '@material-ui/icons/Clear';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import {eventEmitter} from "../../App";

import './ClientsInfoComponent.css';

export default class ClientsInfoComponent extends Component{

	constructor(){
		super();
		this.state = {users:[], admins:[]};
		getUsers('getUsers', (resp) => {
			//success
			this.setState({users:resp});
			}, (resp) => {
			//failed
			console.log('Error encountered in getUsers request to API layer');
		});

		getUsers('getAdministrators', (resp) => {
			//success
			this.setState({admins:resp});
			}, (resp) => {
			//failed
			console.log('Error encountered in getUsers request to API layer');
		});
	}

	enableOrDisableAdmin(event, row){
		var popupMsg = ""
		var isAdmin = event.target.checked;
		if(isAdmin){
			popupMsg = "Do you want to enable "+ row.firstName + " " + row.lastName+ " as admin.";
			this.setState({showIsAdminPopup: true, adminChangePopupMsg:popupMsg, targetUser: row.email, isAdmin: true, firstName: row.firstName, lastName: row.lastName});
		}
		else{
			popupMsg = "Do you want to disable "+ row.firstName + " " + row.lastName+ " as admin.";
			this.setState({showIsAdminPopup: true, adminChangePopupMsg:popupMsg, targetUser: row.email, isAdmin: false, firstName: row.firstName, lastName: row.lastName});
		}		
	}

	enableOrDisableAdminUsingStateInfo(email, isAdmin){
		enableOrDisableAdmin(email, isAdmin).then((resp)=>{
			if(resp){
				eventEmitter.emit("errorOccured", this.state.firstName + " " + this.state.lastName + " is " + (this.state.isAdmin ? "enabled": "disabled") + " as admin.");
			}
			else{
				eventEmitter.emit("errorOccured", "Error while updating the user");
			}
			getUsers('getUsers', (resp) => {
				this.setState({users:resp, showIsAdminPopup: false, adminChangePopupMsg: "", targetUser: "", firstName: "", lastName: ""});
				}, (error) => {
				console.log('Error encountered in getUsers request to API layer');
			});
		});
	}
	handleClose = () => {
		this.setState({ showIsAdminPopup: false, adminChangePopupMsg: "", targetUser: "", firstName: "", lastName: ""});
	};

	render(){
		const height = window.innerHeight+"px";
		const {users} = this.state;
		const {admins} = this.state;
		const tbcellStyle= {textAlign: 'center'}

		return(
			<div>
				<Dialog
	          open={this.state.showIsAdminPopup}
	          onClose={this.handleClose}
						aria-labelledby="form-dialog-title"
	        >
	          <DialogTitle id="form-dialog-title">Confirm</DialogTitle>
	          <DialogContent style={{width:"100%"}}>
	            <DialogContentText>
	              {this.state.adminChangePopupMsg}
	            </DialogContentText>	            
	          </DialogContent>
	          <DialogActions>
			  	<Button onClick={() => this.enableOrDisableAdminUsingStateInfo(this.state.targetUser, this.state.isAdmin)} color="primary">
	              Yes
	            </Button>
	            <Button onClick={() => this.handleClose()} color="secondary">
	              No
	            </Button>
	          </DialogActions>
	        </Dialog>
				<Paper id="clientsInfo" style={{marginLeft: '9%', marginRight: '9%', marginTop: '5%', marginBottom: '5%', border: 'solid 2px #d9edf7'}}>
					<Table>
						<TableHead style={{backgroundColor: '#d9edf7'}}>
							<TableRow>
							<TableCell colSpan={7} style={{...tbcellStyle, backgroundColor: '#d9edf7', width: '7.5%',  fontSize: '2rem', color: '#31708f'}}>Users Information</TableCell>
							</TableRow>
							<TableRow>
							<TableCell style={{...tbcellStyle, width: '25%',  fontSize: '2rem', color: '#31708f'}}><People />Users</TableCell>
							<TableCell style={{...tbcellStyle, width: '10%',  fontSize: '2rem', color: '#31708f'}}>First Name</TableCell>
							<TableCell style={{...tbcellStyle, width: '10%',  fontSize: '2rem', color: '#31708f'}}>Last Name</TableCell>
							<TableCell style={{...tbcellStyle, width: '25%',  fontSize: '2rem', color: '#31708f'}}>Sign Up</TableCell>
							<TableCell style={{...tbcellStyle, width: '5%',  fontSize: '2rem', color: '#31708f'}}>Validation</TableCell>
							<TableCell style={{...tbcellStyle, width: '25%',  fontSize: '2rem', color: '#31708f'}}>Last Activity</TableCell>
							<TableCell style={{...tbcellStyle, width: '25%',  fontSize: '2rem', color: '#31708f'}}>Make Admin</TableCell>
							</TableRow>
						</TableHead>
						<TableBody>
							{
								users.map(resp =>{
									var timeStamp = resp.registerMoment * 1000;
									var date = new Date(timeStamp);

									return(
									<TableRow>
										<TableCell style={{fontSize: '1rem'}}><Person />{resp.email}</TableCell>
										<TableCell style={{...tbcellStyle, fontSize: '1rem'}}>{resp.firstName}</TableCell>
										<TableCell style={{...tbcellStyle, fontSize: '1rem'}}>{resp.lastName}</TableCell>
										<TableCell style={{...tbcellStyle, fontSize: '1rem'}}>{
											date.getMonth() +'/' + date.getDate() + '/' + date.getFullYear() + ' ' + date.getHours() + ':' + date.getMinutes()
										}</TableCell>
										<TableCell style={{...tbcellStyle, fontSize: '1rem'}}>
											{(resp.validated)?<Done style={{color: 'green'}} />:<Clear style={{color: 'red'}} />}
										</TableCell>
										<TableCell style={{...tbcellStyle, fontSize: '1rem'}}>TBD</TableCell>
										<TableCell style={{...tbcellStyle, fontSize: '1rem'}}><input type="checkbox" checked = {resp.isAdmin} onChange={(event) => this.enableOrDisableAdmin(event, resp)}></input></TableCell>
									</TableRow>)
								})
							}
						</TableBody>
					</Table>   
				</Paper>

				<Paper id="adminsInfo" style={{marginLeft: '10%', marginRight: '10%', marginTop: '2%', marginBottom: '10%', border: 'solid 2px #d9edf7'}}>
					<Table>
						<TableHead style={{backgroundColor: '#d9edf7'}}>
							<TableRow>
							<TableCell colSpan={6} style={{...tbcellStyle, backgroundColor: '#d9edf7', width: '7.5%',  fontSize: '2rem', color: '#31708f'}}>Admin Information</TableCell>
							</TableRow>
							<TableRow>
							<TableCell style={{...tbcellStyle, width: '33%',  fontSize: '2rem', color: '#31708f'}}><People />Users</TableCell>
							<TableCell style={{...tbcellStyle, width: '33%',  fontSize: '2rem', color: '#31708f'}}>First Name</TableCell>
							<TableCell style={{...tbcellStyle, width: '33%',  fontSize: '2rem', color: '#31708f'}}>Last Name</TableCell>
							<TableCell style={{...tbcellStyle, width: '33%',  fontSize: '2rem', color: '#31708f'}}>Last Activity</TableCell>
							</TableRow>
						</TableHead>
						<TableBody>
							{
								admins.map(resp =>(
									<TableRow>
										<TableCell style={{fontSize: '1rem'}}><Person />{resp.email}</TableCell>
										<TableCell style={{...tbcellStyle, fontSize: '1rem'}}>{resp.firstName}</TableCell>
										<TableCell style={{...tbcellStyle, fontSize: '1rem'}}>{resp.lastName}</TableCell>
										<TableCell style={{...tbcellStyle, fontSize: '1rem'}}>TBD</TableCell>
									</TableRow>))
							}
						</TableBody>
					</Table>   
				</Paper>
			</div>
		);
	}
}