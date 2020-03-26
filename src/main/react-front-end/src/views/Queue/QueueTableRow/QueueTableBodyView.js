import TableBody from "@material-ui/core/TableBody";
import CircularProgress from "@material-ui/core/CircularProgress";
import React from "react";

const QueueTableBodyView = ({
                                loading,
                                populateRows
                            }) => {
    return (
        <TableBody style={{height:'100%', display: "block"}}>
            { loading ?
                <CircularProgress />
                :
                populateRows
            }
        </TableBody>
    );
};

export default QueueTableBodyView;