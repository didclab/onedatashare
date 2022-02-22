import LinearProgress from "@material-ui/core/LinearProgress";
import React from "react";
import Grid from "@material-ui/core/Grid";

const QueueProgressBar = ({
                              status,
                              total,
                              done
                          }) => {
    var bar = null;
    var progress = null;
    if (status === 'complete') {
        bar = (
            <LinearProgress className={"queueBar"} variant="determinate" value={100} />
        );
        progress = "Done"
    }
    else if (status === 'failed') {
        bar = (
            <LinearProgress className={"queueBar queueBarFailed"} variant="determinate" value={100} />
        );
        progress = "Failed"
    }
    else if (status === 'removed' || status === "cancelled") {
        bar = (
            <LinearProgress className={"queueBar queueBarFailed"} variant="determinate" value={100} />
        );
        progress = "Cancelled"
    }
    else {
        progress = Math.ceil(((done / total) * 100));
        bar = (
            <LinearProgress className={"queueBar queueBarLoading"} variant="determinate" value={progress} />
        );
        progress = progress.toString().concat("%");
    }
    return (
        <Grid className={QueueProgressBar} container direction='row'>
            <Grid container xs={2} md={4}>
                <p>{progress}</p>
            </Grid>
            <Grid xs={10} md={8}>
                {bar}
            </Grid>
        </Grid>
    );
};

export default QueueProgressBar;