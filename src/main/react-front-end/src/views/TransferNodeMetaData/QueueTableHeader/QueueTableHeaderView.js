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

    const headerCells = {
        'odsOwner': { title: 'ODS Owner', class: 'odsOwnerCell' },
        'nodeName': { title: 'Node Name', class: 'nodeNameCell' },
        'nodeUuid': { title: 'Node UUID', class: 'nodeUuidCell' },
        'runningJob': { title: 'Running Job', class: 'runningJobCell' },
        'online': { title: 'Online', class: 'onlineCell' },
        'jobId': { title: 'Job ID', class: 'jobIdCell' },
        'jobUuid': { title: 'Job UUID', class: 'jobUuidCell' },
    };
    

    const enabledHeaders = [
        'odsOwner', 
        'nodeName', 
        'nodeUuid', 
        'runningJob', 
        'online', 
        'jobId', 
        'jobUuid', 
    ];
    

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