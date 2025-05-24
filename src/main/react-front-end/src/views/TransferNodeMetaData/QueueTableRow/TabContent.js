import React from "react";
import {Grid} from "@material-ui/core";
import {humanReadableSpeed} from "../../../utils";
import moment from "moment";


export default class TabContent extends React.Component {

    render() {
        const resp = this.props.resp;
        const selectedTab = this.props.selectedTab;
        const info = [
            ["User", resp.owner],
            ["Job ID", resp.job_id],
            ["Source", decodeURIComponent(resp.src.uri)],
            ["Destination", decodeURIComponent(resp.dest.uri)],
            ["Instant Speed", humanReadableSpeed(resp.bytes.inst)],
            ["Average Speed", humanReadableSpeed(resp.bytes.avg)],
            ["Scheduled Time", moment(resp.times.scheduled).format("DD-MM-YYYY HH:mm:ss")],
            ["Started Time", moment(resp.times.started).format("DD-MM-YYYY HH:mm:ss")],
            ["Completed Time", moment(resp.times.completed).format("DD-MM-YYYY HH:mm:ss")],
            ["Time Duration", ((resp.times.completed - resp.times.started) / 1000).toFixed(2) + "sec"],
            ["Attempts", resp.attempts],
            ["Status", resp.status]
        ]

        if (selectedTab) {
            return <pre className={"detailedInfo"}> {JSON.stringify(resp, null, "\t")} </pre>
        } else {
            return <Grid container direction={"column"} className={"detailedInfo"}>
                {info.map(function (value){
                    return(
                        <Grid item container direction={"row"}>
                            <Grid item sm={6} xs={12}><b>{value[0]}</b></Grid>
                            <Grid item sm={6} xs={12}>{value[1]}</Grid>
                        </Grid>
                    );
                })}
            </Grid>
        }
    }
}