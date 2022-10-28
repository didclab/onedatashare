import React from "react";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import Cancel from "@material-ui/icons/Cancel";
import Refresh from "@material-ui/icons/Refresh";
import DeleteOutline from "@material-ui/icons/DeleteOutline";
import {humanReadableSpeed} from "../../../../utils";
import {Hidden} from "@material-ui/core";
import QueueProgressBar from "../QueueProgressBar";
import JobActionButton from "./JobActionButton";
import JobInfoButton from "./JobInfoButton";
import moment from "moment";
import InfoRow from "./InfoRow";

export default class RowElement extends React.Component {

    infoRow() {
        return (
            <InfoRow
                resp={this.props.resp}
                span={this.props.adminPg ? 9 : 7}
                />
        );
    }

    renderActions(owner, jobID, status, deleted) {
        const {infoButtonOnClick, cancelButtonOnClick, restartButtonOnClick, deleteButtonOnClick} = this.props
        const titles = ["Cancel", "Restart", "Delete"];
        const events = [cancelButtonOnClick, restartButtonOnClick, deleteButtonOnClick];
        const icons = [<Cancel/>, <Refresh/>, <DeleteOutline/>];
        const log = [status === 'transferring' || status === 'scheduled',
            status !== 'transferring' && status !== 'scheduled',
            status !== 'transferring' && status !== 'scheduled' && !deleted]
        let butts = []
        for (let i = 0; i < titles.length; i += 1) {
            butts.push(
                log[i] &&
                <JobActionButton
                    icon={icons[i]}
                    jobId={jobID}
                    onClick={events[i]}
                    title={titles[i]}
                />
            );
        }
        return (
            <div>
                <JobInfoButton
                    jobId={jobID}
                    onClick={infoButtonOnClick}
                    owner={owner} />
                {!this.props.adminPg && butts}
            </div>
        );
    }

    render() {
        const {resp, infoVisible} = this.props
        let bar = (<QueueProgressBar status={resp.status} total={resp.bytes.total} done={resp.bytes.done}/>);
        let actions = (this.renderActions(resp.owner, resp.job_id, resp.status, resp.deleted));
        let time = moment(resp.times.started).fromNow();
        let admin = this.props.adminPg;
        return (
            <React.Fragment>
                <TableRow className={"QueueRow"} style={{alignSelf: "stretch"}}>
                    <Hidden mdDown>
                        { admin &&
                        <TableCell className={"userCell-admin queueBodyCell"}>
                            <p>{resp.owner}</p>
                        </TableCell> }
                        <TableCell className={"idCell" + (admin ? "-admin" : "") + " queueBodyCell"}>
                            <p>{resp.job_id}</p>
                        </TableCell>
                        <TableCell className={"progressCell" + (admin ? "-admin" : "") + " queueBodyCell"}>
                            {bar}
                        </TableCell>
                        <TableCell className={"speedCell" + (admin ? "-admin" : "") + " queueBodyCell"}>
                            <p>{humanReadableSpeed(resp.bytes.avg)}</p>
                        </TableCell>
                        <TableCell className={"sourceCell" + (admin ? "-admin" : "") + " queueBodyCell"}>
                            <p>{decodeURIComponent(resp.src.uri)}</p>
                        </TableCell>
                        <TableCell className={"destinationCell" + (admin ? "-admin" : "") + " queueBodyCell"}>
                            <p>{decodeURIComponent(resp.dest.uri)}</p>
                        </TableCell>
                        { this.props.adminPg &&
                        <TableCell className={"startCell-admin queueBodyCell"}>
                            <p>{time}</p>
                        </TableCell>}
                        <TableCell className={"actionCell" + (admin ? "-admin" : "") + " queueBodyCell"}>
                            {actions}
                        </TableCell>
                    </Hidden>
                    <Hidden lgUp>
                        <TableCell className="mobileCell">
                            <p><b>Job ID:</b> {resp.job_id}</p>
                            {/* <p><b>Job ID:</b> 1</p> */}
                            <p><b>Progress: </b>{bar}</p>
                            <p><b>Average Speed:</b> {humanReadableSpeed(resp.bytes.avg)}</p>
                            <p><b>Source:</b> {decodeURIComponent(resp.src.uri)}</p>
                            <p><b>Destination:</b> {decodeURIComponent(resp.dest.uri)}</p>
                            <p>{time}</p>
                            {actions}
                        </TableCell>
                    </Hidden>
                </TableRow>
                {infoVisible && this.infoRow()}
            </React.Fragment>
        );
    }
}
