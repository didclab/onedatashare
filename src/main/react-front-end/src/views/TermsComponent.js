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


import React, { Component } from 'react';
import terms from '../assets/terms.txt';

export default class TermsComponent extends Component {
    state = {
        termsData: ''
    }

    readTextFile = file => {
        var rawFile = new XMLHttpRequest();
        rawFile.open("GET", file, false);
        rawFile.onreadystatechange = () => {
            if (rawFile.readyState === 4) {
                if (rawFile.status === 200 || rawFile.status === 0) {
                    var allText = rawFile.responseText;
                    this.setState({
                        termsData: (allText)
                    });
                }
            }
        };
        rawFile.send(null);
    };

    componentDidMount() {
        this.readTextFile(terms);
    }

    render() {
        return (
            <div style={{ whiteSpace: 'pre-wrap', paddingLeft: '15%', paddingRight: '15%', margin: '10px', display: 'flex', flex: 1, alignItems: 'left', textAlign: 'left' }} >
                <p>
                    <span dangerouslySetInnerHTML={{__html: this.state.termsData}} >
                    </span>
                </p>
            </div>
        );
    };

};