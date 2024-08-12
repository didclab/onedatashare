import TableBody from "@material-ui/core/TableBody";
import CircularProgress from "@material-ui/core/CircularProgress";
import React from "react";

const QueueTableBodyView = ({
                                loading,
                                populateRows
                            }) => {
    const [timeout,setTime] = React.useState(false);
    setTimeout(() => {
        setTime(true)
    },15000);
    return (
        <TableBody style={{height:'100%'}}>
            { loading ?
                timeout ? '' : <CircularProgress/>
                :
                populateRows()
            }
        </TableBody>
    );
};

export default QueueTableBodyView;