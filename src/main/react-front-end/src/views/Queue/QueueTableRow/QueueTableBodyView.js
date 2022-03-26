import TableBody from "@material-ui/core/TableBody";

import React from "react";

const QueueTableBodyView = ({
                                populateRows
                            }) => {
    return (
        <TableBody style={{height:'100%'}}>
            {populateRows()}
        </TableBody>
    );
};

export default QueueTableBodyView;