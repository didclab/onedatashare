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
import { getJobsForAdmin } from '../../APICalls/APICalls';
import { updateGAPageView } from "../../analytics/ga";
import QueueView from "../Queue/QueueView";
import RowElement from "../Queue/QueueTableRow/RowElement/RowElement";

const rowsPerPageOptions = [10, 20, 50, 100]

class HistoryComponent extends Component {
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
			orderBy : 'jobId',
			selectedRowId: null,
			totalCount: 0,
			loading: true,
		}

		this.queueFunc = this.queueFunc.bind(this)
		this.toggleTabs = this.toggleTabs.bind(this);
		this.refreshSuccess = this.refreshSuccess.bind(this)
		this.refreshFailure = this.refreshFailure.bind(this)
		this.toggleTabs = this.toggleTabs.bind(this)
		this.handleSearchChange = this.handleSearchChange.bind(this)
		this.handleSearch = this.handleSearch.bind(this)
		this.infoButtonOnClick = this.infoButtonOnClick.bind(this)
		this.handleChangeRowsPerPage = this.handleChangeRowsPerPage.bind(this)
		this.handleChangePage	= this.handleChangePage.bind(this)
		this.interval = setInterval(this.queueFunc, 2000);    //making a queue request every 2 seconds
		updateGAPageView()
	}

	componentDidMount() {
		document.title = "OneDataShare - History";
		this.queueFunc()
	}

	componentWillUnmount() {
		clearInterval(this.interval);
	}

	componentDidUpdate(prevProps, prevState) {
		const {
			page: prevPage,
			rowsPerPage: prevRowsPerPage,
			orderBy: prevOrderBy,
			order: prevOrder,
			response: prevResponse,
			loading: prevLoading
		} = prevState;
		const { loading, response, page, rowsPerPage, orderBy, order } = this.state
		if ((!prevLoading && loading !== prevLoading) || response.length !== prevResponse.length ||
			page !== prevPage || rowsPerPage !== prevRowsPerPage || orderBy !== prevOrderBy ||
			order !== prevOrder) {
			this.queueFunc();
		}
	}

	queueFunc() {
		this.refreshTransfers()
	}

	paginateResults(results, page, limit) {
		let offset = page * limit
		return results.slice(offset, offset + limit)
	}

	refreshSuccess = (resp) => {
		// const { page, rowsPerPage } = this.state
		//let responsesToDisplay = this.paginateResults(resp.jobs, page, rowsPerPage)
		//commented to fix second page render issue as it slices all jobs and returns null object

		this.setState({
			response: resp.jobs,
			responsesToDisplay: resp.jobs,
			totalCount: resp.totalCount,
			loading: false
		})
	}

	refreshFailure = () => {
	}

	refreshTransfers() {
		const { searchValue, page, rowsPerPage, orderBy, order } = this.state
		getJobsForAdmin(
			searchValue,
			page,
			rowsPerPage,
			orderBy,
			order,
			this.refreshSuccess,
			this.refreshFailure
		)
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

	handleChangePage = (event, page) => {
		const { response } = this.state
		// const { response, rowsPerPage } = this.state
		//let nextRecords = this.paginateResults(response, page, rowsPerPage)
		this.setState({
			page: page,
			responsesToDisplay: response,
			selectedRowId: null,
			loading: true
		});
	}

	handleChangeRowsPerPage = (event) => {
		this.setState({ page: 0, rowsPerPage: parseInt(event.target.value), loading: true })
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

	handleSearchChange = (event) => {
		this.setState({searchValue: event.target.value})
	}

	handleSearch = (event) => {
		event.preventDefault()
		this.setState({loading: true})
	}

	customToolbar = () => {
		const { searchValue } = this.state
		return (
			<form onSubmit={this.handleSearch}>
				<input
					name="searchByOwner"
					label="Search By Owner"
					value={searchValue}
					onChange={this.handleSearchChange}
					placeholder='Search By Owner'
				/>
			</form>
		);
	}

	populateRows = () => {
		const {selectedRowId} = this.state;
		return this.state.responsesToDisplay.map((row, index) => {
			let identifier = `${index}-${row.jobId}`
			return (
				<RowElement
					adminPg={true}
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
		const sortableColumns = {
			userName: "owner",
			jobId: 'jobId',
			status: 'status',
			avgSpeed : "bytes.avg",
			source : "src.uri",
			destination: "dest.uri",
			startTime: 'times.started'
		};
		return(
			<QueueView
				adminPg={true}
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
				queueFunc={this.queueFunc}
				customToolbar={this.customToolbar()}
			/>
		);
	}
}

export default HistoryComponent;