import React, {Component} from 'react';
import { getUsers, updateAdminRightsApiCall } from '../../APICalls/APICalls';

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
import {store} from '../../App.js';
import TablePagination from '@material-ui/core/TablePagination'
import TableFooter from '@material-ui/core/TableFooter'
import TablePaginationActions from '../TablePaginationActions'
import TableSortLabel from '@material-ui/core/TableSortLabel';
import Tooltip from '@material-ui/core/Tooltip';
import { withStyles } from '@material-ui/core';

import './ClientsInfoComponent.css';
const styles = theme => ({
	root:{
		width:'fit-content'
	},
	toolbar:{
		paddingLeft:'300px',
		paddingRight:'300px'
	},
tablePaginationCaption: {
		fontSize: '15px'
	},
tablePaginationSelect: {
		fontSize: '15px',
		lineHeight:'20px'
	}
})
class ClientsInfoComponent extends Component{

	constructor(){
		super();
		this.state = {users:[],
			 totalUsersCount:0,
			 admins:[],
			 totalAdminsCount:0,
			 userTblPage: 0,
			 userTblRowsPerPage: 10,
			 userTblRowsPerPageOptions : [10, 20, 50, 100],
			 userTblOrder : 'asc',
			 userTblOrderBy : 'email',
			 adminTblPage: 0,
			 adminTblRowsPerPage: 10,
			 adminTblRowsPerPageOptions : [10, 20, 50, 100],
			 adminTblOrder : 'asc',
			 adminTblOrderBy : 'email'};
		this.getUserInfo = this.getUserInfo.bind(this)
		this.getAdminInfo = this.getAdminInfo.bind(this)
		this.getUserInfo()
		this.getAdminInfo()
	}

	getUserInfo = () => getUsers('getUsers',  this.state.userTblPage, this.state.userTblRowsPerPage, this.state.userTblOrderBy, this.state.userTblOrder, (resp) => {
		//success
		this.setState({users:resp.users, totalUsersCount: resp.totalCount});
		}, (resp) => {
		//failed
		console.log('Error encountered in getUsers request to API layer');
	});

	getAdminInfo = () => getUsers('getAdministrators',  this.state.adminTblPage, this.state.adminTblRowsPerPage, this.state.adminTblOrderBy, this.state.adminTblOrder, (resp) => {
		//success
		console.log(resp.users.length + "---")
		this.setState({admins:resp.users, totalAdminsCount: resp.totalCount});
		}, (resp) => {
		//failed
		console.log('Error encountered in getUsers request to API layer');
	});

	// Shows the user a confirmation popup to confirm the update request.
	updateAdminRights(event, row){
		var popupMsg = ""
		var isAdmin = event.target.checked;
		if(isAdmin){
			popupMsg = "Please confirm if " + row.firstName + " " + row.lastName+ " must be granted admin privileges.";
			this.setState({showIsAdminPopup: true, adminChangePopupMsg:popupMsg, targetUser: row.email, isAdmin: true, firstName: row.firstName, lastName: row.lastName});
		}
		else{
			popupMsg = "Please confirm if admin privileges of "+ row.firstName + " " + row.lastName+ " must be revoked.";
			this.setState({showIsAdminPopup: true, adminChangePopupMsg:popupMsg, targetUser: row.email, isAdmin: false, firstName: row.firstName, lastName: row.lastName});
		}		
	}

	// The actual call to update the admin information is triggered after the user selects "Yes" in the confirmation popup
	// The user information is retrieved from the state
	updateAdminRightsUsingStateInfo(email, isAdmin){
		updateAdminRightsApiCall(email, isAdmin).then((resp)=>{
			if(resp){
				eventEmitter.emit("errorOccured", "Admin privileges is " + (this.state.isAdmin ? "granted for ": "revoked for ") + this.state.firstName + " " + this.state.lastName);
			}
			else{
				eventEmitter.emit("errorOccured", "Error while updating the user");
			}
			getUsers('getUsers', this.state.userTblPage, this.state.userTblRowsPerPage, this.state.userTblOrderBy, this.state.userTblOrder, (resp) => {
				this.setState({users:resp.users, showIsAdminPopup: false, adminChangePopupMsg: "", targetUser: "", firstName: "", lastName: ""});
				}, (error) => {
				console.log('Error encountered in getUsers request to API layer');
			});
		});
	}
	handleClose = () => {
		this.setState({ showIsAdminPopup: false, adminChangePopupMsg: "", targetUser: "", firstName: "", lastName: ""});
	};
	handleUserTblChangePage = (event, page) => {
		this.state.userTblPage=page
		this.setState({ userTblPage: page });
		this.getUserInfo()
	};

