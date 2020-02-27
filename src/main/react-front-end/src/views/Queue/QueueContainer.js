import React, { Component } from 'react';
import { cancelJob, restartJob, deleteJob, getJobUpdatesForUser, getJobsForUser } from '../../APICalls/APICalls';
import { eventEmitter } from '../../App'
import { ProgressBar, Grid, Row, Col } from 'react-bootstrap';

import { updateGAPageView } from '../../analytics/ga';
import { jobStatus } from '../../constants';
import Paper from "@material-ui/core/Paper";
import Table from "@material-ui/core/Table";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import Tooltip from "@material-ui/core/Tooltip";
import TableSortLabel from "@material-ui/core/TableSortLabel";
import TableBody from "@material-ui/core/TableBody";
import CircularProgress from "@material-ui/core/CircularProgress";
import TableFooter from "@material-ui/core/TableFooter";
import TablePagination from "@material-ui/core/TablePagination";
import TablePaginationActions from "../TablePaginationActions";

const QueueView = ({

                   }) => {
    return (
        <Paper className={classes.root} style={{marginLeft: '10%', marginRight: '10%', border: 'solid 2px #d9edf7'}}>
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
    );
};
export default QueueView;