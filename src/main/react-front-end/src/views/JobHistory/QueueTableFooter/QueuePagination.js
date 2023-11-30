import TableRow from "@material-ui/core/TableRow";
import TablePagination from "@material-ui/core/TablePagination";
import React from "react";

const QueuePagination = ({
                             colSpan,
                             classes,
                             handleChangePage,
                             handleChangeRowsPerPage,
                             page,
                             rowsPerPage,
                             rowsPerPageOptions,
                             TablePaginationActions,
                             totalCount
                         }) => {
    return(
        <TableRow>
            <TablePagination
                rowsPerPageOptions={rowsPerPageOptions}
                colSpan={colSpan}
                count={totalCount}
                rowsPerPage={rowsPerPage}
                page={page}
                SelectProps={{
                    native: true,
                }}
                onPageChange={handleChangePage}
                onRowsPerPageChange={handleChangeRowsPerPage}
                ActionsComponent={TablePaginationActions}
                classes={{
                    caption: classes.tablePaginationCaption,
                    select: classes.tablePaginationSelect,
                    toolbar: classes.toolbar
                }}
            />
        </TableRow>
    );
};

export default QueuePagination;