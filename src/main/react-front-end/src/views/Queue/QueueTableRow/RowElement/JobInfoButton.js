import Tooltip from "@mui/material/Tooltip";
import Zoom from "@mui/material/Zoom";
import Button from "@mui/material/Button";
import React from "react";
import Info from "@mui/icons-material/Info";

const JobInfoButton = ({ jobId, onClick, owner }) => {
  return (
    <Tooltip
      TransitionComponent={Zoom}
      placement="top"
      title="Detailed Information"
    >
      <Button
        onClick={onClick.bind(null, owner, jobId)}
        variant="contained"
        size="small"
        color="primary"
        style={{
          backgroundColor: "rgb(224, 224, 224)",
          color: "#333333",
          fontFamily: "FontAwesome",
          fontSize: "1.5rem",
          height: "30%",
          fontWeight: "bold",
          width: "20%",
          textTransform: "none",
          minWidth: "0px",
          minHeigth: "0px",
        }}
      >
        <Info />
      </Button>
    </Tooltip>
  );
};

export default JobInfoButton;
