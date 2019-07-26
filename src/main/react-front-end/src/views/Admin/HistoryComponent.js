import React, { Component } from 'react';
import { queue } from '../../APICalls/APICalls';

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

class QueueComponent extends Component {

	constructor(){
		super();
		this.state = {response:[],
						responsesToDisplay:[],
					  selectedTab: 0,
						page: 0,
						rowsPerPage: 10,
						rowsPerPageOptions : [10, 20, 50, 100],
						order : 'desc',
						orderBy : 'job_id'};

		this.queueFunc = this.queueFunc.bind(this)
		this.queueFunc();
		setInterval(this.queueFunc, 2000);    //making a queue request every 2 seconds

		var infoRowsIds= [];
		this.toggleTabs = this.toggleTabs.bind(this);
	}

	queueFunc = () => {
		let isHistory = true;

		queue(isHistory, this.state.page, this.state.rowsPerPage, this.state.orderBy, this.state.order,(resp) => {
		//success
		let responsesToDisplay = [];
		if(this.state.page == 0){
			responsesToDisplay = resp.jobs.slice(0, this.state.rowsPerPage);
		}
		else{
			responsesToDisplay = resp.jobs.slice(this.state.rowsPerPage, 2 * this.state.rowsPerPage);
		}

		this.setState({response:resp.jobs, responsesToDisplay: responsesToDisplay, totalCount: resp.totalCount});

	}, (resp) => {
		//failed
		console.log('Error in queue request to API layer');
	})};

	getStatus(status, total, done){
		const style = {marginTop: '5%', fontWeight: 'bold'};
		if(status === 'complete'){
			return(<ProgressBar now={100} label={'Complete'} style={style} />);
		}
		else if(status === 'failed'){
			return(<ProgressBar bsStyle="danger" now={100} style={style} label={'Failed'} />);
		}
		else{
			var percentCompleted = ((done/total) * 100).toFixed();
			return(<ProgressBar bsStyle="danger" now={percentCompleted} style={style} label={'Processing ' + percentCompleted + '%'} />);
		}
	}

	renderSpeed(speedInBps){
		var displaySpeed = "";
		if(speedInBps > 1000000000){
			displaySpeed = (speedInBps/1000000000).toFixed(2) + " GB/s";
		}
		else if(speedInBps > 1000000){
			displaySpeed = (speedInBps/1000000).toFixed(2) + " MB/s";
		}
		else if(speedInBps > 1000){
			displaySpeed = (speedInBps/1000).toFixed(2) + " KB/s";
		}
		else{
			displaySpeed = speedInBps + " B/s";
		}

		return displaySpeed;
	}

