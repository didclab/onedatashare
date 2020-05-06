import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import Button from "@material-ui/core/Button";
import RefreshIcon from "@material-ui/icons/Refresh";
import React from "react";
import SearchComponent from "../SearchComponent";
import {Hidden} from "@material-ui/core";

const AdminHistoryTools = ({
                               customToolbar,
                               order,
                               orderBy,
                               page,
                               refreshFailure,
                               refreshSuccess,
                               rowsPerPage,
                               queueFunc
                           }) => {
    return(
        <React.Fragment>
            {/*            <TableRow>
                <TableCell className={"queueHeaderCell"} colSpan='7'>
                    <h1> Transfer History </h1>
                </TableCell>
                <TableCell className={"queueHeaderCell"} colSpan='1'>
                    <Button variant="outlined" startIcon={<RefreshIcon />} color="primary" disableElevation
                            onClick={queueFunc} size="small">
                        Refresh
                    </Button>
                </TableCell>
                <TableCell style={{width: '30%', fontSize: '2rem', color: '#31708f'}} colSpan='2'>
                    { customToolbar }
                </TableCell>
            </TableRow>*/}
            <Hidden mdDown>
                <TableRow>
                    <TableCell className={"queueHeaderCell"} colSpan='7'>
                        <SearchComponent
                            refreshSuccess = {refreshSuccess}
                            refreshFailure = {refreshFailure}
                            rowsPerPage = {rowsPerPage}
                            page = {page}
                            order = {order}
                            orderBy = {orderBy}
                        />
                    </TableCell>
                    <TableCell className={"queueHeaderCell"} colSpan='1'>
                        <Button variant="outlined" startIcon={<RefreshIcon />} color="primary" disableElevation
                                onClick={queueFunc} size="small">
                            Refresh
                        </Button>
                    </TableCell>
                </TableRow>
            </Hidden>
            <Hidden lgUp>
                <SearchComponent
                    refreshSuccess = {refreshSuccess}
                    refreshFailure = {refreshFailure}
                    rowsPerPage = {rowsPerPage}
                    page = {page}
                    order = {order}
                    orderBy = {orderBy}
                />
            </Hidden>
        </React.Fragment>
    );
};

export default AdminHistoryTools;