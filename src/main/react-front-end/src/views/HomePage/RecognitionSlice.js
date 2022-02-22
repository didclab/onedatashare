/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


import Grid from "@material-ui/core/Grid";
import {nsfImage, ubImage} from "../../constants";
import React, {Component} from "react";

export default class RecognitionSlice extends Component {

    render() {
        return (
            <Grid container
                  direction='row'
                  justifyContent='center'
                  alignItems='center'
                  className='RecognitionSlice'>

                <img width={'100px'} src={nsfImage} style={{float: 'left'}} alt="NSF Logo"/>
                <img width={'100px'} src={ubImage} style={{float: 'left'}} alt="UB Logo"/>

            </Grid>
        );
    }
}