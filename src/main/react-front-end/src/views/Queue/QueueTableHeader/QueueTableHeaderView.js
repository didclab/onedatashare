import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import React from 'react';
import {Hidden} from "@material-ui/core";
import MenuItem from "@material-ui/core/MenuItem";
import Tooltip from "@material-ui/core/Tooltip";
import QueueMobileHeader from "./QueueMobileHeader";


function makeHeaderCells(order, orderBy, handleRequestSort, sortableColumns) {

    let headers = [];
    let menuOpts = [];
    // jobId: 'job_id',
	// status: 'status',
	// avgSpeed : "bytes.avg",
	// source : "src.uri",
	// destination: "dest.uri"

    const headerCells = {
        'id': { title: 'Job ID', class: 'idCell' },
        'version': { title: 'Version', class: 'versionCell' },
        'jobInstanceId': { title: 'Job Instance ID', class: 'jobInstanceIdCell' },
        'createTime': { title: 'Create Time', class: 'createTimeCell' },
        'startTime': { title: 'Start Time', class: 'startTimeCell' },
        'status': { title: 'Status', class: 'statusCell' },
        'exitCode': { title: 'Exit Code', class: 'exitCodeCell' },
        'exitMessage': { title: 'Exit Message', class: 'exitMessageCell' },
        'lastUpdated': { title: 'Last Updated', class: 'lastUpdatedCell' },
        'batchSteps': { title: 'Batch Steps', class: 'batchStepsCell' },
        'jobParameters': { title: 'Job Parameters', class: 'jobParametersCell' },
        'speed': { title: 'Speed', class: 'speedCell' },
        'source': { title: 'Source', class: 'sourceCell' },
        'destination': { title: 'Destination', class: 'destinationCell' }
    };
    

    const enabledHeaders = ['id', 'status', 'speed', 'source', 'destination']


    for (const key of enabledHeaders) {
        const item = headerCells[key]
        if (item) {
            headers.push(
                <TableCell className={item.class + " queueHeaderCell"} key={key}>
                    <p>{item.title}</p>
                </TableCell>
            );
            menuOpts.push(
                <MenuItem>
                    {item.title}
                </MenuItem>
            );
        }
    }
    for (const item in headerCells) {
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
        <TableHead >
            <TableRow >
                <Hidden mdDown>
                    {headerCells}
                    <TableCell className="actionCell queueHeaderCell" key="action"><p>Actions</p></TableCell>
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