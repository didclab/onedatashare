import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import React from 'react';
import QueueTableSortLabel from "./QueueTableSortLabel";
import {Hidden} from "@material-ui/core";
import MenuItem from "@material-ui/core/MenuItem";
import Tooltip from "@material-ui/core/Tooltip";
import QueueMobileHeader from "./QueueMobileHeader";
import AdminHistoryTools from "./AdminHistoryTools";
import { useEffect, useState } from "react";
import TableBody from "@material-ui/core/TableBody";
import { axios } from "../../../APICalls/APICalls";


function makeHeaderCells(adminPg, order, orderBy, handleRequestSort, sortableColumns) {
    let labels = [];
    let headers = [];
    let menuOpts = [];
    let titles = ["Job ID", "Progress", "Speed", "Source", "Destination"];
    let classes = ["idCell", "progressCell", "speedCell", "sourceCell", "destinationCell"];
    let keys = [sortableColumns.jobId, sortableColumns.status, sortableColumns.avgSpeed, sortableColumns.source, sortableColumns.destination];
    if (adminPg) {
        for (let i = 0; i < classes.length; i += 1) {
            classes[i] = classes[i] + "-admin";
        }
        titles.splice(0, 0, "User");
        classes.splice(0, 0, "userCell-admin");
        keys.splice(0,0, sortableColumns.userName);
        titles.push("Start Time");
        classes.push("timeCell-admin");
        keys.push(sortableColumns.startTime);
    }
    for (let i = 0; i < titles.length; i += 1) {
        labels.push(
            <QueueTableSortLabel
                handleRequestSort={handleRequestSort}
                order={order}
                orderBy={orderBy}
                sortKey={keys[i]}
                title={titles[i]}
            />
        );
    }
    for (let i = 0; i < titles.length; i += 1) {
        headers.push(
            <Tooltip title={"Sort by" + titles[i]} placement='bottom-end'>
                <TableCell className={classes[i] + " queueHeaderCell"}>
                    {labels[i]}
                </TableCell>
            </Tooltip>
        );
        menuOpts.push(
            <MenuItem value={keys[i]}>
                {titles[i]}
            </MenuItem>
        );
    }
    return [headers, menuOpts];
};

const rows = [];
const QueueTableHeaderView = ({
                                  adminPg,
                                  customToolbar,
                                  handleRequestSort,
                                  order,
                                  orderBy,
                                  page,
                                  queueFunc,
                                  refreshSuccess,
                                  refreshFailure,
                                  rowsPerPage,
                                  sortableColumns,
                              }) => {
            const [data, setData] = useState([]);
            const [influxData, setInfluxData] = useState([[]]);

            useEffect(() => {
                axios
                  .get("/api/metadata/all/page/jobs",{
                    params :
                {	
                    page:1,
                    direction:"desc",
                    size:rowsPerPage,
                    sort:"id"
                }})
                  .then((res) => {
                    for(let j=0;j<res.data.content[j].length;j++)
                    {
                        if (res.data.content[j].status==="STARTED" || res.data.content[j].status==="STARTING")
                        {
                            axios.get("/api/metadata/measurements/job",{
                                params :
                                {
                                    jobId:res.data.content[j].id
                                }
                            })
                                .then((influx_response) => {
                                    setInfluxData((influxData) => [...influxData, influx_response.data]);                                    
                                })
                                .catch((error) => {
                                    console.log(error);
                                });
                        }  
                    }
                    setData(res.data.content);

                  })
                  .catch((error) => {
                    console.log(error);
                  });
              }, [rowsPerPage]);
            console.log("Influx data",influxData);
            var difference;

            for(let i=0;i<data.length;i++)
            {
                if (data[i].status=="COMPLETED")
                {
                    difference = Date.parse(data[i].endTime)/1000 - Date.parse(data[i].startTime)/1000
                    data[i]["speed"]=parseFloat((data[i].jobParameters.jobSize/1000000)*8)/(difference);
                }
                else if (data[i].status=="STARTING" || data[i].status=="STARTED")
                {
                    //To be computed
                    data[i]["speed"] = 5;
                }
                else{
                    data[i]["speed"]=0;
                }
            }
    let [headerCells, menuOpts] = makeHeaderCells(adminPg, order, orderBy, handleRequestSort, sortableColumns);
    return (
        <>
        <TableHead >
            { adminPg && <AdminHistoryTools
                customToolbar={customToolbar}
                order={order}
                orderBy={orderBy}
                page={page}
                refreshFailure={refreshFailure}
                refreshSuccess={refreshSuccess}
                rowsPerPage={rowsPerPage}
                queueFunc={queueFunc}
            />
            }
            <TableRow >
                <Hidden mdDown>
                    {headerCells}
                    <TableCell className="actionCell queueHeaderCell"><p>Actions</p></TableCell>
                </Hidden>
                <Hidden lgUp>
                    <QueueMobileHeader
                        handleRequestSort={handleRequestSort}
                        menuOpts={menuOpts}
                        orderBy={orderBy}/>
                </Hidden>
            </TableRow>
        </TableHead>
        <TableBody>
        {data.map((row) => (
           <TableRow key={row.id}>
             <TableCell component="th" scope="row" align="center">{row.id}</TableCell>
             <TableCell align="center">{row.status}</TableCell>
             <TableCell align="center">{row.speed}</TableCell>
             <TableCell align="center">{row.jobParameters.sourceBasePath}</TableCell>
             <TableCell align="center">{row.jobParameters.destBasePath}</TableCell>
             <TableCell align="center">3</TableCell>
           </TableRow>
         ))}
        </TableBody>
        </>
    );
};

export default QueueTableHeaderView;