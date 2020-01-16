import React, { Component } from 'react';
import { fetchJobsForAdmin, fetchAllJobs } from '../../APICalls/APICalls';
import { humanReadableSpeed } from './../../utils' 
import moment from 'moment'

import TextField from '@material-ui/core/TextField';
import Input from '@material-ui/core/Input';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import Button from '@material-ui/core/Button';
import TableRow from '@material-ui/core/TableRow';
import { ProgressBar, Grid, Row, Col } from 'react-bootstrap';
import Paper from '@material-ui/core/Paper';
import AppBar from '@material-ui/core/AppBar';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import TableSortLabel from '@material-ui/core/TableSortLabel';
import Tooltip from '@material-ui/core/Tooltip';
import Zoom from '@material-ui/core/Zoom';
import Info from '@material-ui/icons/Info';
import Cancel from '@material-ui/icons/Cancel';
import TablePagination from '@material-ui/core/TablePagination'
import TableFooter from '@material-ui/core/TableFooter'
import TablePaginationActions from '../TablePaginationActions'
import { updateGAPageView } from "../../analytics/ga";

import MaterialTable from 'material-table';
import { MTableToolbar } from 'material-table';
import { ArrowDownward, ArrowUpward, Search } from '@material-ui/icons'

import { withStyles } from '@material-ui/core';

const styles = theme => ({
		root:{
			width:'fit-content'
		},
		toolbar:{
			paddingLeft:'300px'
		},
	tablePaginationCaption: {
			fontSize: '15px'
		},
	tablePaginationSelect: {
			fontSize: '15px',
			lineHeight:'20px'
		}
	})

const rowsPerPageOptions = [10, 20, 50, 100]
const tbcellStyle = {textAlign: 'center'}

