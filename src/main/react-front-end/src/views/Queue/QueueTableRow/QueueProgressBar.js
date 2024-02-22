import LinearProgress from "@material-ui/core/LinearProgress";
import Grid from "@material-ui/core/Grid";
import React, { useState, useMemo } from 'react';

const QueueProgressBar = ({
                              status,
                              resp
                          }) => {
    
    const calculateProgress = useMemo (()=> {
        let filelist = []
        let totalJobSize = resp.jobParameters.jobSize
        let totalWrote = 0
        for (const file of resp.batchSteps) {
            // console.log(file.writeCount)
            let temp = resp.jobParameters[file.step_name]
            try {
                temp = JSON.parse(temp)
                totalWrote = file.writeCount * temp.chunkSize
            }
            catch (error) {
                continue
            }
        }
        // console.log(filelist)
        console.log(totalWrote / totalJobSize * 100)
        return Math.ceil((totalWrote / totalJobSize) * 100)
    }, [resp])


    var bar = <LinearProgress className={"queueBar queueBarLoading"} variant="determinate" value={calculateProgress}/>
    var progress = status
    
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