import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import React from 'react';
import QueueTableSortLabel from "./QueueTableSortLabel";
import {Hidden} from "@material-ui/core";
import MenuItem from "@material-ui/core/MenuItem";
import InputLabel from "@material-ui/core/InputLabel";
import FormControl from "@material-ui/core/FormControl";
import Select from "@material-ui/core/Select";
import Tooltip from "@material-ui/core/Tooltip";
import TableSortLabel from "@material-ui/core/TableSortLabel";
import Table from "@material-ui/core/Table";
import Paper from "@material-ui/core/Paper";
import {OverrideMaterialUICss} from "override-material-ui-css";
import QueueMobileHeader from "./QueueMobileHeader";
import Button from "@material-ui/core/Button";
import RefreshIcon from "@material-ui/icons/Refresh";
import AdminHistoryTools from "../AdminHistoryTools";


function makeHeaderCells(adminPg, order, orderBy, handleRequestSort, sortableColumns) {
    let labels = [];
    let headers = [];
    let menuOpts = [];
    let titles;
    let classes;
    let keys;
    if (adminPg) {
        titles = ["User", "Job ID", "Progress", "Average Speed", "Source", "Destination", "Start Time"];
        classes = ["userCell","idCell", "progressCell", "speedCell", "sourceCell", "destinationCell", "timeCell"]
        keys = [sortableColumns.userName, sortableColumns.jobId, sortableColumns.status, sortableColumns.avgSpeed, sortableColumns.source, sortableColumns.destination, sortableColumns.startTime];
    } else {
        titles = ["Job ID", "Progress", "Average Speed", "Source", "Destination"];
        classes = ["idCell", "progressCell", "speedCell", "sourceCell", "destinationCell"]
        keys = [sortableColumns.jobId, sortableColumns.status, sortableColumns.avgSpeed, sortableColumns.source, sortableColumns.destination];
    }
    for (let i = 0; i < titles.length; i += 1) {
        labels.push(
            <QueueTableSortLabel
                handleRequestSort={handleRequestSort}
                order={order}
                orderBy={orderBy}
                sortKey={keys[i]}
                title={titles[i]}
            />
        );
    }
    for (let i = 0; i < titles.length; i += 1) {
        headers.push(
            <Tooltip title={"Sort by" + titles[i]} placement='bottom-end'>
                <TableCell className={classes[i] + " queueHeaderCell"}>
                    {labels[i]}
                </TableCell>
            </Tooltip>
        );
        menuOpts.push(
            <MenuItem value={keys[i]}>
                {titles[i]}
            </MenuItem>
        );
    }
    return [headers, menuOpts];
};

const QueueTableHeaderView = ({
                                  adminPg,
                                  customToolbar,
                                  handleRequestSort,
                                  order,
                                  orderBy,
                                  queueFunc,
                                  sortableColumns,
                              }) => {
    let [headerCells, menuOpts] = makeHeaderCells(adminPg, order, orderBy, handleRequestSort, sortableColumns);
    return (
        <TableHead>
            { adminPg &&
            <AdminHistoryTools
                customToolbar={customToolbar}
                queueFunc={queueFunc} /> }
            <TableRow>
                <Hidden mdDown>
                    {headerCells}
                    <TableCell className="actionCell queueHeaderCell"><p>Actions</p></TableCell>
                </Hidden>
                <Hidden lgUp>
                    <QueueMobileHeader
                        handleRequestSort={handleRequestSort}
                        menuOpts={menuOpts}
                        orderBy={orderBy}/>
                </Hidden>
            </TableRow>
        </TableHead>
    );
};

export default QueueTableHeaderView;