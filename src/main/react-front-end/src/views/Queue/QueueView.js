import React from 'react';
import Table from "@material-ui/core/Table";
import TableFooter from "@material-ui/core/TableFooter";
import TablePaginationActions from "../TablePaginationActions";
import QueueTableHeaderView from "./QueueTableHeader/QueueTableHeaderView";
import QueueTableBodyView from "./QueueTableRow/QueueTableBodyView";
import {TableContainer} from "@material-ui/core";
import Paper from "@material-ui/core/Paper";
import QueuePagination from "./QueuePagination";

const QueueView = ({
                       // A boolean to determine if this is for the admin page
                       adminPg,
                       // Values and variables
                       classes,
                       customToolbar,
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
                       // For admin history
                       queueFunc,
                       refreshSuccess,
                       refreshFailure,
                   }) => {
    return (
        <div className="QueueView">
            <TableContainer component={Paper}>
                <Table style={{display: "block"}}>
                    <QueueTableHeaderView
                        adminPg={adminPg}
                        customToolbar={customToolbar}
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
                        queueFunc={queueFunc}
                        refreshFailure={refreshFailure}
                        refreshSuccess={refreshSuccess}
                    />
                    <QueueTableBodyView
                        loading={loading}
                        populateRows={populateRows}
                    />
                    <TableFooter>
                        <QueuePagination
                            colSpan={7}
                            classes={classes}
                            handleChangePage={handleChangePage}
                            handleChangeRowsPerPage={handleChangeRowsPerPage}
                            page={page}
                            rowsPerPage={rowsPerPage}
                            rowsPerPageOptions={rowsPerPageOptions}
                            TablePaginationActions={TablePaginationActions}
                            totalCount={totalCount} />
                    </TableFooter>
                </Table>
            </TableContainer>
        </div>
    );
};

export default QueueView;