class HistoryComponent extends Component {
	constructor(props) {
		super(props)
		this.state = {
			response:[],
			responsesToDisplay:[],
			selectedTab: 0,
			page: 0,
			rowsPerPage: 10,
			searchValue: '',
			order : 'desc',
			orderBy : 'job_id'
		}
		this.queueFunc = this.queueFunc.bind(this)
		this.toggleTabs = this.toggleTabs.bind(this);
		this.refreshSuccess = this.refreshSuccess.bind(this)
		this.refreshFailure = this.refreshFailure.bind(this)
		//this.queueFunc();
		//this.interval = setInterval(this.queueFunc, 2000);    //making a queue request every 2 seconds

		updateGAPageView();
	}
	componentDidMount() {
		document.title = "OneDataShare - History";
		//this.queueFunc();
	}
	componentDidUpdate(prevProps) {
		
	}
	componentWillUnmount() {
		//clearInterval(this.interval);
	}
	queueFunc() {
		this.refreshTransfers()
	}
	paginateResults(results, page, limit) {
		let offset = page * limit
		return results.slice(offset, offset + limit)
	}
	refreshSuccess(resp) {
		const { page, rowsPerPage } = this.state
		let responsesToDisplay = this.paginateResults(resp.jobs, page, rowsPerPage)
		this.setState({
			response: resp.jobs,
			responsesToDisplay: responsesToDisplay,
			totalCount: resp.totalCount
		})
	}
	refreshFailure() {
		console.log('Error in queue request to API layer')
	}
	refreshTransfers() {
		const { searchValue, page, rowsPerPage, orderBy, order } = this.state
		fetchJobsForAdmin(
			searchValue,
			page,
			rowsPerPage,
			orderBy,
			order,
			this.refreshSuccess,
			this.refreshFailure
		)
	}
	getStatus(status, total, done) {
		//TODO: move to CSS file
		const style = {marginTop: '5%', fontWeight: 'bold', textTransform: 'capitalize'}
		let now, bsStyle, label
		if (status === 'complete') {
			now = 100
			bsStyle = 'info'
			label = 'Complete'
		} else if (status === 'failed') {
			now = 100
			bsStyle = 'danger'
			label = 'Failed'
		} else {
			now = ((done / total) * 100).toFixed()
			bsStyle = 'danger'
			label = `Transferring ${now}%`
		}
		return <ProgressBar
			bsStyle={bsStyle}
			label={label}
		/>
	}
	getFormattedDate(d){
		//TODO: use moment
		return (1 + d.getMonth() + '/' + d.getDate() + '/' + d.getFullYear() + ' ' + d.getHours() + ':' + d.getMinutes() + ':' + d.getSeconds());
	}
	infoButtonOnClick(jobID){
		// remove function
		var row = document.getElementById("info_" + jobID);
		if(this.selectedJobInfo === jobID && row.style.display !== "none"){
			row.style.display = "none";
		} else{
			var row2close = document.getElementById("info_" + this.selectedJobInfo);
			if(row2close)
				row2close.style.display = "none";
			row.style.display = "table-row";
			this.selectedJobInfo = jobID;
		}
	}
	closeAllInfoRows(){
		for(var i=0 ; i < this.infoRowsIds.length; i++){
			var infoRow = document.getElementById(this.infoRowsIds[i]);
			if(infoRow.style.display !== 'none')
				infoRow.style.display = 'none';
		}
		this.setState({selectedTab: 0});
	}
	cancelButtonClick(jobID){

	}
	toggleTabs(){
		const { selectedTab } = this.state
		this.setState({selectedTab: !selectedTab})
	}
	handleChangePage(event, nextPage) {
		const { page, rowsPerPage, response } = this.state
		this.setState({
			page: nextPage,
			responsesToDisplay: this.paginateResults(response, page, rowsPerPage)
		})
		// remove this block
		var x = document.getElementsByClassName("rohit");
		for (var i = 0; i < x.length; i++) {
			x[i].style.display = "none";
		}
		this.queueFunc()
	}
	handleChangeRowsPerPage(event) {		
		this.setState({page: 0, rowsPerPage: parseInt(event.target.value)})
		this.queueFunc()
	}
	handleRequestSort(property) {
		let defaultOrder = 'desc'
		let newOrder = defaultOrder
		const { order, orderBy } = this.state
		if (orderBy === property && order === defaultOrder) {
			newOrder = 'asc'
		}
		this.setState({order:order, orderBy:orderBy})
		this.queueFunc()
  }
	customToolbar() {
		return <form noValidate autoComplete="off">
			<TextField name="searchByOwner" label="Search By Owner" placeholder="Search by Owner"/>
		</form>
	}
}

