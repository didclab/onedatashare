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
		}

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
					<QueueView
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




