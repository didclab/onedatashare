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
import { cancelJob, restartJob, deleteJob, getJobUpdatesForUser, getJobsForUser } from '../../APICalls/APICalls';
import { eventEmitter } from '../../App';
import { updateGAPageView } from '../../analytics/ga';
import { jobStatus } from '../../constants';
import QueueView from "./QueueView";
import RowElement from "./QueueTableRow/RowElement/RowElement";

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
			order: 'desc',
			orderBy: 'createTime',
			selectedRowId: null,
			totalCount: 0,
			loading: true,
			pollCountdown: 5,
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
		this.handleChangePage = this.handleChangePage.bind(this)
		this.queueFuncSuccess = this.queueFuncSuccess.bind(this)
		this.queueFuncFail = this.queueFuncFail.bind(this)
		updateGAPageView()
	}
	
	componentDidMount() {
		document.title = "OneDataShare - Queue"
		this.queueFunc()
		this.startInterval()
	}

	startInterval() {
		this.interval = setInterval(() => {
			this.setState({pollCountdown: this.state.pollCountdown - 1})
			// Reason for -1 is because when reached 0, we want to wait 1 more second to accomodate for decimal seconds
			if (this.state.pollCountdown === -1) {
				this.update()
				this.setState({pollCountdown: 5})
			}
		}, 1000);
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
		const {loading, response, page, rowsPerPage, orderBy, order} = this.state
		if ((!prevLoading && loading !== prevLoading) || response.length !== prevResponse.length ||
			page !== prevPage || rowsPerPage !== prevRowsPerPage || orderBy !== prevOrderBy ||
			order !== prevOrder) {
			this.queueFunc()
		}
	}

	update() {
		const statusSet = new Set(["STARTED", "STARTING", "EXECUTING", "STOPPING"])
		const {responsesToDisplay} = this.state

		for (const job of responsesToDisplay) {
			if (job.status in statusSet) {
				const resp = await new Promise((resolve, reject) => {
					getJobUpdatesForUser(job.id, resolve, reject)
				})
				const data = resp[0].data
				const index = responsesToDisplay.findIndex(obj => obj.id === data.id)
				if (index != -1) {
					responsesToDisplay[index] = data
				}
				else {
					responsesToDisplay.push(data)
				}
			}
		}
			// existingJob.status = resp.status
			// existingJob.bytes.total = resp.bytes.total
			// existingJob.bytes.done = resp.bytes.done
			// existingJob.bytes.avg = resp.bytes.avg
	}
	paginateResults(results, page, limit) {
		let offset = page * limit
		return results.slice(offset, offset + limit)
	}

	queueFuncSuccess = (resp) => {
		// const { page, rowsPerPage } = this.state
		//success
		//let responsesToDisplay = this.paginateResults(resp.jobs, page, rowsPerPage);
		//commented to fix second page render issue as it slices all jobs and returns null object
		const filterSet = new Set(["STARTED", "STARTING", "EXECUTING", "STOPPING"])
		let filteredResponse = []
		for (const job of resp.content) {
			if (job.status in filterSet) {
				filteredResponse.push(job)
			}
		}
		this.setState({
			response: filteredResponse,
			responsesToDisplay: resp.content,
			totalCount: resp.totalElements,
			loading: false
		});
	}

	queueFuncFail(resp) {
		//failed
		console.error('Error in queue request to API layer response: ' + resp);
	}

	queueFunc(isHistory = false) {
		const {page, rowsPerPage, orderBy, order} = this.state
		getJobsForUser(
			page,
			rowsPerPage,
			orderBy,
			order,
			this.queueFuncSuccess,
			this.queueFuncFail
		);
	}

	infoButtonOnClick(owner, jobID) {
		const {selectedRowId} = this.state
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
		const {selectedTab} = this.state
		this.setState({selectedTab: !selectedTab})
	}

	handleChangePage(event, page) {
		const {response, rowsPerPage} = this.state
		let nextRecords = this.paginateResults(response, page, rowsPerPage)
		this.setState({
			page: page,
			responsesToDisplay: nextRecords,
			selectedRowId: null,
			loading: true
		});
	}

	handleChangeRowsPerPage = (event) => {
		this.setState({page: 0, rowsPerPage: parseInt(event.target.value), loading: true})
	}

	handleRequestSort = (property) => {
		console.log(property)
		let defaultOrder = 'desc'
		let newOrder = defaultOrder
		const { order, orderBy} = this.state;
		if (orderBy === property && order === defaultOrder) {
			newOrder = 'asc'
		}
		this.setState({order: newOrder, orderBy: property, loading: true})
	}

	populateRows = () => {
		const {selectedRowId} = this.state;
		return this.state.responsesToDisplay.map(row => {
			let identifier = row.id;
			return (
				<RowElement
					adminPg={false}
					key={identifier}
					infoVisible={selectedRowId === identifier}
					resp={row}
					infoButtonOnClick={this.infoButtonOnClick}
					cancelButtonOnClick={this.cancelButtonOnClick}
					restartButtonOnClick={this.restartButtonOnClick}
					deleteButtonOnClick={this.deleteButtonOnClick}
				/>
			);
		});
	}
	render() {
		const rowsPerPageOptions = [10, 20, 50, 100];
		const sortableColumns = []
		console.log(this.state.responsesToDisplay)
		if (this.state.responsesToDisplay[0]) {
			for (const key of Object.keys(this.state.responsesToDisplay[0])) {
				sortableColumns.push(key)
			}
		}
		return(
			<div className='historyPage'>
				<div className='QueueTable'>
					<button className="pollingButton" onMouseEnter={() => {console.log("Enter")}}>{this.state.pollCountdown}</button>
					<QueueView
						adminPg={false}
						loading={this.state.loading}
						orderBy={this.state.orderBy}
						order={this.state.order}
						page={this.state.page}
						responsesToDisplay={this.state.responsesToDisplay}
						rowsPerPage={this.state.rowsPerPage}
						rowsPerPageOptions={rowsPerPageOptions}
						sortableColumns={sortableColumns}
						totalCount={this.state.totalCount}
						classes={this.props}
						handleChangePage={this.handleChangePage}
						handleChangeRowsPerPage={this.handleChangeRowsPerPage}
						handleRequestSort={this.handleRequestSort}
						populateRows={this.populateRows}
					/>
				</div>
			</div>
		);
	}
}

export default QueueComponent