class QueueComponent extends Component {
	constructor(props) {
		super(props)
		this.state = {
			response:[],
			responsesToDisplay:[],
			selectedTab: 0,
			page: 0,
			rowsPerPage: 10,
			searchValue: '',
			order : 'desc',
			orderBy : 'job_id',
			selectedRowId: null
		}
		this.queueFunc = this.queueFunc.bind(this)
		this.toggleTabs = this.toggleTabs.bind(this);
		this.refreshSuccess = this.refreshSuccess.bind(this)
		this.refreshFailure = this.refreshFailure.bind(this)
		this.toggleTabs = this.toggleTabs.bind(this)
		this.handleSearchChange = this.handleSearchChange.bind(this)
		this.handleSearch = this.handleSearch.bind(this)
		this.infoButtonOnClick = this.infoButtonOnClick.bind(this)
		//this.queueFunc();
		//this.interval = setInterval(this.queueFunc, 2000);    //making a queue request every 2 seconds

		updateGAPageView();
	}
	componentDidMount() {
		document.title = "OneDataShare - History";
		this.queueFunc();
	}
	componentWillUnmount() {
		//clearInterval(this.interval);
	}
	queueFunc() {
		this.refreshTransfers()
	}
	paginateResults(results, page, limit) {
		let offset = page * limit
		return results.slice(offset, offset + limit)
	}
	refreshSuccess(resp) {
		const { page, rowsPerPage } = this.state
		let responsesToDisplay = this.paginateResults(resp.jobs, page, rowsPerPage)
		this.setState({
			response: resp.jobs,
			responsesToDisplay: responsesToDisplay,
			totalCount: resp.totalCount
		})
	}
	refreshFailure() {
	}
	refreshTransfers() {
		const { searchValue, page, rowsPerPage, orderBy, order } = this.state
		fetchJobsForAdmin(
			searchValue,
			page,
			rowsPerPage,
			orderBy,
			order,
			this.refreshSuccess,
			this.refreshFailure
		)
	}
	getStatus(status, total, done) {
		//TODO: move to CSS file
		const style = {marginTop: '5%', fontWeight: 'bold', textTransform: 'capitalize'}
		let now, bsStyle, label
		if (status === 'complete') {
			now = 100
			bsStyle = 'info'
			label = 'Complete'
		} else if (status === 'failed') {
			now = 100
			bsStyle = 'danger'
			label = 'Failed'
		} else {
			now = ((done / total) * 100).toFixed()
			bsStyle = 'danger'
			label = `Transferring ${now}%`
		}
		return <ProgressBar
			bsStyle={bsStyle}
			label={label}
			now={now}
		/>
	}
	getFormattedDate(d) {
		return (1 + d.getMonth() + '/' + d.getDate() + '/' + d.getFullYear() + ' ' + d.getHours() + ':' + d.getMinutes() + ':' + d.getSeconds());
	}
	infoButtonOnClick(jobID) {
		const { selectedRowId } = this.state
		console.log(jobID)
		if (selectedRowId && selectedRowId === jobID) {
			this.setState({selectedRowId: null})
		} else {
			this.setState({selectedRowId: jobID})
		}
	}
	// deleteButtonOnClick(jobID, owner){
	// 	deleteJob(jobID, owner, (resp) => {
	// 		//success
	// 		this.queueFunc();
	// 	}, (resp) => {
	// 		//failed
	// 		console.log('Error in delete job request to API layer');
	// 	});
	// }
	closeAllInfoRows() {
		this.setState({selectedRowId: null})
	}
	cancelButtonClick(jobID){
		console.log('cancel')
	}
	toggleTabs() {
		const { selectedTab } = this.state
		this.setState({selectedTab: !selectedTab})
	}
	renderActions(jobID, status, owner){
		this.infoRowsIds = this.infoRowsIds || [];
		this.infoRowsIds.push("info_" + jobID);
		// Convert all these to CSS files
		return(
			<div>
				<Tooltip TransitionComponent={Zoom} placement="top" title="Detailed Information">
					<Button onClick={() => {this.infoButtonOnClick(jobID)}} variant="contained" size="small" color="primary"
						style={{backgroundColor: 'rgb(224, 224, 224)', color: '#333333', fontFamily: 'FontAwesome', fontSize: '1.5rem', height: '30%',
						fontWeight: 'bold', width: '20%', textTransform: 'none',
						minWidth: '0px', minHeigth: '0px'}}>
						<Info />
					</Button>
				</Tooltip>
				{status === 'transferring' &&
				<Tooltip TransitionComponent={Zoom} title="Cancel">
						<Button onClick={() => {this.cancelButtonOnClick(jobID)}}  variant="contained" size="small" color="primary"
							style={{backgroundColor: 'rgb(224, 224, 224)', color: '#333333', fontSize: '1.5rem', fontWeight: 'bold', width: '20%', height: '20%',
							textTransform: 'none', minWidth: '0px', minHeigth: '0px'}}>
							<Cancel />
						</Button>
					</Tooltip>
				}
				{/* {
					<Tooltip TransitionComponent={Zoom} title="Delete">
						<Button onClick={() => {this.deleteButtonOnClick(jobID, owner)}} disabled={deleted} variant="contained" size="small" color="primary"
							style={{backgroundColor: 'rgb(224, 224, 224)', color: '#333333', fontSize: '1.5rem', fontWeight: 'bold', width: '20%', height: '20%',
							textTransform: 'none', minWidth: '0px', minHeigth: '0px'}}>
							<DeleteOutline />
						</Button>
					</Tooltip>
				} */}
			</div>
		);
	}
	renderTabContent(resp){
		//XNOTE: unescape the source and destination fields
		if(this.state.selectedTab === 0){
			return(
				<Grid style={{ paddingTop : '0.5%', paddingBottom: '0.5%', width:'fit-content'}}>
					<Row>
						<Col md={6}><b>User</b></Col>
						<Col md={6}>{resp.owner}</Col>
					</Row>
					<Row>
						<Col md={6}><b>Job ID</b></Col>
						<Col md={6}>{resp.job_id}</Col>
					</Row>
					<Row>
						<Col md={6}><b>Source</b></Col>
						<Col md={6}>{resp.src.uri}</Col>
					</Row>
					<Row>
						<Col md={6}><b>Destination</b></Col>
						<Col md={6}>{resp.dest.uri}</Col>
					</Row>
					<Row>
						<Col md={6}><b>Instant Speed</b></Col>
						<Col md={6}>{humanReadableSpeed(resp.bytes.inst)}</Col>
					</Row>
					<Row>
						<Col md={6}><b>Average Speed</b></Col>
						<Col md={6}>{humanReadableSpeed(resp.bytes.avg)}</Col>
					</Row>
					<Row>
						<Col md={6}><b>Scheduled Time</b></Col>
						<Col md={6}>{this.getFormattedDate(new Date(resp.times.scheduled))}</Col>
					</Row>
					<Row>
						<Col md={6}><b>Started Time</b></Col>
						<Col md={6}>{this.getFormattedDate(new Date(resp.times.started))}</Col>
					</Row>
					<Row>
						<Col md={6}><b>Completed Time</b></Col>
						<Col md={6}>{this.getFormattedDate(new Date(resp.times.completed))}</Col>
					</Row>
					<Row>
						<Col md={6}><b>Time Duration</b></Col>
						<Col md={6}>{((resp.times.completed - resp.times.started)/1000).toFixed(2)} sec</Col>
					</Row>
					<Row>
						<Col md={6}><b>Attempts</b></Col>
						<Col md={6}>{resp.attempts}</Col>
					</Row>
					<Row>
						<Col md={6}><b>Status</b></Col>
						<Col md={6}>{resp.status}</Col>
					</Row>
				</Grid>
			);
		} else if (this.state.selectedTab === 1){
			return <pre>{JSON.stringify(resp, null, "\t")}</pre>
		}
	}
	handleChangePage(event, page) {
		const { response, rowsPerPage } = this.state
		let nextRecords = this.paginateResults(response, page, rowsPerPage)
		this.setState({
			page: page,
			responsesToDisplay: nextRecords,
			selectedRowId: null
		});
		this.queueFunc()
	}
	handleChangeRowsPerPage(event) {		
		this.setState({ page: 0, rowsPerPage: parseInt(event.target.value) })
		this.queueFunc()
	}
	handleRequestSort(property) {
		let defaultOrder = 'desc'
		let newOrder = defaultOrder
		const { order, orderBy } = this.state
		if (orderBy === property && order === defaultOrder) {
			newOrder = 'asc'
		}
		this.setState({order: newOrder, orderBy: property})
		this.queueFunc()
  }
	handleSearchChange(event) {
		this.setState({searchValue: event.target.value})
	}
	handleSearch(event) {
		event.preventDefault()
		this.queueFunc()
	}
	customToolbar() {
		const { searchValue } = this.state
		return <form onSubmit={this.handleSearch}>
			<input
				name="searchByOwner"
				label="Search By Owner"
				value={searchValue}
				onChange={this.handleSearchChange}
				placeholder='	Search By Owner'
			/>
		</form>
	}
	populateRows(rows) {
		const { selectedRowId } = this.state
		console.log(selectedRowId)
		return rows.map(row => {
			console.log(row)
			return <RowElement
				key={row.jobID}
				infoVisible={selectedRowId === row.job_id}
				resp={row}
				infoButtonOnClick={this.infoButtonOnClick}
			/>
		})
	}
	render() {
		const {
			totalCount,
			responsesToDisplay,
			rowsPerPage,
			rowsPerPageOptions,
			page,
			order,
			orderBy
		} = this.state
		const {classes} = this.props;
		const sortableColumns = {
			jobId: 'job_id',
			status: 'status',
			avgSpeed : "bytes.avg",
			source : "src.uri",
			userName: "owner"
		}
		console.log(this.state)
		return(
		<Paper className={classes.root} style={{marginLeft: '10%', marginRight: '10%', border: 'solid 2px #d9edf7'}}>
	  		<Table style={{display: "block"}}>
		        <TableHead style={{backgroundColor: '#d9edf7'}}>
		          <TableRow>
		            <TableCell style={{...tbcellStyle, width: '50%', fontSize: '2rem', color: '#31708f'}} colSpan='4'>
									Transfer History
								</TableCell>
		            <TableCell style={{...tbcellStyle, width: '50%', fontSize: '2rem', color: '#31708f'}} colSpan='2'>
									{ this.customToolbar() }
								</TableCell>
							</TableRow>
		          <TableRow>
		            <TableCell style={{...tbcellStyle, width: '7.5%',  fontSize: '2rem', color: '#31708f'}}>
								<Tooltip title="Sort on Username" placement='bottom-end' enterDelay={300}>
										<TableSortLabel
											active={orderBy === sortableColumns.userName}
											direction={order}
											id={"HistoryUsername"}
											onClick={() => {this.handleRequestSort(sortableColumns.userName)}}>
											Username
										</TableSortLabel>
									</Tooltip>
								</TableCell>
		            <TableCell style={{...tbcellStyle, width: '7.5%',  fontSize: '2rem', color: '#31708f'}}>
								<Tooltip title="Sort on Job ID" placement='bottom-end' enterDelay={300}>
										<TableSortLabel
											active={orderBy === sortableColumns.jobId}
											direction={order}
											id={"HistoryJobID"}
											onClick={() => {this.handleRequestSort(sortableColumns.jobId)}}>
											Job ID
										</TableSortLabel>
									</Tooltip>
								</TableCell>
		            <TableCell style={{...tbcellStyle, width: '45%',  fontSize: '2rem', color: '#31708f'}}>
								<Tooltip title="Sort on Progress" placement='bottom-end' enterDelay={300}>
										<TableSortLabel
											active={orderBy === sortableColumns.status}
											direction={order}
											id={"HistoryProgress"}
											onClick={() => {this.handleRequestSort(sortableColumns.status)}}>
											Progress
										</TableSortLabel>
									</Tooltip>
								</TableCell>
		            <TableCell style={{...tbcellStyle, width: '5%',  fontSize: '2rem', color: '#31708f'}}>
								<Tooltip title="Sort on Average Speed" placement='bottom-end' enterDelay={300}>
										<TableSortLabel
											active={orderBy === sortableColumns.avgSpeed}
											direction={order}
											id={"HistorySpeed"}
											onClick={() => {this.handleRequestSort(sortableColumns.avgSpeed)}}>
											Average Speed
										</TableSortLabel>
									</Tooltip>
								</TableCell>
		            <TableCell style={{...tbcellStyle, width: '30%',  fontSize: '2rem', color: '#31708f'}}>
								<Tooltip title="Sort on Source/Destination" placement='bottom-end' enterDelay={300}>
										<TableSortLabel
											active={orderBy === sortableColumns.source}
											direction={order}
											id={"HistorySD"}
											onClick={() => {this.handleRequestSort(sortableColumns.source)}}>
											Source/Destination
										</TableSortLabel>
									</Tooltip>
								</TableCell>
		            <TableCell style={{...tbcellStyle, width: '5%',  fontSize: '2rem', color: '#31708f'}}>Actions</TableCell>
		          </TableRow>
		        </TableHead>
		        <TableBody style={{height:'100%', display: "block"}}>
							{this.populateRows(responsesToDisplay)}
		        </TableBody>
						<TableFooter style={{textAlign:'center'}}>
							<TableRow>
								<TablePagination
									rowsPerPageOptions={rowsPerPageOptions}
									colSpan={6}
									count={totalCount}
									rowsPerPage={rowsPerPage}
									page={page}
									SelectProps={{
										native: true,
									}}
									onChangePage={this.handleChangePage}
									onChangeRowsPerPage={this.handleChangeRowsPerPage}
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
		);
	}
}

