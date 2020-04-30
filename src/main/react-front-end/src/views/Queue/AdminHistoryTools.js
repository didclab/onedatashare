import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import Button from "@material-ui/core/Button";
import RefreshIcon from "@material-ui/icons/Refresh";
import React from "react";
import TableHead from "@material-ui/core/TableHead";

const AdminHistoryTools = ({
                               customToolbar,
                               queueFunc
                           }) => {
    return(
        <TableRow>
            <TableCell>
                Transfer History
            </TableCell>
            <TableCell>
                <Button variant="outlined" startIcon={<RefreshIcon />} color="primary" disableElevation
                        onClick={queueFunc} size="small">
                    Refresh
                </Button>
            </TableCell>
            <TableCell colSpan='2'>
                { customToolbar }
            </TableCell>
        </TableRow>
    );
};

export default AdminHistoryTools;