	handleUserTblChangeRowsPerPage = event => {		
		this.state.userTblPage=0
		this.state.userTblRowsPerPage = parseInt(event.target.value)
		this.setState({ userTblPage: 0, userTblRowsPerPage: parseInt(event.target.value) });
		this.getUserInfo()
	};

	handleUserTblRequestSort = (property) => {
		const orderBy = property;
		let order = 'desc';
		if (this.state.userTblOrderBy === property && this.state.userTblOrder === 'desc') {
		order = 'asc';
		}
		this.setState({ userTblOrder:order, userTblOrderBy:orderBy });
		this.state.userTblOrder=order
		this.state.userTblOrderBy = orderBy
		this.getUserInfo()
		
	};
	
	handleAdminsTblChangePage = (event, page) => {
		this.state.adminTblPage=page
		this.setState({ adminTblPage: page });
		this.getAdminInfo()
	};

	handleAdminsTblChangeRowsPerPage = event => {		
		this.state.adminTblPage=0
		this.state.adminTblRowsPerPage = parseInt(event.target.value)
		this.setState({ adminTblPage: 0, adminTblRowsPerPage: parseInt(event.target.value) });
		this.getAdminInfo()
	};

	handleAdminsTblRequestSort = (property) => {
		const orderBy = property;
		let order = 'desc';
		if (this.state.adminTblOrderBy === property && this.state.adminTblOrder === 'desc') {
		order = 'asc';
		}
		this.setState({ adminTblOrder:order, adminTblOrderBy:orderBy });
		this.state.adminTblOrder=order
		this.state.adminTblOrderBy = orderBy
		this.getAdminInfo()
		
  	};

