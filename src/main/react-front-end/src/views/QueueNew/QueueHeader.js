import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import TableHead from "@material-ui/core/TableHead";
import React from "react";

function makeHeaderCells() {
    let headers = [];
    let titles = ["Job ID", "Start Time", "End Time", "Status", "Exit Code", "Exit Message", "Last Updated", "App Name","Source Credential", "Show Steps"];
    let classes = ["idCell", "columnDataCell", "columnDataCell", "columnDataCell", "columnDataCell","columnDataCell","columnDataCell","columnDataCell","columnDataCell","columnDataCell"];

    for (let i = 0; i < titles.length; i += 1) {
        headers.push(
                <TableCell className={"columnDataCell"}>
                    {titles[i]}
                </TableCell>
        );
    }
    return [headers];
};

const QueueHeader = () => {
    let [headerCells] = makeHeaderCells();
    return (
        <TableHead>
            <TableRow>
                {headerCells}
            </TableRow>
        </TableHead>
    );
};

export default QueueHeader;