import TableRow from "@mui/material/TableRow";
import TablePagination from "@mui/material/TablePagination";
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
  totalCount,
}) => {
  return (
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
          toolbar: classes.toolbar,
        }}
      />
    </TableRow>
  );
};

export default QueuePagination;
