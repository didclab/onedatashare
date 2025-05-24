import React from 'react';
import OneDataShare from "../../assets/images/ODS.png";

const WhoSection = () => {
    return (
        <div className='who_section'>
            <div className='who_container'>
                <h2>Transfer Files</h2>
                <img src={OneDataShare}></img>
                <p>OneDataShare is a free cloud-hosted service that optimizes data transfers, 
                enables interoperability between different transfer protocols, and predicts data transfer times. 
                </p>
            </div>
        </div>
    );
};

export default WhoSection;