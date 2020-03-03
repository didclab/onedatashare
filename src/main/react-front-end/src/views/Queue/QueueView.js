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
import QueueTableHeaderView from "./QueueTableHeaderView";
import QueueTableBodyView from "./QueueTableBodyView";

const tbcellStyle = {textAlign: 'center'};


const QueueView = ({
                       // Values and variables
                       classes,
                       loading,
                       order,
                       orderBy,
                       page,
                       responsesToDisplay,
                       rowsPerPage,
                       rowsPerPageOptions,
                       sortableColumns,
                       totalCount,
                       // Functions
                       handleChangePage,
                       handleChangeRowsPerPage,
                       handleRequestSort,
                       populateRows,
                   }) => {
    return (
        <Paper style={{marginLeft: '10%', marginRight: '10%', border: 'solid 2px #d9edf7'}}>
            <Table style={{display: "block"}}>
                <QueueTableHeaderView
                    loading={loading}
                    orderBy={orderBy}
                    order={order}
                    page={page}
                    responsesToDisplay={responsesToDisplay}
                    rowsPerPage={rowsPerPage}
                    rowsPerPageOptions={rowsPerPageOptions}
                    sortableColumns={sortableColumns}
                    totalCount={totalCount}
                    classes={classes}
                    handleRequestSort={handleRequestSort}
                />
                <QueueTableBodyView
                    loading={loading}
                    populateRows={populateRows}
                />
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
                            onChangePage={handleChangePage}
                            onChangeRowsPerPage={handleChangeRowsPerPage}
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