import React, { Component } from 'react';

export default class PolicyComponent extends Component {
    state = {
        termsData: ''
    }

    readTextFile = () => {
        var rawFile = new XMLHttpRequest();
        rawFile.open("GET", 'https://ods-static-assets.s3.us-east-2.amazonaws.com/privacy.txt', false);
        rawFile.onreadystatechange = () => {
            if (rawFile.readyState === 4) {
                if (rawFile.status === 200 || rawFile.status === 0) {
                    var allText = rawFile.responseText;
                    this.setState({
                        termsData: allText
                    });
                }
            }
        };
        rawFile.send(null);
    };

    componentDidMount() {
        this.readTextFile();
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