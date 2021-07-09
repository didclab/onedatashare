import React from "react";
import {Button, Popper, Grid, ButtonGroup} from '@material-ui/core';
import styled from "@material-ui/core/styles/styled";

const BrowseButton = (props) => {

    const ButtonLabel = styled(Popper)({
        textAlign: "center",
        fontSize: "12px",
        color: "white",
        backgroundColor: "rgba(0,0,0,0.7)",
        padding: "5px"
    });

    const [mouseOn, setMouse] = React.useState(null);
    const [label, setLabel] = React.useState("");
    const hoverOpen = (event, label) => {
        setMouse(event.currentTarget);
        // console.log(mouseOn);
        setLabel(label);
    };
    const hoverClose = () => {
        console.log(mouseOn);
        setMouse(null);

    }
    const isMouseOn = Boolean(mouseOn);
    const id = props.id;

    if(!props.buttongroup){
        return(
            <Grid item xs={1}>
                <Button
                    id={props.id}
                    style={props.style}
                    disabled={props.disabled}
                    onClick={props.click}
                    onMouseOver={(event, label) => hoverOpen(event, props.label)}
                    onMouseOut={hoverClose}
                >
                    {props.buttonIcon}
                </Button>
                <ButtonLabel open={isMouseOn}
                             anchorEl={mouseOn}
                             placement={"top"}
                             transition
                >
                    {label}
                </ButtonLabel>
            </Grid>
        );
    }else{
        return(
            <ButtonGroup fullWidth>
                {id.map(function (value,index){
                    return(
                        <Button
                            id={props.id[index]}
                            style={props.style[index]}
                            onClick={props.click[index]}
                            onMouseOver={(event, label) => hoverOpen(event, props.label[index])}
                            onMouseOut={hoverClose}
                        >
                            {props.buttonIcon[index]}
                        </Button>
                    )
                })}
                <ButtonLabel open={isMouseOn}
                             anchorEl={mouseOn}
                             placement={"top"}
                             transition
                >
                    {label}
                </ButtonLabel>
            </ButtonGroup>
        );
    }


}

export default BrowseButton;