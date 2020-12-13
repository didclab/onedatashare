import Tooltip from "@material-ui/core/Tooltip";
import Zoom from "@material-ui/core/Zoom";
import Button from "@material-ui/core/Button";
import React from "react";

const JobActionButton = ({
                             icon,
                             jobId,
                             onClick,
                             title
                         }) => {
    return(
        <Tooltip TransitionComponent={Zoom} placement="top" title={title}>
            <Button onClick={onClick.bind(null, jobId)} variant="contained" size="small" color="primary"
                    style={{
                        backgroundColor: 'rgb(224, 224, 224)',
                        color: '#333333',
                        fontSize: '1.5rem',
                        fontWeight: 'bold',
                        width: '20%',
                        height: '20%',
                        textTransform: 'none',
                        minWidth: '0px',
                        minHeigth: '0px'
                    }} >
                {icon}
            </Button>
        </Tooltip>
    );
};

export default JobActionButton;