class RowElement extends React.PureComponent {
	constructor(props) {
		super(props)
		this.state = { selectedTab: 0 }
		this.toggleTabs = this.toggleTabs.bind(this)
	}
	toggleTabs() {
		const { selectedTab } = this.state
		this.setState({selectedTab: !selectedTab})
	}
	infoRow() {
		const { resp } = this.props
		const { selectedTab } = this.state
		return <TableRow>
			<TableCell colSpan={6} style={{...tbcellStyle, fontSize: '1rem', backgroundColor: '#e8e8e8', margin: '2%' }}>
				<div id="infoBox" style={{ marginBottom : '0.5%' }}>
					<AppBar position="static" style={{ boxShadow: 'unset' }}>
						<Tabs value={selectedTab ? 1: 0} onChange={this.toggleTabs} style={{ backgroundColor: '#e8e8e8' }}>
							<Tab style={{ backgroundColor: '#428bca', margin: '0.5%', borderRadius: '4px' }} label="Formatted" />
							<Tab style={{ backgroundColor: '#428bca', margin: '0.5%', borderRadius: '4px' }} label="JSON" />
						</Tabs>
					</AppBar>
					<div style={{ backgroundColor: 'white', borderRadius: '4px', textAlign: 'left', marginTop: '0.3%'}}>
						<TabContent resp={resp} selectedTab={selectedTab}/>
					</div>
				</div>
			</TableCell>
		</TableRow>
	}
	getStatus(status, total, done) {
		//TODO: move to CSS file
		const style = {marginTop: '5%', fontWeight: 'bold', textTransform: 'capitalize'}
		let now, bsStyle, label
		if (status === 'complete') {
			now = 100
			bsStyle = 'info'
			label = 'Complete'
		} else if (status === 'failed') {
			now = 100
			bsStyle = 'danger'
			label = 'Failed'
		} else {
			now = ((done / total) * 100).toFixed()
			bsStyle = 'danger'
			label = `Transferring ${now}%`
		}
		return <ProgressBar
			bsStyle={bsStyle}
			label={label}
			now={now}
		/>
	}
	renderActions(jobID, status) {
		const { infoButtonOnClick } = this.props
		// Convert all these to CSS files
		return <div>
			<Tooltip TransitionComponent={Zoom} placement="top" title="Detailed Information">
				<Button onClick={infoButtonOnClick.bind(null, jobID)} variant="contained" size="small" color="primary"
					style={{backgroundColor: 'rgb(224, 224, 224)', color: '#333333', fontFamily: 'FontAwesome', fontSize: '1.5rem', height: '30%',
					fontWeight: 'bold', width: '20%', textTransform: 'none',
					minWidth: '0px', minHeigth: '0px'}}>
					<Info />
				</Button>
			</Tooltip>
			{status === 'transferring' &&
			<Tooltip TransitionComponent={Zoom} title="Cancel">
				<Button onClick={() => {this.cancelButtonOnClick(jobID)}}  variant="contained" size="small" color="primary"
					style={{backgroundColor: 'rgb(224, 224, 224)', color: '#333333', fontSize: '1.5rem', fontWeight: 'bold', width: '20%', height: '20%',
					textTransform: 'none', minWidth: '0px', minHeigth: '0px'}}>
					<Cancel />
				</Button>
			</Tooltip>
			}
		</div>
	}
	render() {
		const { resp, infoVisible } = this.props
		console.log(infoVisible)
		return <React.Fragment>
			<TableRow style={{alignSelf: "stretch"}}>
				<TableCell scope="row" style={{...tbcellStyle, width: '7.5%',  fontSize: '1rem'}} numeric>
					{resp.owner}
				</TableCell>
				<TableCell style={{...tbcellStyle, width: '7.5%',  fontSize: '1rem'}}>
					{resp.job_id}
				</TableCell>
				<TableCell style={{...tbcellStyle, width: '45%',  fontSize: '1rem'}}>
					{this.getStatus(resp.status, resp.bytes.total, resp.bytes.done)}
				</TableCell>
				<TableCell style={{...tbcellStyle, width: '5%', maxWidth: '20vw', overflow:"hidden", fontSize: '1rem', margin: "0px", maxHeight: "10px"}}>
					{humanReadableSpeed(resp.bytes.avg)}
				</TableCell>
				<TableCell style={{...tbcellStyle, width: '30%',  fontSize: '1rem'}}>
					{decodeURIComponent(resp.src.uri)} <b>-></b> {decodeURIComponent(resp.dest.uri)}
				</TableCell>
				<TableCell style={{...tbcellStyle, width: '5%',  fontSize: '1rem'}}>
					{this.renderActions(resp.job_id, resp.status)}
				</TableCell>
			</TableRow>
			{ infoVisible && this.infoRow() }
		</React.Fragment>
	}
}

