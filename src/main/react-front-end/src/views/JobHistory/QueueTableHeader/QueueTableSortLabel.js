import TableSortLabel from "@mui/material/TableSortLabel";
import React from "react";

const QueueTableSortLabel = ({
  handleRequestSort,
  order,
  orderBy,
  sortKey,
  title,
}) => {
  return (
    <TableSortLabel
      active={orderBy === sortKey}
      className={"queueHeaderCell"}
      direction={order}
      onClick={() => {
        handleRequestSort(sortKey);
      }}
    >
      <p>{title}</p>
    </TableSortLabel>
  );
};

export default QueueTableSortLabel;
