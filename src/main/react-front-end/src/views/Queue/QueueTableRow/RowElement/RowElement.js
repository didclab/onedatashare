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
    constructor(props) {
        super(props);
        this.state = {
            bar: null,
            actions: null,
            speed: 0,
        };
    }
    
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
                    key={jobID}
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

    calculateState = () => {
        const { resp } = this.props;
        
        // Calculate progress bar and actions
        let bar = (<QueueProgressBar status={resp.status} resp={resp} />);
        let actions = this.renderActions(resp.owner, resp.job_id, resp.status, resp.deleted);

        // Calculate speed
        let speed = 0;
        for (let element of resp.batchSteps) {
            const fileInfo = JSON.parse(resp.jobParameters[element.step_name]);
            let sizeWritten = element.writeCount * fileInfo.chunkSize * 8; // Convert bytes to bits
            let time_difference = (Date.parse(resp.endTime) ? Date.parse(resp.endTime) : Date.now() - Date.parse(resp.startTime)) / 1000;
            speed += (sizeWritten / time_difference) / resp.batchSteps.length;
        }

        if (isNaN(speed)) {
            speed = 0;
        }

        // Update the state with calculated values
        this.setState({ bar, actions, speed });
    }

    componentDidMount() {
        this.calculateState();
    }

    componentDidUpdate(prevProps) {
        if (prevProps.resp !== this.props.resp || prevProps.infoVisible !== this.props.infoVisible) {
            this.calculateState();
        }
    }

    render() {
        const {resp, infoVisible} = this.props
        const {bar, actions, speed} = this.state;

        let time = moment(resp.startTime).fromNow();
        return (
            <React.Fragment>
                <TableRow className={"QueueRow"} style={{alignSelf: "stretch"}}>
                    <Hidden mdDown>
                        <TableCell className={"idCell" + " queueBodyCell"}>
                            <p>{resp.id}</p>
                        </TableCell>
                        <TableCell className={"progressCell" + " queueBodyCell"}>
                            {bar}
                        </TableCell>
                        <TableCell className={"speedCell" + " queueBodyCell"}>
                            <p>{humanReadableSpeed(speed)}</p>
                        </TableCell>
                        <TableCell className={"sourceCell" + " queueBodyCell"}>
                            <p>{resp.jobParameters.sourceCredential}</p>
                        </TableCell>
                        <TableCell className={"destinationCell" + " queueBodyCell"}>
                            <p>{resp.jobParameters.destCredential}</p>
                        </TableCell>
                        <TableCell className={"actionCell" + " queueBodyCell"}>
                            {actions}
                        </TableCell>
                    </Hidden>
                    <Hidden lgUp>
                        <TableCell className="mobileCell">
                            <p><b>Job ID:</b> {resp.id}</p>
                            <p><b>Progress: </b>{bar}</p>
                            <p><b>Average Speed:</b> {humanReadableSpeed(speed)}</p>
                            <p><b>Source:</b> {resp.jobParameters.sourceBasePath}</p>
                            <p><b>Destination:</b>{resp.jobParameters.destBasePath}</p>
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
