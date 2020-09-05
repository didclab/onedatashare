import React, {useState} from 'react';
import Typography from "@material-ui/core/Typography";
import Button from "@material-ui/core/Button";
import IconButton from "@material-ui/core/IconButton";
import CreateIcon from "@material-ui/icons/Create";
import {spaceBetweenStyle} from "./../../constants.js";
import Input from "@material-ui/core/Input";
import DoneIcon from "@material-ui/icons/Done";
import CancelIcon from "@material-ui/icons/Clear";

export default function EditUserAccountAttributes({textDisplayed, editable=false}) {
    const buttonStyling = {minWidth:"0", padding:"10px"};
    const [editModeActive, setEditActive] = useState(false);

    //if attribute is not editable, display only the text with no attached functions 
    if (!editable){
        return(
            <Typography color="textSecondary" style={{padding:"10px"}}>
                <Typography >{textDisplayed}</Typography>
            </Typography>    
        )
    }

    
    return (
        <Typography component="div" style={spaceBetweenStyle} >
            <Typography style={{padding:"10px"}}>
                {editModeActive ?
                <Input defaultValue={textDisplayed} autoFocus/> :
                <Typography >{textDisplayed}</Typography>}
            </Typography> 
            {editModeActive ?
            <Typography component="span">
                {/** Dummy button, need to create backend API call in the future */}
                <IconButton style={buttonStyling}>
                    <DoneIcon/>
                </IconButton>
                <IconButton style={buttonStyling} onClick={()=> {setEditActive(false)}}>
                    <CancelIcon/>
                </IconButton>
            </Typography> :
            <IconButton style={buttonStyling} onClick={()=>{setEditActive(true)}}>
            <   CreateIcon/>
            </IconButton>}  
        </Typography>
    )
}
