import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import React from 'react';
import QueueHeaderCell from "./QueueHeaderCell";

export default class QueueTableHeaderView extends React.Component {

    handleRequestSort = this.props.handleRequestSort;
    order = this.props.order;
    orderBy = this.props.orderBy;
    sortableColumns = this.props.sortableColumns;

    makeHeaderCells() {
        let retVal = [];
        let titles = ["Job ID", "Progress", "Average Speed", "Source & Destination"];
        let keys = [this.sortableColumns.jobId, this.sortableColumns.status, this.sortableColumns.avgSpeed, this.sortableColumns.source];
        for (let i=0; i<titles.length; i+=1) {
            retVal.push(
                <QueueHeaderCell
                    handleRequestSort={this.handleRequestSort}
                    order={this.order}
                    orderBy={this.orderBy}
                    sortKey={keys[i]}
                    title = {titles[i]}
                />
            );
        }
        return retVal;
    }

    render() {
        let headerCells = this.makeHeaderCells();
        return (
            <TableHead className="QueueTableHeaderView">
                <TableRow>
                    {headerCells}
                    <TableCell className="QueueHeaderCell"> Actions </TableCell>
                </TableRow>
            </TableHead>
        );
    }

}