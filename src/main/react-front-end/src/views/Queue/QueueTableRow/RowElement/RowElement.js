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
                    key={`actions-${i}`}
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
        let bar = (<QueueProgressBar status={resp.status} total={ 100 || resp.bytes.total} done={100 || resp.bytes.done}/>);
        let actions = (this.renderActions(resp.owner, resp.job_id, resp.status, resp.deleted));
        let time = moment(100 || resp.times.started).fromNow();
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
                            <p>{resp.jobId}</p>
                        </TableCell>
                        <TableCell className={"progressCell" + (admin ? "-admin" : "") + " queueBodyCell"}>
                            {bar}
                        </TableCell>
                        <TableCell className={"speedCell" + (admin ? "-admin" : "") + " queueBodyCell"}>
                            <p>{humanReadableSpeed(200 || resp.bytes.avg)}</p>
                        </TableCell>
                        <TableCell className={"sourceCell" + (admin ? "-admin" : "") + " queueBodyCell"}>
                            <p>{decodeURIComponent(resp.src?.uri)}</p>
                        </TableCell>
                        <TableCell className={"destinationCell" + (admin ? "-admin" : "") + " queueBodyCell"}>
                            <p>{decodeURIComponent(resp.dest?.uri)}</p>
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
                            <div><b>Job ID:</b> {resp.job_id}</div>
                            <div><b>Progress: </b>{bar}</div>
                            <div><b>Average Speed:</b> {humanReadableSpeed(resp.bytes?.avg)}</div>
                            <div><b>Source:</b> {decodeURIComponent(resp.src?.uri)}</div>
                            <div><b>Destination:</b> {decodeURIComponent(resp.dest?.uri)}</div>
                            <div>{time}</div>
                            {actions}
                        </TableCell>
                    </Hidden>
                </TableRow>
                {infoVisible && this.infoRow()}
            </React.Fragment>
        );
    }
}