class TabContent extends React.PureComponent {
	render() {
		const { resp, selectedTab } = this.props
		if (selectedTab) {
			return <Grid style={{ paddingTop : '0.5%', paddingBottom: '0.5%', width:'fit-content'}}>
				<Row>
					<pre>{JSON.stringify(resp, null, "\t")}</pre>
				</Row>
			</Grid>
		} else {
			return <Grid style={{ paddingTop : '0.5%', paddingBottom: '0.5%', width:'fit-content'}}>
				<Row>
					<Col md={6}><b>User</b></Col>
					<Col md={6}>{resp.owner}</Col>
				</Row>
				<Row>
					<Col md={6}><b>Job ID</b></Col>
					<Col md={6}>{resp.job_id}</Col>
				</Row>
				<Row>
					<Col md={6}><b>Source</b></Col>
					<Col md={6}>{decodeURIComponent(resp.src.uri)}</Col>
				</Row>
				<Row>
					<Col md={6}><b>Destination</b></Col>
					<Col md={6}>{decodeURIComponent(resp.dest.uri)}</Col>
				</Row>
				<Row>
					<Col md={6}><b>Instant Speed</b></Col>
					<Col md={6}>{humanReadableSpeed(resp.bytes.inst)}</Col>
				</Row>
				<Row>
					<Col md={6}><b>Average Speed</b></Col>
					<Col md={6}>{humanReadableSpeed(resp.bytes.avg)}</Col>
				</Row>
				<Row>
					<Col md={6}><b>Scheduled Time</b></Col>
					<Col md={6}>{moment(resp.times.scheduled).format("DD-MM-YYYY HH:mm:ss")}</Col>
				</Row>
				<Row>
					<Col md={6}><b>Started Time</b></Col>
					<Col md={6}>{moment(resp.times.started).format("DD-MM-YYYY HH:mm:ss")}</Col>
				</Row>
				<Row>
					<Col md={6}><b>Completed Time</b></Col>
					<Col md={6}>{moment(resp.times.completed).format("DD-MM-YYYY HH:mm:ss")}</Col>
				</Row>
				<Row>
					<Col md={6}><b>Time Duration</b></Col>
					<Col md={6}>{((resp.times.completed - resp.times.started)/1000).toFixed(2)} sec</Col>
				</Row>
				<Row>
					<Col md={6}><b>Attempts</b></Col>
					<Col md={6}>{resp.attempts}</Col>
				</Row>
				<Row>
					<Col md={6}><b>Status</b></Col>
					<Col md={6}>{resp.status}</Col>
				</Row>
			</Grid>
		}
	}
} 

export default withStyles(styles)(QueueComponent) 
