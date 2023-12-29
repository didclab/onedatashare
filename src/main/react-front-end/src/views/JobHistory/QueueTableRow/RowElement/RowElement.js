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
          expanded: true,
        };
        this.handleToggle = this.handleToggle.bind(this);
    }
    infoRow() {
        return (
            <InfoRow
                resp={this.props.resp}
                span={this.props.adminPg ? 9 : 7}
                />
        );
    }

    handleToggle() {
        this.setState((prevState) => ({
          expanded: !prevState.expanded,
        }));
      }

    renderActions(owner, jobID, status, deleted) {
        const {infoButtonOnClick, cancelButtonOnClick, restartButtonOnClick, deleteButtonOnClick} = this.props
        const titles = ["Cancel", "Restart", "Delete"];
        const events = [cancelButtonOnClick, restartButtonOnClick, deleteButtonOnClick];
        const icons = [<Cancel/>, <Refresh/>, <DeleteOutline/>];
        const log = [status === 'transferring' || status === 'scheduled',
            status !== 'transferring' && status !== 'scheduled',
            status !== 'transferring' && status !== 'scheduled' && !deleted]
        let buttons = []
        for (let i = 0; i < titles.length; i += 1) {
            buttons.push(
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
                {!this.props.adminPg && buttons}
            </div>
        );
    }
    render() {
        const {resp, infoVisible} = this.props
        let bar = (<QueueProgressBar status={resp.status} total={resp.jobParameters.jobSize} done={resp.jobParameters.jobSize}/>);
        let difference = (Date.parse(resp.endTime) - Date.parse(resp.startTime))/1000;
        let speed = parseFloat((resp.jobParameters.jobSize/1000000)*8)/(difference);
        if (isNaN(speed))
        {
            speed = 0;
        }

        let time = moment(resp.startTime).fromNow();
        return (
            <React.Fragment>
                {this.state.expanded? <></> : 
                <React.Fragment>
                    <div className="screenShade" onClick={() => {this.handleToggle()}}>               
                        <div className="jobInfoBox" onClick={(e) => { e.stopPropagation(); }}>
                            {resp.batchSteps.map((file) => {
                                return(
                                    <div>{JSON.stringify(file.step_name)}</div>
                                )
                            })}
                        </div>
                    </div>
                </React.Fragment>
                
                }
                <TableRow className={this.state.expanded? "Row": "Row expanded"} onClick={() => {this.handleToggle()}}>
                    <Hidden mdDown >
                            <TableCell className={"idCell" + " queueBodyCell"}>
                                <p>{resp.id}</p>
                            </TableCell>
                            <TableCell className={"dateCell" + " queueBodyCell"}>
                                <p>{resp.createTime.substring(0, 10)}</p>
                            </TableCell>
                            <TableCell className={"sourceCell" + " queueBodyCell"}>
                                <p>{resp.jobParameters.sourceCredentialType}</p>
                            </TableCell>
                            <TableCell className={"destinationCell" + " queueBodyCell"}>
                                <p>{resp.jobParameters.destCredentialType}</p>
                            </TableCell>
                            <TableCell className={"jobSizeCell" + " queueBodyCell"}>
                                <p>{resp.jobParameters.jobSize}</p>
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
                        </TableCell>
                    </Hidden>
                </TableRow>
                {infoVisible && this.infoRow()}
            </React.Fragment>
        );
    }
}
