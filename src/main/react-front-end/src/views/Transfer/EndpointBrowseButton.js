import React from "react";
import {Button, Popper, Grid, Typography} from '@material-ui/core';
import styled from "@material-ui/core/styles/styled";

const BrowseButton = (props) => {

    const ButtonLabel = styled(Popper)({
        textAlign: "center",
        fontSize: "12px",
        color: "white",
        backgroundColor: "rgba(0,0,0,0.7)",
        borderRadius: "5px",
        padding: "5px"
    });

    const [mouseOn, setMouse] = React.useState(null);
    const hoverOpen = (event) => {
        setMouse(event.currentTarget);
        // console.log(mouseOn);
        // console.log(event.currentTarget);
    };
    const hoverClose = () => {
        console.log(mouseOn);
        setMouse(null);

    }
    const isMouseOn = Boolean(mouseOn);
    const buttonStyle = {flexGrow: 1, padding: "5px"};

    return(
        <Grid item xs={1}>
            <ButtonLabel open={isMouseOn}
                     anchorEl={mouseOn}
                     placement={"top"}
                     transition
            >
                {props.label}
            </ButtonLabel>
            <Button
                id={props.id}
                style={buttonStyle}
                disabled={props.disabled}
                onClick={props.click}
                onMouseOver={hoverOpen}
                onMouseOut={hoverClose}
            >
                {props.buttonIcon}
            </Button>
        </Grid>
    );
}

export default BrowseButton;