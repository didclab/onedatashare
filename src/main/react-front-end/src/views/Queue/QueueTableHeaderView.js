import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import Tooltip from "@material-ui/core/Tooltip";
import TableSortLabel from "@material-ui/core/TableSortLabel";
import React from "react";

const tbcellStyle = {textAlign: 'center'};

const QueueTableHeaderView = ({
                                  // Values and variables
                                  classes,
                                  loading,
                                  order,
                                  orderBy,
                                  page,
                                  rowsPerPage,
                                  rowsPerPageOptions,
                                  sortableColumns,
                                  totalCount,
                                  // Functions
                                  handleRequestSort,
                              }) => {

    return (
        <TableHead style={{backgroundColor: '#d9edf7'}}>
            <TableRow>
                <TableCell style={{...tbcellStyle, width: '5%',  fontSize: '2rem', color: '#31708f'}}>
                    <Tooltip title="Sort on Job ID" placement='bottom-end' enterDelay={300}>
                        <TableSortLabel
                            active={orderBy === sortableColumns.jobId}
                            direction={order}
                            onClick={() => {handleRequestSort(sortableColumns.jobId)}}>
                            Job ID
                        </TableSortLabel>
                    </Tooltip>
                </TableCell>
                <TableCell style={{...tbcellStyle, width: '30%',  fontSize: '2rem', color: '#31708f'}}>
                    <Tooltip title="Sort on Progress" placement='bottom-end' enterDelay={300}>
                        <TableSortLabel
                            active={orderBy === sortableColumns.status}
                            direction={order}
                            onClick={() => {handleRequestSort(sortableColumns.status)}}>
                            Progress
                        </TableSortLabel>
                    </Tooltip>
                </TableCell>
                <TableCell style={{...tbcellStyle, width: '5%',  fontSize: '2rem', color: '#31708f'}}>
                    <Tooltip title="Sort on Average Speed" placement='bottom-end' enterDelay={300}>
                        <TableSortLabel
                            active={orderBy === sortableColumns.avgSpeed}
                            direction={order}
                            onClick={() => {handleRequestSort(sortableColumns.avgSpeed)}}>
                            Average Speed
                        </TableSortLabel>
                    </Tooltip>
                </TableCell>
                <TableCell style={{...tbcellStyle, width: '25%',  fontSize: '2rem', color: '#31708f'}}>
                    <Tooltip title="Sort on Source/Destination" placement='bottom-end' enterDelay={300}>
                        <TableSortLabel
                            active={orderBy === sortableColumns.source}
                            direction={order}
                            onClick={() => {handleRequestSort(sortableColumns.source)}}>
                            Source/Destination
                        </TableSortLabel>
                    </Tooltip>
                </TableCell>
                <TableCell style={{...tbcellStyle, width: '15%',  fontSize: '2rem', color: '#31708f'}}>Actions</TableCell>
            </TableRow>
        </TableHead>
    );
};

export default QueueTableHeaderView;