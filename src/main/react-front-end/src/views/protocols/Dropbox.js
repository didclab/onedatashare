import { useEffect, useState } from 'react';
import {Button} from '@material-ui/core';
import Icon from '@material-ui/core/Icon';
import styled from "@material-ui/core/styles/styled";

export const Dropbox = (props) => {
    const EndpointButton = styled(Button)({
		flexGrow: 1,
		justifyContent: "flex-start",
		width: "100%",
		fontSize: "16px",
		paddingLeft: "35%"
	});

    return (
        <EndpointButton onClick={props.login}>
            <Icon className={props.data[1].icon + ' browseIcon'}/>
			{props.data[1].label}
        </EndpointButton>
    );
}

