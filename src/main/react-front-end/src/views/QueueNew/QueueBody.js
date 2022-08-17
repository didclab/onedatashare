import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import React from "react";
import IconButton from "@material-ui/core/IconButton";
import SvgMore from "@material-ui/icons/ExpandMore";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import {Component} from "react";
import NestedGrid from "./NestedGrid";


class QueueBody extends Component {

    constructor(props) {
        super(props);

        this.state = {
            expand: false,
            batchStepData: []
        };

        this.handleExpand = this.handleExpand.bind(this);
    }

    handleExpand(row) {
        //debugger
        console.log('row', row)
        this.setState({
            expand: !this.state.expand,
            batchStepData: row
        });
    };

    render() {
        return (
            <TableBody>
                {this.props.rowData.map((row) => (
                    <TableRow key={row.id} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                        <TableCell component="th" scope="row" className={"columnDataCell"} >
                            {row.id}
                        </TableCell>
                        <TableCell className={"columnDataCell"}>{row.startTime}</TableCell>
                        <TableCell className={"columnDataCell"}>{row.endTime}</TableCell>
                        <TableCell className={"columnDataCell"}>{row.status}</TableCell>
                        <TableCell className={"columnDataCell"}>{row.exitCode}</TableCell>
                        <TableCell className={"columnDataCell"}>{row.exitMessage}</TableCell>
                        <TableCell className={"columnDataCell"}>{row.lastUpdated}</TableCell>
                        <TableCell className={"columnDataCell"}>{row.jobParameters.appName}</TableCell>
                        <TableCell className={"columnDataCell"}>{row.jobParameters.sourceCredential ? row.jobParameters.sourceCredential  + ' ' +row.jobParameters.sourceCredentialType : ''}</TableCell>
                        <TableCell >
                            <IconButton id={row.id} onClick={() => this.handleExpand(row.batchSteps)}>
                                <SvgMore />
                            </IconButton>
                        </TableCell>
                    </TableRow>
                ))}
                {this.state.expand ? (
                    <NestedGrid stepData={this.state.batchStepData}/>
                ) : null}
            </TableBody>
        );
    }

}
export default QueueBody;