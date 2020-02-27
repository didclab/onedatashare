import React from "react";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import AppBar from "@material-ui/core/AppBar";
import Tabs from "@material-ui/core/Tabs";
import Tab from "@material-ui/core/Tab";
import {ProgressBar} from "react-bootstrap";
import Tooltip from "@material-ui/core/Tooltip";
import Zoom from "@material-ui/core/Zoom";
import Button from "@material-ui/core/Button";
import Info from "@material-ui/core/SvgIcon";
import {humanReadableSpeed} from "../../utils";
import {TabContent} from "./TabContent.js";
import {Cancel, DeleteOutline, Refresh} from "@material-ui/icons";


export class RowElement extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        return (
        <p>hi</p>
        );
    }
}