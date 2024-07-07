import TableRow from "@mui/material/TableRow";
import TableCell from "@mui/material/TableCell";
import Button from "@mui/material/Button";
import RefreshIcon from "@mui/icons-material/Refresh";
import React from "react";
import SearchComponent from "../../SearchComponent";
import { Hidden } from "@mui/material";

const AdminHistoryTools = ({
  customToolbar,
  order,
  orderBy,
  page,
  refreshFailure,
  refreshSuccess,
  rowsPerPage,
  queueFunc,
}) => {
  return (
    <React.Fragment>
      <Hidden mdDown>
        <TableRow>
          <TableCell className={"queueHeaderCell"} colSpan="7">
            <SearchComponent
              refreshSuccess={refreshSuccess}
              refreshFailure={refreshFailure}
              rowsPerPage={rowsPerPage}
              page={page}
              order={order}
              orderBy={orderBy}
            />
          </TableCell>
          <TableCell className={"queueHeaderCell"} colSpan="1">
            <Button
              variant="outlined"
              startIcon={<RefreshIcon />}
              color="primary"
              disableElevation
              onClick={queueFunc}
              size="small"
            >
              Refresh
            </Button>
          </TableCell>
        </TableRow>
      </Hidden>
      <Hidden lgUp>
        <SearchComponent
          refreshSuccess={refreshSuccess}
          refreshFailure={refreshFailure}
          rowsPerPage={rowsPerPage}
          page={page}
          order={order}
          orderBy={orderBy}
        />
      </Hidden>
    </React.Fragment>
  );
};

export default AdminHistoryTools;
