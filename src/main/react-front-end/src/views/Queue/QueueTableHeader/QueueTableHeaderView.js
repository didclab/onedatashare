import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import React from 'react';
import QueueHeaderCell from "./QueueHeaderCell";
import {Hidden} from "@material-ui/core";
import MenuItem from "@material-ui/core/MenuItem";
import InputLabel from "@material-ui/core/InputLabel";
import FormControl from "@material-ui/core/FormControl";
import Select from "@material-ui/core/Select";
import Tooltip from "@material-ui/core/Tooltip";

export default class QueueTableHeaderView extends React.Component {

    handleRequestSort = this.props.handleRequestSort;
    order = this.props.order;
    orderBy = this.props.orderBy;
    sortableColumns = this.props.sortableColumns;

    makeHeaderCells() {
        let retVal = [];
        let titles = ["Job ID", "Progress", "Average Speed", "Source & Destination"];
        let classes = ["idCell", "progressCell", "speedCell", "sourceCell"]
        let keys = [this.sortableColumns.jobId, this.sortableColumns.status, this.sortableColumns.avgSpeed, this.sortableColumns.source];
        for (let i=0; i<titles.length; i+=1) {
            retVal.push(
                <Tooltip title={"Sort by" + titles[i]} placement='bottom-end'>
                    <TableCell className={classes[i]}>
                        <QueueHeaderCell
                            handleRequestSort={this.handleRequestSort}
                            order={this.order}
                            orderBy={this.orderBy}
                            sortKey={keys[i]}
                            title = {titles[i]}
                        />
                    </TableCell>
                </Tooltip>
            );
        }
        return retVal;
    }

    makeSortOptions() {
        let retVal = [];
        let titles = ["Job ID", "Progress", "Average Speed", "Source & Destination"];
        let keys = [this.sortableColumns.jobId, this.sortableColumns.status, this.sortableColumns.avgSpeed, this.sortableColumns.source];
        let classes = ["idCell", "progressCell", "speedCell", "sourceCell"]
        for (let i=0; i<titles.length; i+=1) {
            retVal.push(
                <MenuItem className={classes[i] + "queueHeaderCell"}>
                    <QueueHeaderCell
                        handleRequestSort={this.handleRequestSort}
                        order={this.order}
                        orderBy={this.orderBy}
                        sortKey={keys[i]}
                        title = {titles[i]}
                    />
                </MenuItem>
            );
        }
        return retVal;
    }

    render() {
        let headerCells = this.makeHeaderCells();
        let opts = this.makeSortOptions()
        return (
            <TableHead>
                <TableRow>
                    <Hidden mdDown>
                        {headerCells}
                        <TableCell className="actionCell"> <p>Actions</p> </TableCell>
                    </Hidden>
                    <Hidden lgUp>
                        <TableCell className="QueueHeaderCell mobileCell">
                            <p>Transfer History</p>
                            <div className="queueDropDown">
                                <FormControl variant="outlined">
                                    <InputLabel> Sort by </InputLabel>
                                    <Select
                                        onChange={this.handleRequestSort}
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
        );
    }

}