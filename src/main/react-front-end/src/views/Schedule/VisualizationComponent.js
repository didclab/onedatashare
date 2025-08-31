import { React, useState, useEffect } from "react";
import { Box } from "@material-ui/core";
import { FormControl, RadioGroup, FormControlLabel, Radio, Typography, styled, Button, CircularProgress, Backdrop } from "@material-ui/core";
import { DateTimePicker, LocalizationProvider } from "@mui/x-date-pickers";
import { makeStyles } from "@material-ui/core/styles";
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { XAxis, YAxis, Tooltip, ResponsiveContainer, BarChart, Bar, Cell } from 'recharts';
import { getPreviousJobs, getQueuedJobs } from "../../APICalls/APICalls";
import { generateNewColors } from "../../utils";
import { eventEmitter } from "../../App.js";

const useStyles = makeStyles((theme) => ({
    backdrop: {
        zIndex: theme.zIndex.drawer + 1,
        position: "absolute",
        backgroundColor: "rgba(255, 255, 255, 0.8)"
    },
    noData: {
        position: "absolute",
        top: "50%",
        left: "50%",
        transform: "translate(-50%, -50%)"
    }
}))

// returns list of timestamps that will be displayed on x axis
const getXTicks = (data) => {
    if (!data || data.length === 0) return [];

    // Find min start and max end timestamps
    const { minStart, maxEnd } = data.reduce(
        (acc, curr) => {
            acc.minStart = Math.min(acc.minStart, curr.schedule[0]);
            acc.maxEnd = Math.max(acc.maxEnd, curr.schedule[1]);
            return acc;
        },
        { minStart: Infinity, maxEnd: -Infinity }
    );

    // Get the start of the minStart day in local timezone
    const startDate = new Date(minStart);
    startDate.setHours(0, 0, 0, 0); // Set to start of the day

    // Get the start of the next day of maxEnd in local timezone
    const endDate = new Date(maxEnd);
    endDate.setHours(0, 0, 0, 0); // Start of maxEnd's day
    endDate.setDate(endDate.getDate() + 1); // Move to next day

    // Generate timestamps with 1-day gap
    const timestamps = [];
    let currentTimestamp = startDate.getTime();

    while (currentTimestamp <= endDate.getTime()) {
        timestamps.push(currentTimestamp);
        startDate.setDate(startDate.getDate() + 1);
        // currentTimestamp += 24 * 60 * 60 * 1000; does not work / daylight savings and hence add 1 day in milliseconds
        currentTimestamp = startDate.getTime();
    }

    return timestamps;
}

const formatTooltip = (value, name, props) => {
    if (value.length < 2) {
        return "";
    }
    let start = new Date(value[0]);
    let end = new Date(value[1]);
    return `${start.toDateString()} ${start.toLocaleTimeString()} - ${end.toDateString()} ${end.toLocaleTimeString()} `;
}

const xAxisFormatter = (timestamp) => {
    return new Date(timestamp).toDateString().split(' ').slice(1).join(' ');
}

const fieldLabelStyle = () => styled(Typography)({
    fontSize: "12px"
});

