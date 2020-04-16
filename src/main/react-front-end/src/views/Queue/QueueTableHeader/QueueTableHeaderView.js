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


function makeHeaderCells(order, orderBy, handleRequestSort, sortableColumns) {
    let labels = [];
    let headers = [];
    let menuOpts = [];
    let titles = ["Job ID", "Progress", "Average Speed", "Source", "Destination"];
    let classes = ["idCell", "progressCell", "speedCell", "sourceCell", "destinationCell"]
    let keys = [sortableColumns.jobId, sortableColumns.status, sortableColumns.avgSpeed, sortableColumns.source, sortableColumns.destination];
/*    let keys = ['job_id','status',"bytes.avg","src.uri"];*/
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
                                  handleRequestSort,
                                  order,
                                  orderBy,
                                  sortableColumns,
                              }) => {
    let [headerCells, menuOpts] = makeHeaderCells(order, orderBy, handleRequestSort, sortableColumns);
    return (
        <TableHead>
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