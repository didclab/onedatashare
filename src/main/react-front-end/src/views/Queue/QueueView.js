import React from 'react';
import Table from "@material-ui/core/Table";
import TableRow from "@material-ui/core/TableRow";
import TableFooter from "@material-ui/core/TableFooter";
import TablePagination from "@material-ui/core/TablePagination";
import TablePaginationActions from "../TablePaginationActions";
import QueueTableHeaderView from "./QueueTableHeader/QueueTableHeaderView";
import QueueTableBodyView from "./QueueTableBodyView";

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
        <div className="QueueView">
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
                <TableFooter>
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
        </div>
    );
};

export default QueueView;