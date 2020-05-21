import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import React from 'react';
import QueueTableSortLabel from "./QueueTableSortLabel";
import {Hidden} from "@material-ui/core";
import MenuItem from "@material-ui/core/MenuItem";
import Tooltip from "@material-ui/core/Tooltip";
import QueueMobileHeader from "./QueueMobileHeader";
import AdminHistoryTools from "./AdminHistoryTools";


function makeHeaderCells(adminPg, order, orderBy, handleRequestSort, sortableColumns) {
    let labels = [];
    let headers = [];
    let menuOpts = [];
    let titles = ["Job ID", "Progress", "Speed", "Source", "Destination"];
    let classes = ["idCell", "progressCell", "speedCell", "sourceCell", "destinationCell"];
    let keys = [sortableColumns.jobId, sortableColumns.status, sortableColumns.avgSpeed, sortableColumns.source, sortableColumns.destination];
    if (adminPg) {
        for (let i = 0; i < classes.length; i += 1) {
            classes[i] = classes[i] + "-admin";
        }
        titles.splice(0, 0, "User");
        classes.splice(0, 0, "userCell-admin");
        keys.splice(0,0, sortableColumns.userName);
        titles.push("Start Time");
        classes.push("timeCell-admin");
        keys.push(sortableColumns.startTime);
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
                                  page,
                                  queueFunc,
                                  refreshSuccess,
                                  refreshFailure,
                                  rowsPerPage,
                                  sortableColumns,
                              }) => {
    let [headerCells, menuOpts] = makeHeaderCells(adminPg, order, orderBy, handleRequestSort, sortableColumns);
    return (
        <TableHead>
            { adminPg && <AdminHistoryTools
                customToolbar={customToolbar}
                order={order}
                orderBy={orderBy}
                page={page}
                refreshFailure={refreshFailure}
                refreshSuccess={refreshSuccess}
                rowsPerPage={rowsPerPage}
                queueFunc={queueFunc}
            />
            }
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