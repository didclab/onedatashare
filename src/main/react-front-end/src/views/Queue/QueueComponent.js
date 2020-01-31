import React, { Component } from 'react';
import { cancelJob, restartJob, deleteJob, updateJobStatus, fetchUserJobs } from '../../APICalls/APICalls';
import { eventEmitter } from '../../App'
import moment from 'moment'
import { humanReadableSpeed } from '../../utils'

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

import Zoom from '@material-ui/core/Zoom';
import DeleteOutline from '@material-ui/icons/DeleteOutline';
import Refresh from '@material-ui/icons/Refresh';
import Info from '@material-ui/icons/Info';
import Cancel from '@material-ui/icons/Cancel';
import TableSortLabel from '@material-ui/core/TableSortLabel';
import Tooltip from '@material-ui/core/Tooltip';
import TablePagination from '@material-ui/core/TablePagination';
import TableFooter from '@material-ui/core/TableFooter';
import TablePaginationActions from '../TablePaginationActions';
import CircularProgress from '@material-ui/core/CircularProgress'

import { updateGAPageView } from '../../analytics/ga';
import { jobStatus } from '../../constants';

import { withStyles } from '@material-ui/core';
const styles = theme => ({
	root: {
		width: 'fit-content'
	},
	toolbar: {
		paddingLeft: '300px'
	},
	tablePaginationCaption: {
		fontSize: '15px'
	},
	tablePaginationSelect: {
		fontSize: '15px',
		lineHeight: '20px'
	}
})

const rowsPerPageOptions = [10, 20, 50, 100]
const tbcellStyle = {textAlign: 'center'}