const VisualizationComponent = () => {
    const [formData, setFormData] = useState({
        day: "30",
        startDateTime: null,
        endDateTime: null,
        history: true
    });
    const [isQueuedJobsFetched, setQueuedJobsFetched] = useState(false);
    const [queuedJobs, setQueuedJobs] = useState([]);
    const [data, setData] = useState([]);
    const [xticks, setXticks] = useState([]);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');
    const [barHovered, setBarHovered] = useState(false);
    const [barColors, setBarColors] = useState([]);

    const classes = useStyles();

    const FieldLabel = fieldLabelStyle();

    const filterQueuedJobs = (startDateTime, endDateTime, jobsData) => {
        let new_res = [];
        setLoading(true);
        const startTimeStamp = startDateTime.getTime();
        const endTimeStamp = endDateTime.getTime();
        jobsData.forEach(jobs => {
            if (startTimeStamp <= jobs.schedule[0]  && jobs.schedule[1] <= endTimeStamp) {
                new_res.push(jobs);
            }
        });
        setMessage("");
        setData(new_res);
        setXticks(getXTicks(new_res));
        setBarColors(generateNewColors(new_res.length));
        if (new_res.length == 0)
            setMessage("No Data");
        setLoading(false);
    }

    const getData = (startDateTime, endDateTime) => {
        if (formData.history) {
            setLoading(true);
            getPreviousJobs(startDateTime, endDateTime, (fetchedData) => {
                setMessage("");
                setData(fetchedData);
                setXticks(getXTicks(fetchedData));
                setBarColors(generateNewColors(fetchedData.length))
                setLoading(false);
                if (fetchedData.length == 0) {
                    setMessage("No Data");
                }
            }, (err) => {
                setMessage(err);
                setData([]);
                setXticks([]);
                setBarColors([])
                setLoading(false);
            })
        } else if (isQueuedJobsFetched) {
            filterQueuedJobs(startDateTime, endDateTime, queuedJobs);
        } else {
            setLoading(true);
            getQueuedJobs((fetchedData) => {
                setLoading(false);
                setQueuedJobs(fetchedData);
                filterQueuedJobs(startDateTime, endDateTime, fetchedData);
                setQueuedJobsFetched(true);
            }, (err) => {
                setMessage(err);
                setData([]);
                setXticks([]);
                setBarColors([]);
                setLoading(false);
            });
        }
    }

    useEffect(() => {
        searchJobs();
    }, []);

    const handleFormData = (name, event) => {
        switch (name) {
            case "history":
                setFormData({
                    ...formData,
                    history: event.target.value === "history"
                });
                break;
            case "day":
                setFormData({
                    ...formData,
                    day: event.target.value,
                    startDateTime: null,
                    endDateTime: null
                });
                break;
            case "startDateTime":
                setFormData({
                    ...formData,
                    day: "",
                    startDateTime: event
                });
                break;
            case "endDateTime":
                setFormData({
                    ...formData,
                    day: "",
                    endDateTime: event
                });
                break;
        }
    }


    const searchJobs = () => {
        // perform validations
        let startDateTime;
        let endDateTime;
        if (formData.day) {
            startDateTime = new Date();
            startDateTime.setHours(0, 0, 0, 0);
            endDateTime = new Date();
            endDateTime.setHours(0, 0, 0, 0);
            if (formData.history) {
                endDateTime.setDate(endDateTime.getDate() + 1);
                startDateTime.setDate(endDateTime.getDate() - 1 - parseInt(formData.day));
            } else {
                endDateTime.setDate(startDateTime.getDate() + parseInt(formData.day) + 1);
            }
        } else if (!formData.startDateTime || !formData.endDateTime) {
            eventEmitter.emit("errorOccured", "Please enter start date and end date");
            return;
        }
        else {
            startDateTime = new Date(formData.startDateTime);
            endDateTime = new Date(formData.endDateTime);
            if (startDateTime >= endDateTime) {
                eventEmitter.emit("errorOccured", "Start date must be before end Date");
                return;
            }
        }
        getData(startDateTime, endDateTime);
    }

    return (
        <Box className="scheduleVisualization">
            <Box className="scheduleVisualization__container">
                {
                    loading && <Backdrop className={classes.backdrop} open={true}>
                        <CircularProgress />
                    </Backdrop>
                }
                <>
                    <Box className="scheduleVisualization__form">

                        <FormControl component="fieldset">
                            <RadioGroup row aria-label="position" value={formData.history ? "history" : "queued"} onChange={(e) => handleFormData("history", e)}>
                                <FormControlLabel
                                    value="history"
                                    control={<Radio color="primary" />}
                                    label="History"
                                />
                                <FormControlLabel
                                    value="queued"
                                    control={<Radio color="primary" />}
                                    label="Queued"
                                />
                            </RadioGroup>
                        </FormControl>


                        <FormControl component="fieldset">
                            <RadioGroup row aria-label="position" value={formData.day} onChange={(e) => handleFormData("day", e)}>
                                <FormControlLabel
                                    value="1"
                                    control={<Radio color="primary" />}
                                    label={formData.history ? "Previous 1 Day" : "Next 1 Day"}
                                />
                                <FormControlLabel
                                    value="7"
                                    control={<Radio color="primary" />}
                                    label={formData.history ? "Previous 7 Days" : "Next 7 Days"}
                                />
                                <FormControlLabel
                                    value="30"
                                    control={<Radio color="primary" />}
                                    label={formData.history ? "Previous 30 Days" : "Next 30 Days"}
                                />
                            </RadioGroup>
                        </FormControl>

                        <FormControl component="fieldset">
                            <LocalizationProvider dateAdapter={AdapterDayjs}>
                                <DateTimePicker label={<FieldLabel>Start</FieldLabel>} onChange={(e) => handleFormData("startDateTime", e)} value={formData.startDateTime} viewRenderers={{ hours: null, minutes: null, seconds: null }} />
                            </LocalizationProvider>
                        </FormControl>
                        <FormControl component="fieldset">
                            <LocalizationProvider dateAdapter={AdapterDayjs}>
                                <DateTimePicker label={<FieldLabel>End</FieldLabel>} onChange={(e) => handleFormData("endDateTime", e)} value={formData.endDateTime} viewRenderers={{ hours: null, minutes: null, seconds: null }} />
                            </LocalizationProvider>
                        </FormControl>

                        <FormControl component="fieldset">
                            <Button
                                onClick={searchJobs}
                                style={{ backgroundColor: "#172753", color: "white" }}
                            >
                                Search
                            </Button>
                        </FormControl>
                    </Box>

                    <Box className="scheduleVisualization__chart">
                        {message.length != 0 ?
                            <Box className={classes.noData}>{message}</Box>
                            :
                            <ResponsiveContainer height={data.length > 10 ? 60 * data.length : "98%"} width={xticks.length > 10 ? 120 * xticks.length : "98%"}>
                                <BarChart data={data} margin={{ top: 20, right: 20, bottom: 20, left: 20 }} layout="vertical">
                                    <XAxis type="number" domain={[xticks[0], xticks[xticks.length - 1]]} ticks={xticks} tickFormatter={xAxisFormatter} />
                                    <YAxis dataKey="node" type="category" />
                                    <Tooltip formatter={formatTooltip} cursor={false} active={barHovered} />
                                    <Bar barSize={30} dataKey="schedule" fill="#8884d8" onMouseEnter={() => setBarHovered(true)}
                                        onMouseLeave={() => setBarHovered(false)} >
                                        {
                                            data.map((entry, index) => (
                                                <Cell key={`cell-${index}`} fill={barColors[index]} />
                                            ))
                                        }
                                    </Bar>
                                </BarChart>
                            </ResponsiveContainer>
                        }
                    </Box>
                </>
            </Box>
        </Box>
    );
}

export default VisualizationComponent;
