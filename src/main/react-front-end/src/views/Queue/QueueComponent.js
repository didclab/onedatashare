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
			pollCountdown: 30,
			pollButtonHover: false,
			pollRunning: true
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
				this.setState({pollCountdown: 30})
			}
		}, 1000);
	}

	handlePollButton() {
		if (this.state.pollRunning) {
			clearInterval(this.interval)
			this.setState({pollRunning: !this.state.pollRunning})
		}
		else {
			this.startInterval()
			this.setState({pollRunning: !this.state.pollRunning})
		}
	}

	componentWillUnmount() {
		clearInterval(this.interval)
	}

	update() {
		const statusSet = new Set(["STARTED", "STARTING", "EXECUTING", "STOPPING"])
		let newData = [...this.state.responsesToDisplay]

		for (const job of newData) {
			if (statusSet.has(job.status)) {
				const resp = new Promise((resolve, reject) => {
					getJobUpdatesForUser(job.id, resolve, reject)
				}).then((resp) => {
					const data = resp[0].data
					const index = newData.findIndex(obj => obj.id === data.id)
					if (index != -1) {
						newData[index] = data
						this.setState({responsesToDisplay: newData})
					}
					else {
						newData.push(data)
						this.setState({responsesToDisplay: newData})
					}
				}).catch(error => {
					console.error(error)
				})
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
			if (filterSet.has(job.status)) {
				filteredResponse.push(job)
			}
		}

		
		this.setState({
			response: resp,
			responsesToDisplay: filteredResponse,
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
		}, () => {
			this.queueFunc();
		});
	}

	handleChangeRowsPerPage = (event) => {
		this.setState({page: 0, rowsPerPage: parseInt(event.target.value), loading: true})
	}

	handleRequestSort = (property) => {
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
		if (this.state.responsesToDisplay[0]) {
			for (const key of Object.keys(this.state.responsesToDisplay[0])) {
				sortableColumns.push(key)
			}
		}
		return(
			<div className='historyPage'>
				<div className='QueueTable'>
					{this.state.pollRunning ? (	
						<button className="pollingButton" 
							onMouseLeave={() => {this.setState({pollButtonHover: !this.state.pollButtonHover})}} 
							onMouseEnter={() => {this.setState({pollButtonHover: !this.state.pollButtonHover})}}
							onClick={() => {clearInterval(this.handlePollButton())}}
						>
							{this.state.pollButtonHover ? "Stop" : this.state.pollCountdown}
						</button>
					) : (
					<button className="pollingButton stopped" 
						onMouseLeave={() => {this.setState({pollButtonHover: !this.state.pollButtonHover})}} 
						onMouseEnter={() => {this.setState({pollButtonHover: !this.state.pollButtonHover})}}
						onClick={() => {clearInterval(this.handlePollButton())}}
					>
						{this.state.pollButtonHover ? "Continue" : this.state.pollCountdown}
					</button>
				)
					}
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