class QueueComponent extends Component {
	constructor(props) {
		super(props)
		this.state = {
			response: [],
			responsesToDisplay: [],
			selectedTab: 0,
			page: 0,
			rowsPerPage: 10,
			searchValue: '',
			order : 'desc',
			orderBy : 'job_id',
			selectedRowId: null,
			totalCount: 0,
			loading: true,
		}

		this.update = this.update.bind(this)
		this.queueFunc = this.queueFunc.bind(this)
		this.toggleTabs = this.toggleTabs.bind(this);
		this.toggleTabs = this.toggleTabs.bind(this)
		this.infoButtonOnClick = this.infoButtonOnClick.bind(this)
		this.cancelButtonOnClick = this.cancelButtonOnClick.bind(this)
		this.restartButtonOnClick = this.restartButtonOnClick.bind(this)
		this.deleteButtonOnClick = this.deleteButtonOnClick.bind(this)
		this.handleChangeRowsPerPage = this.handleChangeRowsPerPage.bind(this)
		this.handleChangePage	= this.handleChangePage.bind(this)
		this.interval = setInterval(this.update, 2000) //making a queue request every 2 seconds
		this.queueFuncSuccess = this.queueFuncSuccess.bind(this)
		this.queueFuncFail = this.queueFuncFail.bind(this)

		updateGAPageView()
	}
	componentDidMount() {
		document.title = "OneDataShare - Queue"
		this.queueFunc()
	}
	componentWillUnmount() {
		clearInterval(this.interval)
	}
	componentDidUpdate(prevProps, prevState) {
		const {
			page: prevPage,
			rowsPerPage: prevRowsPerPage,
			orderBy: prevOrderBy,
			order: prevOrder,
			response: prevResponse,
			loading: prevLoading
		} = prevState
		const { loading, response, page, rowsPerPage, orderBy, order } = this.state
		if ((!prevLoading && loading !== prevLoading) || response.length !== prevResponse.length ||
			page !== prevPage || rowsPerPage !== prevRowsPerPage || orderBy !== prevOrderBy ||
			order !== prevOrder) {
			this.queueFunc()
		}
	}
	update() {
		const { responsesToDisplay } = this.state
		let jobIds = []
		responsesToDisplay.forEach(job => {
			if (job.status === jobStatus.TRANSFERRING || job.status === jobStatus.SCHEDULED) {
				jobIds.push(job.uuid)
			}
		})
		if (jobIds.length > 0) {
			updateJobStatus(jobIds)
				.then(resp => {
					let jobs = resp
					//TODO: use hash keys and values instead of finding on each update
					let existingData = [...responsesToDisplay]
					jobs.forEach(job => {
						let existingJob = existingData.find(item => item.uuid === job.uuid)
						existingJob.status = job.status
						existingJob.bytes.total = job.bytes.total
						existingJob.bytes.done = job.bytes.done
						existingJob.bytes.avg = job.bytes.avg
					})
					this.setState({responsesToDisplay: existingData})
				})
				.catch(resp => {
					console.log('Failed to get job updates')
				})
		}
	}
	paginateResults(results, page, limit) {
		let offset = page * limit
		return results.slice(offset, offset + limit)
	}
	queueFuncSuccess(resp) {
		const { page, rowsPerPage } = this.state
		//success
		let responsesToDisplay = this.paginateResults(resp.jobs, page, rowsPerPage)
		this.setState({
			response: resp.jobs,
			responsesToDisplay: responsesToDisplay,
			totalCount: resp.totalCount,
			loading: false
		})
	}
	queueFuncFail(resp) {
		//failed
		console.log(resp)
		console.log('Error in queue request to API layer');
	}
	queueFunc(isHistory = false) {
		const { page, rowsPerPage, orderBy, order } = this.state
		fetchUserJobs(
			page,
			rowsPerPage,
			orderBy,
			order,
			this.queueFuncSuccess,
			this.queueFuncFail
		)
	}
	getStatus(status, total, done) {
		const style = { marginTop: '5%', fontWeight: 'bold' };
		if (status === 'complete') {
			return (<ProgressBar now={100} label={'Complete'} style={style} />);
		}
		else if (status === 'failed' ) {
			return (<ProgressBar bsStyle="danger" now={100} style={style} label={'Failed'} />);
		}
		else if (status === 'removed' || status === "cancelled") {
			return (<ProgressBar bsStyle="danger" now={100} style={style} label={'Cancelled'} />);
		}
		else {
			let percentCompleted = Math.ceil(((done / total) * 100));
			return (<ProgressBar bsStyle="warning" striped now={percentCompleted} style={style} label={'Transferring ' + percentCompleted + '%'} />);
		}
	}
	infoButtonOnClick(owner, jobID) {
		const { selectedRowId } = this.state
		let identifier = `${owner}-${jobID}`
		if (selectedRowId && selectedRowId === identifier) {
			this.setState({selectedRowId: null})
		} else {
			this.setState({selectedRowId: identifier})
		}
	}
	cancelButtonOnClick(jobID) {
		cancelJob(jobID, () => {
			//success
			this.queueFunc();
		}, (resp) => {
			//failed
			console.log('Error in cancel request to API layer');
		});
	}
	restartButtonOnClick(jobID) {
		restartJob(jobID, () => {
			//success
			this.queueFunc()
		}, () => {
			//failed
			var msg = 'Restart job failed since either or both credentials of the job do not exist'
			console.log(msg)
			eventEmitter.emit("errorOccured", msg)
		});
	}
	deleteButtonOnClick(jobID) {
		deleteJob(jobID, () => {
			//success
			this.queueFunc()
		}, () => {
			//failed
			console.log('Error in delete job request to API layer')
		})
	}
	toggleTabs() {
		const { selectedTab } = this.state
		this.setState({selectedTab: !selectedTab})
	}
	handleChangePage(event, page) {
		const { response, rowsPerPage } = this.state
		let nextRecords = this.paginateResults(response, page, rowsPerPage)
		this.setState({
			page: page,
			responsesToDisplay: nextRecords,
			selectedRowId: null,
			loading: true
		});
	}
	handleChangeRowsPerPage(event) {		
		this.setState({ page: 0, rowsPerPage: parseInt(event.target.value), loading: true })
	}
	handleRequestSort(property) {
		let defaultOrder = 'desc'
		let newOrder = defaultOrder
		const { order, orderBy } = this.state
		if (orderBy === property && order === defaultOrder) {
			newOrder = 'asc'
		}
		this.setState({order: newOrder, orderBy: property, loading: true})
  }
	populateRows(rows) {
		const { selectedRowId } = this.state
		return rows.map(row => {
			let identifier = `${row.owner}-${row.job_id}`
			return <RowElement
				key={identifier}
				infoVisible={selectedRowId === identifier}
				resp={row}
				infoButtonOnClick={this.infoButtonOnClick}
				cancelButtonOnClick={this.cancelButtonOnClick}
				restartButtonOnClick={this.restartButtonOnClick}
				deleteButtonOnClick={this.deleteButtonOnClick}
			/>
		})
	}
	render() {
		const {
			totalCount,
			responsesToDisplay,
			rowsPerPage,
			page,
			order,
			orderBy,
			loading
		} = this.state
		const {classes} = this.props;
		const sortableColumns = {
			jobId: 'job_id',
			status: 'status',
			avgSpeed : "bytes.avg",
			source : "src.uri",
			// userName: "owner",
			startTime: 'times.started'
		}
		return <Paper className={classes.root} style={{marginLeft: '10%', marginRight: '10%', border: 'solid 2px #d9edf7'}}>
			<Table style={{display: "block"}}>
				<TableHead style={{backgroundColor: '#d9edf7'}}>
					<TableRow>
						{/* <TableCell style={{...tbcellStyle, width: '15%',  fontSize: '2rem', color: '#31708f'}}>
							<Tooltip title="Sort on Username" placement='bottom-end' enterDelay={300}>
								<TableSortLabel
									active={orderBy === sortableColumns.userName}
									direction={order}
									onClick={() => {this.handleRequestSort(sortableColumns.userName)}}>
									Username
								</TableSortLabel>
							</Tooltip>
						</TableCell> */}
						<TableCell style={{...tbcellStyle, width: '5%',  fontSize: '2rem', color: '#31708f'}}>
							<Tooltip title="Sort on Job ID" placement='bottom-end' enterDelay={300}>
								<TableSortLabel
									active={orderBy === sortableColumns.jobId}
									direction={order}
									onClick={() => {this.handleRequestSort(sortableColumns.jobId)}}>
									Job ID
								</TableSortLabel>
							</Tooltip>
						</TableCell>
						<TableCell style={{...tbcellStyle, width: '30%',  fontSize: '2rem', color: '#31708f'}}>
							<Tooltip title="Sort on Progress" placement='bottom-end' enterDelay={300}>
								<TableSortLabel
									active={orderBy === sortableColumns.status}
									direction={order}
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
									onClick={() => {this.handleRequestSort(sortableColumns.avgSpeed)}}>
									Average Speed
								</TableSortLabel>
							</Tooltip>
						</TableCell>
						<TableCell style={{...tbcellStyle, width: '25%',  fontSize: '2rem', color: '#31708f'}}>
							<Tooltip title="Sort on Source/Destination" placement='bottom-end' enterDelay={300}>
								<TableSortLabel
									active={orderBy === sortableColumns.source}
									direction={order}
									onClick={() => {this.handleRequestSort(sortableColumns.source)}}>
									Source/Destination
								</TableSortLabel>
							</Tooltip>
						</TableCell>
						<TableCell style={{...tbcellStyle, width: '15%',  fontSize: '2rem', color: '#31708f'}}>Actions</TableCell>
					</TableRow>
				</TableHead>
				<TableBody style={{height:'100%', display: "block"}}>
					{ loading ?
						<div style={{textAlign: 'center'}}>
							<CircularProgress />
						</div>
						:
						this.populateRows(responsesToDisplay)
					}
				</TableBody>
				<TableFooter style={{textAlign:'center'}}>
					<TableRow>
						<TablePagination
							rowsPerPageOptions={rowsPerPageOptions}
							colSpan={7}
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
			<TableCell colSpan={7} style={{...tbcellStyle, fontSize: '1rem', backgroundColor: '#e8e8e8', margin: '2%' }}>
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
	renderActions(owner, jobID, status, deleted) {
		const { infoButtonOnClick, cancelButtonOnClick, restartButtonOnClick, deleteButtonOnClick } = this.props
		// Convert all these to CSS files
		return <div>
			<Tooltip TransitionComponent={Zoom} placement="top" title="Detailed Information">
				<Button onClick={infoButtonOnClick.bind(null, owner, jobID)} variant="contained" size="small" color="primary"
					style={{
					backgroundColor: 'rgb(224, 224, 224)', color: '#333333', fontFamily: 'FontAwesome', fontSize: '1.5rem', height: '30%',
					fontWeight: 'bold', width: '20%', textTransform: 'none',
					minWidth: '0px', minHeigth: '0px'
					}}>
					<Info />
				</Button>
			</Tooltip>
			{(status === 'transferring' || status === 'scheduled' || status === 'transferring') &&
			<Tooltip TransitionComponent={Zoom} title="Cancel">
				<Button onClick={cancelButtonOnClick.bind(null, jobID)} variant="contained" size="small" color="primary"
					style={{
					backgroundColor: 'rgb(224, 224, 224)', color: '#333333', fontSize: '1.5rem', fontWeight: 'bold', width: '20%', height: '20%',
					textTransform: 'none', minWidth: '0px', minHeigth: '0px'
					}}>
					<Cancel />
				</Button>
			</Tooltip>
			}
			{status !== 'transferring' && status !== 'scheduled' &&
			<Tooltip TransitionComponent={Zoom} title="Restart">
				<Button onClick={restartButtonOnClick.bind(null, jobID)} variant="contained" size="small" color="primary"
					style={{
					backgroundColor: 'rgb(224, 224, 224)', color: '#333333', fontSize: '1.5rem', fontWeight: 'bold', width: '20%', height: '20%',
					textTransform: 'none', minWidth: '0px', minHeigth: '0px'
					}}>
					<Refresh />
				</Button>
			</Tooltip>
			}
			{status !== 'transferring' && status !== 'scheduled' && !deleted &&
			<Tooltip TransitionComponent={Zoom} title="Delete">
				<Button onClick={deleteButtonOnClick.bind(null, jobID)} variant="contained" size="small" color="primary"
					style={{
					backgroundColor: 'rgb(224, 224, 224)', color: '#333333', fontSize: '1.5rem', fontWeight: 'bold', width: '20%', height: '20%',
					textTransform: 'none', minWidth: '0px', minHeigth: '0px'
					}}>
					<DeleteOutline />
				</Button>
			</Tooltip>
			}
		</div>
	}
	render() {
		const { resp, infoVisible } = this.props
		return <React.Fragment>
			<TableRow style={{alignSelf: "stretch"}}>
				{/* <TableCell scope="row" style={{...tbcellStyle, width: '15%',  fontSize: '1.2rem'}}>
					{resp.owner}
				</TableCell> */}
				<TableCell style={{...tbcellStyle, width: '5%',  fontSize: '1.2rem'}} numeric="true">
					{resp.job_id}
				</TableCell>
				<TableCell style={{...tbcellStyle, width: '30%',  fontSize: '1.2rem'}}>
					{this.getStatus(resp.status, resp.bytes.total, resp.bytes.done)}
				</TableCell>
				<TableCell style={{...tbcellStyle, width: '5%', maxWidth: '20vw', overflow:"hidden", fontSize: '1.2rem', margin: "0px", maxHeight: "10px"}}>
					{humanReadableSpeed(resp.bytes.avg)}
				</TableCell>
				<TableCell style={{...tbcellStyle, width: '25%',  fontSize: '1.2rem'}}>
					{decodeURIComponent(resp.src.uri)} <b>-></b> {decodeURIComponent(resp.dest.uri)}
				</TableCell>
				<TableCell style={{...tbcellStyle, width: '15%',  fontSize: '1.2rem'}}>
					{this.renderActions(resp.owner, resp.job_id, resp.status, resp.deleted)}
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
