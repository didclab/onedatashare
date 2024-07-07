import TableCell from "@mui/material/TableCell";
import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import Select from "@mui/material/Select";
import React from "react";

const QueueMobileHeader = ({ handleRequestSort, menuOpts, orderBy }) => {
  return (
    <TableCell className="queueHeaderCell mobileHeaderCell">
      <h1>Transfer History</h1>
      <div className="queueDropDown">
        <FormControl variant="">
          <InputLabel id="mobile-queue-sort-select"> Sort by </InputLabel>
          <Select
            label="Sort Category"
            labelId="mobile-queue-sort-select"
            onChange={() => {
              handleRequestSort(orderBy);
            }}
          >
            {menuOpts}
          </Select>
        </FormControl>
      </div>
    </TableCell>
  );
};

export default QueueMobileHeader;
