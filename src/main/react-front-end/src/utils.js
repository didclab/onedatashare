/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


export function humanReadableSpeed(size) {
	if (size < 1024) 
		return parseFloat(size.toFixed(2)) + ' Mb/s';
    let i = Math.floor(Math.log(size) / Math.log(1024));
	let num = (size / Math.pow(1024, i));
    let round = Math.round(num);
    num = round < 10 ? num.toFixed(2) : round < 100 ? num.toFixed(1) : round;
    num = num*(1);
    num = parseFloat(num.toFixed(4));
    return `${num} ${'KMGTPEZY'[i]}b/s`
}

// generates n random colors for e.g. n = 2 res = ["#123134", "#452512"]
export const generateNewColors = (n) => {
    let res = [];
    const hexCharacters = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, "A", "B", "C", "D", "E", "F"];
    let hexColorRep;

    for (let i = 0; i < n; i += 1) {
        hexColorRep = "#";
        for (let index = 0; index < 6; index++) {
            const randomPosition = Math.floor(Math.random() * hexCharacters.length);
            hexColorRep += hexCharacters[randomPosition];
        }
        res.push(hexColorRep);
    }
    return res;
}

// Helper: Get random timestamp between two millis, part of getRandomData
function getRandomTime(start, end) {
    return Math.floor(Math.random() * (end - start)) + start;
}

export const getRandomJobs = (isHistory, startIso, endIso) => {
    let temp = [];
    let start;
    let end;
    let count = 10;
    if (isHistory) {
        start = new Date(startIso).getTime();
        end = new Date(endIso).getTime();
    } else {
        start = new Date();
        start.setDate(start.getDate() + 7);
        end = new Date();
        end.setFullYear(start.getFullYear() + 1); // within 1 year
        start = start.getTime();
        end = end.getTime();
        count = 100;
    }    
    for (let i = 0; i < count; i++) {
        const jobStart = new Date(getRandomTime(start, end - 24 * 60 * 60 * 1000)); // Reserve at least 1 hour for duration
        const jobEnd = new Date(getRandomTime(jobStart.getTime() + 2 * 60 * 60 * 1000, Math.min(jobStart.getTime() + 18 * 60 * 60 * 1000, end))); // 2 - 18 hours max
    
        if (jobStart.getTime() >= start && jobEnd.getTime() <= end) {
            if (isHistory) {
                temp.push({
                    id: i + 1,
                    startTime: jobStart.toISOString().split('.')[0],
                    endTime: jobEnd.toISOString().split('.')[0],
                });
            } else {
                temp.push({
                    jobUuid: String(i + 1),
                    jobStartTime: jobStart.toISOString().split('.')[0],
                });
            }
        }
    }
    let response = {
        status: 200,
        data: temp
    };
    return new Promise(resolve => {
        setTimeout(() => {
            resolve(response);
        }, 1000);
    })
}

export const parseVisualizationData = (jobs, isHistory) => {
    let response = [];
    if (isHistory) {
        // array of jobs with the following keys
        // private Long id;
        // private Long version;
        // private Long jobInstanceId;
        // private Timestamp createTime;
        // private Timestamp startTime;
        // private Timestamp endTime;
        // private String status;
        // private String exitCode;
        // private String exitMessage;
        // private Timestamp lastUpdated;
        // List<BatchStepExecution> batchSteps;
        // Map<String,String> jobParameters;
        jobs.forEach(element => {
            let job = {};
            job.node = String(element.id);
            job.schedule = [
                new Date(Date.parse(element.startTime)).getTime(),
                new Date(Date.parse(element.endTime)).getTime()
            ]
            response.push(job);
        });
    } else {
        // array of jobs with the following keys
        // LocalDateTime jobStartTime;
        // UUID jobUuid;
        // String ownerId;
        // FileSource source;
        // FileDestination destination;
        // UserTransferOptions options;
        // String transferNodeName;
        jobs.forEach(element => {
            let job = {};
            let start = new Date(Date.parse(element.jobStartTime));
            let end = new Date(start.getTime());
            end.setDate(end.getDate() + 1);
            job.node = element.jobUuid;
            job.schedule = [
                start.getTime(),
                end.getTime()
            ];
            response.push(job);
        });
    }
    return response;
}