import React from 'react';
import FreeIcon from "../../assets/images/free.svg";
import FastIcon from "../../assets/images/fast.svg";
import SecureIcon from "../../assets/images/secure.svg";
import EasyIcon from "../../assets/images/ui.png";
import EcofriendlyIcon from "../../assets/images/ecofriendly.svg";
import InteroperableIcon from "../../assets/images/interoperable.png";

const WhySection = () => {
    return (
        <div className='description_section'>
            <h1 id="whyods">Why ODS?</h1>
            <div className='description_card_container'>
                <div id="fast" className='description_card'>
                    <h1>Fast</h1>
                    <img src={FastIcon} className='description_icon' alt="Fast Icon"/>
                    <p>OneDataShare delivers your data in the fastest way possible via its state-of-the-art optimization mechanisms.</p>
                </div>
                <div id="secure" className='description_card'>
                    <h1>Secure</h1>
                    <img src={SecureIcon} className='description_icon' alt="Secure Icon"/>
                    <p>User privacy is of utmost importance for us. OneDataShare encrypts and protects your user credentials.</p>
                </div>
                <div id="easy" className='description_card'>
                    <h1>Easy</h1>
                    <img src={EasyIcon} className='description_icon' alt="Easy Icon"/>
                    <p>Our intuitive web interface makes file transfer and monitoring very easy from any device and location.</p>
                </div>
                {/* <div id="interoperable" className='description_card'>
                    <h1>Interoperable</h1>
                    <img src={InteroperableIcon} className='description_icon' alt="Interoperable Icon"/>
                    <p>We provide support for most popular cloud storage providers and data transfer end-points.</p>
                </div> */}
                <div id="free" className='description_card'>
                    <h1>Free</h1>
                    <img src={FreeIcon} className='description_icon' alt="Free Icon"/>
                    <p>OneDataShare is a free service to the community. You don’t need to pay to transfer your data!</p>
                </div>
                <div id="ecofriendly" className='description_card'>
                    <h1>Ecofriendly</h1>
                    <img src={EcofriendlyIcon} className='description_icon' alt="Free Icon" style={{"width": "150px"}}/>
                    <p>OneDataShare is committed to sustainability, leveraging optimized networking to actively reduce its carbon footprint.</p>
                </div>
            </div>
        </div>
    );
};

export default WhySection;