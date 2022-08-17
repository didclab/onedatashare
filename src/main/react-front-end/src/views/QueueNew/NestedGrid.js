import TableCell from "@material-ui/core/TableCell";
import Table from "@material-ui/core/Table";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableBody from "@material-ui/core/TableBody";
import React from "react";
import {Component} from "react";

class NestedGrid extends Component {

    constructor(props) {
        super(props);
    }

    render() {
        return (
            this.props.stepData.map((row) => (
                    <TableRow key={row.id}>
                        <TableCell colSpan="7">
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>Step Name</TableCell>
                                        <TableCell>Start Time</TableCell>
                                        <TableCell>End Time</TableCell>
                                        <TableCell>Status</TableCell>
                                        <TableCell>Read Count</TableCell>
                                        <TableCell>Write Count</TableCell>
                                        <TableCell>Exit Code</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    <TableRow>
                                        <TableCell>{row.step_name}</TableCell>
                                        <TableCell>{row.startTime}</TableCell>
                                        <TableCell>{row.endTime}</TableCell>
                                        <TableCell>{row.status}</TableCell>
                                        <TableCell>{row.readCount}</TableCell>
                                        <TableCell>{row.writeCount}</TableCell>
                                        <TableCell>{row.exitCode}</TableCell>
                                    </TableRow>
                                </TableBody>
                            </Table>
                        </TableCell>
                    </TableRow>
                ))
        );
    }
}
export default NestedGrid;