	infoButtonOnClick(jobID){
		var row = document.getElementById("info_" + jobID);
        		if(this.selectedJobInfo === jobID && row.style.display != "none"){
        			row.style.display = "none";
        		}
        		else{
        			var row2close = document.getElementById("info_" + this.selectedJobInfo);
        			if(row2close)
        				row2close.style.display = "none";
        			row.style.display = "table-row";
        			this.selectedJobInfo = jobID;
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

	closeAllInfoRows(){
		for(var i=0 ; i < this.infoRowsIds.length; i++){
			var infoRow = document.getElementById(this.infoRowsIds[i]);
			if(infoRow.style.display != 'none')
				infoRow.style.display = 'none';
		}
		this.setState({selectedTab: 0});
	}

	cancelButtonClick(jobID){

	}

	toggleTabs(){
		if(this.state.selectedTab === 0)
			this.setState({selectedTab: 1});
		else
			this.setState({selectedTab: 0});
	}

	renderActions(jobID, status, owner){
		this.infoRowsIds = this.infoRowsIds || [];
		this.infoRowsIds.push("info_" + jobID);
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
				{status == 'processing' &&
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

	decodeURIComponent(url) {
	    return decodeURIComponent(url);
	}


	renderSpeed(speedInBps){
    		var displaySpeed = "";
    		if(speedInBps > 1000000000){
    			displaySpeed = (speedInBps/1000000000).toFixed(2) + " GB/s";
    		}
    		else if(speedInBps > 1000000){
    			displaySpeed = (speedInBps/1000000).toFixed(2) + " MB/s";
    		}
    		else if(speedInBps > 1000){
    			displaySpeed = (speedInBps/1000).toFixed(2) + " KB/s";
    		}
    		else{
    			displaySpeed = speedInBps + " B/s";
    		}

    		return displaySpeed;
    	}


	renderTabContent(resp){
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
						<Col md={6}>{this.renderSpeed(resp.bytes.inst)}</Col>
					</Row>
					<Row>
						<Col md={6}><b>Average Speed</b></Col>
						<Col md={6}>{this.renderSpeed(resp.bytes.avg)}</Col>
					</Row>
					<Row>
						<Col md={6}><b>Scheduled Time</b></Col>
						<Col md={6}>TBD</Col>
					</Row>
					<Row>
						<Col md={6}><b>Started Time</b></Col>
						<Col md={6}>TBD</Col>
					</Row>
					<Row>
						<Col md={6}><b>Completed Time</b></Col>
						<Col md={6}>TBD</Col>
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
		}
		else if (this.state.selectedTab === 1){
			return(
            				<pre>{JSON.stringify(resp, null, "\t")}</pre>
            );
		}
	}
	handleChangePage = (event, page) => {
		console.log("The page is:" + page)
		let nextRecords
		if(page > this.state.page){
			// Moving to next page
			nextRecords = this.state.response.slice(this.state.rowsPerPage, 2 * this.state.rowsPerPage)
		}
		else{
			// Moving to previous page
			nextRecords = this.state.response.slice(0, this.state.rowsPerPage)
		}
		this.state.page=page
		
		this.setState({ page, response: this.state.response, responsesToDisplay: nextRecords});
        var x = document.getElementsByClassName("rohit");

                for (var i = 0; i < x.length; i++) {
                  x[i].style.display = "none";
                }
		this.queueFunc()
	};

	handleChangeRowsPerPage = event => {		
		this.state.page=0
		this.state.rowsPerPage = parseInt(event.target.value)
		this.setState({ page: 0, rowsPerPage: parseInt(event.target.value) });
		this.queueFunc()
	};

	handleRequestSort = (property) => {
    const orderBy = property;
    let order = 'desc';

    if (this.state.orderBy === property && this.state.order === 'desc') {
      order = 'asc';
    }
		this.setState({ order:order, orderBy:orderBy });
		this.state.order=order
		this.state.orderBy = orderBy
		this.queueFunc()
  };


	render(){
		const height = window.innerHeight+"px";
		const {response, totalCount, responsesToDisplay} = this.state;
		const tbcellStyle= {textAlign: 'center'}
		const {rowsPerPage, rowsPerPageOptions, page, order, orderBy} = this.state;
		const {classes} = this.props;
		const sortableColumns = {
			jobId: 'job_id',
			status: 'status',
			avgSpeed : "bytes.avg",
			source : "src.uri",
			userName: "owner"
		}
		var tableRows = [];
		responsesToDisplay.map(resp => {
	      	 tableRows.push(
	      	 	<TableRow style={{alignSelf: "stretch"}}>
		            <TableCell component="th" scope="row" style={{...tbcellStyle, width: '7.5%',  fontSize: '1rem'}} numeric>
		              {resp.owner}
		            </TableCell>
		            <TableCell style={{...tbcellStyle, width: '40%',  fontSize: '1rem'}}>
		            	{resp.job_id}
		            </TableCell>
		            <TableCell style={{...tbcellStyle, width: '40%',  fontSize: '10rem'}}>
                        {this.getStatus(resp.status, resp.bytes.total, resp.bytes.done)}
                    </TableCell>
		            <TableCell style={{...tbcellStyle, width: '35%', maxWidth: '20vw', overflow:"hidden", fontSize: '1rem', margin: "0px", maxHeight: "10px"}}>
		            	{this.renderSpeed(resp.bytes.avg)}
		            </TableCell>
		            <TableCell style={{...tbcellStyle, width: '10%',  fontSize: '1rem'}}>
		            	{this.decodeURIComponent(resp.src.uri)} <b>-></b> {this.decodeURIComponent(resp.dest.uri)}
		            </TableCell>
		            <TableCell style={{...tbcellStyle, width: '10%',  fontSize: '1rem'}}>
									{this.renderActions(resp.job_id, resp.status,resp.owner)}
                </TableCell>
	          	</TableRow>
	        );
	      	tableRows.push(
	      	 	<TableRow id={"info_" + resp.job_id} class="rohit" style={{ display: 'none'}}>
	            	<TableCell colSpan={6} style={{...tbcellStyle, width: '10%',  fontSize: '1rem', backgroundColor: '#e8e8e8', margin: '2%' }}>
	            		<div id="infoBox" style={{ marginBottom : '0.5%' }}>
		            		<AppBar position="static" style={{ boxShadow: 'unset' }}>
								<Tabs value={this.state.selectedTab} onChange={this.toggleTabs} style={{ backgroundColor: '#e8e8e8' }}>
									<Tab style={{ backgroundColor: '#428bca', margin: '0.5%', borderRadius: '4px' }} label="Formatted" />
			            			<Tab style={{ backgroundColor: '#428bca', margin: '0.5%', borderRadius: '4px' }} label="JSON" />
		          				</Tabs>
		        			</AppBar>
		        			<div style={{ backgroundColor: 'white', borderRadius: '4px', textAlign: 'left', marginTop: '0.3%'}}>
		        				{this.renderTabContent(resp)}
		        			</div>
		        		</div>
	            	</TableCell>
	          	</TableRow>
	        );
		});

		return(
		<Paper className={classes.root} style={{marginLeft: '10%', marginRight: '10%', marginTop: '5%', border: 'solid 2px #d9edf7'}}>
	  		<Table style={{display: "block"}}>
		        <TableHead style={{backgroundColor: '#d9edf7'}}>
		          <TableRow>
		            <TableCell style={{...tbcellStyle, width: '7.5%',  fontSize: '2rem', color: '#31708f'}}>
								<Tooltip title="Sort on Username" placement='bottom-end' enterDelay={300}>
										<TableSortLabel
											active={orderBy === sortableColumns.userName}
											direction={order}
											onClick={() => {this.handleRequestSort(sortableColumns.userName)}}>
											Username
										</TableSortLabel>
									</Tooltip>
								</TableCell>
		            <TableCell style={{...tbcellStyle, width: '40%',  fontSize: '2rem', color: '#31708f'}}>
								<Tooltip title="Sort on Job ID" placement='bottom-end' enterDelay={300}>
										<TableSortLabel
											active={orderBy === sortableColumns.jobId}
											direction={order}
											onClick={() => {this.handleRequestSort(sortableColumns.jobId)}}>
											Job ID
										</TableSortLabel>
									</Tooltip>
								</TableCell>
		            <TableCell style={{...tbcellStyle, width: '7.5%',  fontSize: '2rem', color: '#31708f'}}>
								<Tooltip title="Sort on Progress" placement='bottom-end' enterDelay={300}>
										<TableSortLabel
											active={orderBy === sortableColumns.status}
											direction={order}
											onClick={() => {this.handleRequestSort(sortableColumns.status)}}>
											Progress
										</TableSortLabel>
									</Tooltip>
								</TableCell>
		            <TableCell style={{...tbcellStyle, width: '35%',  fontSize: '2rem', color: '#31708f'}}>
								<Tooltip title="Sort on Average Speed" placement='bottom-end' enterDelay={300}>
										<TableSortLabel
											active={orderBy === sortableColumns.avgSpeed}
											direction={order}
											onClick={() => {this.handleRequestSort(sortableColumns.avgSpeed)}}>
											Average Speed
										</TableSortLabel>
									</Tooltip>
								</TableCell>
		            <TableCell style={{...tbcellStyle, width: '10%',  fontSize: '2rem', color: '#31708f'}}>
								<Tooltip title="Sort on Source/Destination" placement='bottom-end' enterDelay={300}>
										<TableSortLabel
											active={orderBy === sortableColumns.source}
											direction={order}
											onClick={() => {this.handleRequestSort(sortableColumns.source)}}>
											Source/Destination
										</TableSortLabel>
									</Tooltip>
								</TableCell>
		            <TableCell style={{...tbcellStyle, width: '10%',  fontSize: '2rem', color: '#31708f'}}>Actions</TableCell>
		          </TableRow>
		        </TableHead>
		        <TableBody style={{height:'620px', overflowY: 'scroll', display: "block"}}>
		            {tableRows}

		        </TableBody>
						<TableFooter style={{textAlign:'center'}}>
							<TableRow>
								<TablePagination
									rowsPerPageOptions={rowsPerPageOptions}
									colSpan={4}
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

export default withStyles(styles)(QueueComponent) 
