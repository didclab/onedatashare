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
                if (rawFile.status === 200 || rawFile.status == 0) {
                    var allText = rawFile.responseText;
                    console.log("allText: ", allText);
                    this.setState({
                        termsData: allText
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
                    <span>
                        {`${this.state.termsData}`}
                    </span>
                </p>
            </div>
        );
    };

};