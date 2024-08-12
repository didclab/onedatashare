import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TableCell from "@mui/material/TableCell";
import React from "react";
import { Hidden } from "@mui/material";
import MenuItem from "@mui/material/MenuItem";
import QueueMobileHeader from "./QueueMobileHeader";
import QueueTableSortLabel from "./QueueTableSortLabel";

function makeHeaderCells(order, orderBy, handleRequestSort) {
  let headers = [];
  let menuOpts = [];

  const headerCells = {
    id: { title: "Job ID", class: "idCell" },
    version: { title: "Version", class: "versionCell" },
    jobInstanceId: { title: "Job Instance ID", class: "jobInstanceIdCell" },
    createTime: { title: "Date", class: "createTimeCell" },
    startTime: { title: "Start Time", class: "startTimeCell" },
    status: { title: "Status", class: "statusCell" },
    exitCode: { title: "Exit Code", class: "exitCodeCell" },
    exitMessage: { title: "Exit Message", class: "exitMessageCell" },
    lastUpdated: { title: "Last Updated", class: "lastUpdatedCell" },
    batchSteps: { title: "Batch Steps", class: "batchStepsCell" },
    jobParameters: { title: "Job Parameters", class: "jobParametersCell" },
    speed: { title: "Speed", class: "speedCell" },
    source: { title: "Source", class: "sourceCell" },
    destination: { title: "Destination", class: "destinationCell" },
    jobSize: { title: "Job Size", class: "jobSizeCell" },
  };

  const enabledHeaders = [
    "id",
    "createTime",
    "source",
    "destination",
    "jobSize",
  ];

  for (const key of enabledHeaders) {
    const item = headerCells[key];
    if (item) {
      headers.push(
        <TableCell className={item.class + " queueHeaderCell"} key={key}>
          <QueueTableSortLabel
            handleRequestSort={handleRequestSort}
            order={order}
            orderBy={orderBy}
            sortKey={key}
            title={item.title}
          />
        </TableCell>
      );
      menuOpts.push(
        <MenuItem key={key} value={key}>
          {item.title}
        </MenuItem>
      );
    }
  }

  return [headers, menuOpts];
}

const QueueTableHeaderView = ({ handleRequestSort, order, orderBy }) => {
  let [headerCells, menuOpts] = makeHeaderCells(
    order,
    orderBy,
    handleRequestSort
  );
  return (
    <TableHead>
      <TableRow>
        <Hidden mdDown>{headerCells}</Hidden>
        <Hidden lgUp>
          <QueueMobileHeader
            handleRequestSort={handleRequestSort}
            menuOpts={menuOpts}
            orderBy={orderBy}
          />
        </Hidden>
      </TableRow>
    </TableHead>
  );
};

export default QueueTableHeaderView;
