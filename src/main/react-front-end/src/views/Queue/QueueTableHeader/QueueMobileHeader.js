import TableCell from "@material-ui/core/TableCell";
import FormControl from "@material-ui/core/FormControl";
import InputLabel from "@material-ui/core/InputLabel";
import Select from "@material-ui/core/Select";
import {Hidden} from "@material-ui/core";
import React from "react";
import MenuItem from "@material-ui/core/MenuItem";

const QueueMobileHeader = ({
                               handleRequestSort,
                               menuOpts,
                               orderBy
                           }) => {
    return (
        <TableCell className="queueHeaderCell mobileHeaderCell">
            <h1>Transfer History</h1>
            <div className="queueDropDown">
                <FormControl variant="">
                    <InputLabel id="mobile-queue-sort-select"> Sort by </InputLabel>
                    <Select
                        label="Sort Category"
                        labelId="mobile-queue-sort-select"
                        onChange={() => {handleRequestSort(orderBy)}}
                    >
                        {menuOpts}
                    </Select>
                </FormControl>
            </div>
        </TableCell>
    );
};

export default QueueMobileHeader