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


function makeHeaderCells(order, orderBy, handleRequestSort, sortableColumns) {
    let retVal = [];
    let titles = ["Job ID", "Progress", "Average Speed", "Source & Destination"];
    let classes = ["idCell", "progressCell", "speedCell", "sourceCell"]
    let keys = [sortableColumns.jobId, sortableColumns.status, sortableColumns.avgSpeed, sortableColumns.source];
    for (let i = 0; i < titles.length; i += 1) {
        retVal.push(
            <TableCell className={classes[i] + " queueHeaderCell"}>
                <TableSortLabel
                    active={orderBy === keys[i]}
                    direction={order}
                    onClick={() => {
                        handleRequestSort(keys[i])
                    }}>
                    <p>{titles[i]}</p>
                </TableSortLabel>
            </TableCell>
        );
    }
    return retVal;
};

function makeSortOptions(order, orderBy, handleRequestSort, sortableColumns) {
    let retVal = [];
    let titles = ["Job ID", "Progress", "Average Speed", "Source & Destination"];
    let keys = [sortableColumns.jobId, sortableColumns.status, sortableColumns.avgSpeed, sortableColumns.source];
    let classes = ["idCell", "progressCell", "speedCell", "sourceCell"]
    for (let i = 0; i < titles.length; i += 1) {
        retVal.push(
            <MenuItem className={classes[i]}>
                <QueueTableSortLabel
                    handleRequestSort={handleRequestSort}
                    order={order}
                    orderBy={orderBy}
                    sortKey={keys[i]}
                    title={titles[i]}
                />
            </MenuItem>
        );
    }
    return retVal;
};

const QueueTableHeaderView = ({
                                         handleRequestSort,
                                         order,
                                         orderBy,
                                         sortableColumns,
                                     }) => {
        let headerCells = makeHeaderCells(order, orderBy, handleRequestSort, sortableColumns);
        let opts = makeSortOptions(order, orderBy, handleRequestSort, sortableColumns)
        return (
            <OverrideMaterialUICss>
                <TableHead>
                    <TableRow>
                        <Hidden mdDown>
                            {headerCells}
                            <TableCell className="actionCell queueHeaderCell"><p>Actions</p></TableCell>
                        </Hidden>
                        <Hidden lgUp>
                            <TableCell className="queueHeaderCell mobileCell">
                                <p>Transfer History</p>
                                <div className="queueDropDown">
                                    <FormControl variant="outlined">
                                        <InputLabel> Sort by </InputLabel>
                                        <Select
                                            onChange={handleRequestSort}
                                            label="Sort Category"
                                        >
                                            {opts}
                                        </Select>
                                    </FormControl>
                                </div>
                            </TableCell>
                        </Hidden>
                    </TableRow>
                </TableHead>
            </OverrideMaterialUICss>
        );
    };

export default QueueTableHeaderView;