	render(){
		const height = window.innerHeight+"px";
		const {classes} = this.props;
		const {users} = this.state;
		const {admins} = this.state;
		const {userTblRowsPerPage, userTblRowsPerPageOptions, userTblPage, userTblOrder, userTblOrderBy, totalUsersCount} = this.state;
		const {adminTblRowsPerPage, adminTblRowsPerPageOptions, adminTblPage, adminTblOrder, adminTblOrderBy, totalAdminsCount} = this.state;
		const tbcellStyle= {textAlign: 'center'}
		const usersTable = 'usersTable'
		const adminsTable = 'adminsTable'
		const sortableColumns = {
			email: 'email',
			firstName: 'firstName',
			lastName : "lastName",
			lastActivity : "lastActivity",
			registerMoment: "registerMoment",
			organization: "organization"
		}
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
			  	<Button onClick={() => this.updateAdminRightsUsingStateInfo(this.state.targetUser, this.state.isAdmin)} color="primary">
	              Yes
	            </Button>
	            <Button onClick={() => this.handleClose()} color="secondary">
	              No
	            </Button>
	          </DialogActions>
	        </Dialog>
				<Paper id="clientsInfo" style={{marginLeft: '5%', marginRight: '5%', marginTop: '5%', marginBottom: '5%', border: 'solid 2px #d9edf7'}}>
					<Table>
						<TableHead style={{backgroundColor: '#d9edf7'}}>
							<TableRow>
								<TableCell colSpan={8} style={{...tbcellStyle, backgroundColor: '#d9edf7', width: '7.5%',  fontSize: '2rem', color: '#31708f'}}>Users Information</TableCell>
							</TableRow>
							<TableRow>
								<TableCell style={{...tbcellStyle, width: '15%',  fontSize: '1.75rem', color: '#31708f'}}>
									<Tooltip title="Sort on Username" placement='bottom-end' enterDelay={300}>
										<TableSortLabel
											active={userTblOrderBy === sortableColumns.email}
											direction={userTblOrder}
											onClick={() => {this.handleUserTblRequestSort(sortableColumns.email)}}>
											<People />Users
										</TableSortLabel>
									</Tooltip>						
								</TableCell>
								<TableCell style={{...tbcellStyle, width: '10%',  fontSize: '1.75rem', color: '#31708f'}}>
									<Tooltip title="Sort on First Name" placement='bottom-end' enterDelay={300}>
										<TableSortLabel
											active={userTblOrderBy === sortableColumns.firstName}
											direction={userTblOrder}
											onClick={() => {this.handleUserTblRequestSort(sortableColumns.firstName)}}>
											Name
										</TableSortLabel>
									</Tooltip>
								</TableCell>
								{/* <TableCell style={{...tbcellStyle, width: '10%',  fontSize: '1rem', color: '#31708f'}}>
									<Tooltip title="Sort on Last Name" placement='bottom-end' enterDelay={300}>
										<TableSortLabel
											active={userTblOrderBy === sortableColumns.lastName}
											direction={userTblOrder}
											onClick={() => {this.handleUserTblRequestSort(sortableColumns.lastName)}}>
											Last Name
										</TableSortLabel>
									</Tooltip>
								</TableCell> */}
								<TableCell style={{...tbcellStyle, width: '10%',  fontSize: '1.75rem', color: '#31708f'}}>
									<Tooltip title="Sort on Last Name" placement='bottom-end' enterDelay={300}>
										<TableSortLabel
											active={userTblOrderBy === sortableColumns.organization}
											direction={userTblOrder}
											onClick={() => {this.handleUserTblRequestSort(sortableColumns.organization)}}>
											Orgainzation
										</TableSortLabel>
									</Tooltip>
								</TableCell>
								<TableCell style={{...tbcellStyle, width: '15%',  fontSize: '1.75rem', color: '#31708f'}}>
									<Tooltip title="Sort on Sign Up" placement='bottom-end' enterDelay={300}>
										<TableSortLabel
											active={userTblOrderBy === sortableColumns.registerMoment}
											direction={userTblOrder}
											onClick={() => {this.handleUserTblRequestSort(sortableColumns.registerMoment)}}>
											Sign Up
										</TableSortLabel>
									</Tooltip>
								</TableCell>
								<TableCell style={{...tbcellStyle, width: '5%',  fontSize: '1.75rem', color: '#31708f'}}>
								Validation
								</TableCell>
								<TableCell style={{...tbcellStyle, width: '15%',  fontSize: '1.75rem', color: '#31708f'}}>
									<Tooltip title="Sort on Last Activity" placement='bottom-end' enterDelay={300}>
										<TableSortLabel
											active={userTblOrderBy === sortableColumns.lastActivity}
											direction={userTblOrder}
											onClick={() => {this.handleUserTblRequestSort(sortableColumns.lastActivity)}}>
											Last Activity
										</TableSortLabel>
									</Tooltip>
								</TableCell>
								<TableCell style={{...tbcellStyle, width: '15%',  fontSize: '1.75rem', color: '#31708f'}}>Make Admin</TableCell>
							</TableRow>
						</TableHead>
						<TableBody>
							{
								users.map(resp =>{
									var timeStamp = resp.registerMoment;
									console.log(timeStamp)
									var date = new Date(timeStamp);
									var lastActivity = new Date(resp.lastActivity);

									return(
									<TableRow>
										<TableCell style={{fontSize: '1rem'}}><Person />{resp.email}</TableCell>
										<TableCell style={{...tbcellStyle, fontSize: '1rem'}}>{resp.firstName + " " + resp.lastName}</TableCell>
										{/* <TableCell style={{...tbcellStyle, fontSize: '1rem'}}>{resp.lastName}</TableCell> */}
										<TableCell style={{...tbcellStyle, fontSize: '1rem'}}>{resp.organization}</TableCell>
										<TableCell style={{...tbcellStyle, fontSize: '1rem'}}>{
											(1 + date.getMonth()) +'/' + date.getDate() + '/' + date.getFullYear() + ' ' + date.getHours() + ':' + date.getMinutes()
										}</TableCell>
										<TableCell style={{...tbcellStyle, fontSize: '1rem'}}>
											{(resp.validated)?<Done style={{color: 'green'}} />:<Clear style={{color: 'red'}} />}
										</TableCell>
										<TableCell style={{...tbcellStyle, fontSize: '1rem'}}>{(1 + lastActivity.getMonth()) +'/' + lastActivity.getDate() + '/' + lastActivity.getFullYear() + ' ' + date.getHours() + ':' + lastActivity.getMinutes()}</TableCell>
										<TableCell style={{...tbcellStyle, fontSize: '1rem'}}><input type="checkbox" disabled = {resp.email === store.getState().email} checked = {resp.isAdmin} onChange={(event) => this.updateAdminRights(event, resp)}></input></TableCell>
									</TableRow>)
								})
							}
						</TableBody>
						<TableFooter style={{textAlign:'center'}}>
							<TableRow>
								<TablePagination
									rowsPerPageOptions={userTblRowsPerPageOptions}
									colSpan={8}
									count={totalUsersCount}
									rowsPerPage={userTblRowsPerPage}
									page={userTblPage}
									SelectProps={{
										native: true,
									}}
									onChangePage={this.handleUserTblChangePage}
									onChangeRowsPerPage={this.handleUserTblChangeRowsPerPage}
									ActionsComponent={TablePaginationActions}
									classes={{
										caption: classes.tablePaginationCaption,
										select: classes.tablePaginationSelect,
										toolbar: classes.toolbar
									}}
								/>
							</TableRow>
						</TableFooter>
					</Table>   
				</Paper>

				<Paper id="adminsInfo" style={{marginLeft: '10%', marginRight: '10%', marginTop: '2%', marginBottom: '10%', border: 'solid 2px #d9edf7'}}>
					<Table>
						<TableHead style={{backgroundColor: '#d9edf7'}}>
							<TableRow>
							<TableCell colSpan={6} style={{...tbcellStyle, backgroundColor: '#d9edf7', width: '7.5%',  fontSize: '2rem', color: '#31708f'}}>Admin Information</TableCell>
							</TableRow>
							<TableRow>
							<TableCell style={{...tbcellStyle, width: '33%',  fontSize: '2rem', color: '#31708f'}}>
								<Tooltip title="Sort on Username" placement='bottom-end' enterDelay={300}>
									<TableSortLabel
										active={adminTblOrderBy === sortableColumns.email}
										direction={adminTblOrder}
										onClick={() => {this.handleAdminsTblRequestSort(sortableColumns.email)}}>
										<People />Users
									</TableSortLabel>
								</Tooltip>
							</TableCell>
							<TableCell style={{...tbcellStyle, width: '33%',  fontSize: '2rem', color: '#31708f'}}>
								<Tooltip title="Sort on First Name" placement='bottom-end' enterDelay={300}>
									<TableSortLabel
										active={adminTblOrderBy === sortableColumns.firstName}
										direction={adminTblOrder}
										onClick={() => {this.handleAdminsTblRequestSort(sortableColumns.firstName)}}>
										First Name
									</TableSortLabel>
								</Tooltip>
							</TableCell>
							<TableCell style={{...tbcellStyle, width: '33%',  fontSize: '2rem', color: '#31708f'}}>
								<Tooltip title="Sort on Last Name" placement='bottom-end' enterDelay={300}>
									<TableSortLabel
										active={adminTblOrderBy === sortableColumns.lastName}
										direction={adminTblOrder}
										onClick={() => {this.handleAdminsTblRequestSort(sortableColumns.lastName)}}>
										Last Name
									</TableSortLabel>
								</Tooltip>
							</TableCell>
							<TableCell style={{...tbcellStyle, width: '33%',  fontSize: '2rem', color: '#31708f'}}>
							<Tooltip title="Sort on Last Activity" placement='bottom-end' enterDelay={300}>
									<TableSortLabel
										active={adminTblOrderBy === sortableColumns.lastActivity}
										direction={adminTblOrder}
										onClick={() => {this.handleAdminsTblRequestSort(sortableColumns.lastActivity)}}>
										Last Activity
									</TableSortLabel>
								</Tooltip>
							</TableCell>
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
						<TableFooter style={{textAlign:'center'}}>
							<TableRow>
								<TablePagination
									rowsPerPageOptions={adminTblRowsPerPageOptions}
									colSpan={5}
									count={totalAdminsCount}
									rowsPerPage={adminTblRowsPerPage}
									page={adminTblPage}
									SelectProps={{
										native: true,
									}}
									onChangePage={this.handleAdminsTblChangePage}
									onChangeRowsPerPage={this.handleAdminsTblChangeRowsPerPage}
									ActionsComponent={TablePaginationActions}
									classes={{
										caption: classes.tablePaginationCaption,
										select: classes.tablePaginationSelect,
										toolbar: classes.toolbar
									}}
								/>
							</TableRow>
						</TableFooter>
					</Table>   
				</Paper>
			</div>
		);
	}
}

export default withStyles(styles)(ClientsInfoComponent) 