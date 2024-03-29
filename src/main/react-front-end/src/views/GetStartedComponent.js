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
import { Carousel } from 'react-bootstrap'
import { isSafari } from 'react-device-detect';
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';

import './GetStartedComponent.css';

import { ODS_S3_BUCKET, gs1, gs2, gs3, gs4, gs5, gs6, gs7 } from "../constants"

export default class GetStartedComponent extends Component{

    constructor(){
        super();

        let imgStyle = { maxWidth : '90%', maxHeight: '70vh',  border: '1px black solid', marginTop: '2vw' }

        this.data = [
            {
                title : "Start by creating your OneDataShare account",
                imgPath: gs1,
                imgStyle : { ...imgStyle}
            },
            {
                title : "Sign in using your account credentials",
                imgPath: gs2,
                imgStyle : { ...imgStyle }
            },
            {
                title : "Browse through the list of supported endpoints",
                imgPath: gs3,
                imgStyle : { ...imgStyle, width : '80%'}
            },
            {
                title : "Log in to the required endpoints",
                imgPath: gs4,
                imgStyle : { ...imgStyle, width : '80%'}
            },
            {
                title : "Intiate a transfer by dragging a file/folder from one endpoint to another",
                imgPath: gs5,
                imgStyle : { ...imgStyle}
            },
            {
                title : "Monitor your transfer on the queue page",
                imgPath: gs6,
                imgStyle : { ...imgStyle, marginTop: '8vw'}
            },
            {
                title : "Review details related to your file/folder transfer",
                imgPath: gs7,
                imgStyle : { ...imgStyle, width : '80%'}
            }
        ];
    }

    render(){

        let carouselItemStyle = { width : '100%', textAlign : 'center'}
        var carouselItems = [];
        this.data.forEach(item =>{
            carouselItems.push(
                <Carousel.Item style={ carouselItemStyle }>
                    <div style={{ fontSize: '2em', color: 'white' }}>
                        {item.title}
                    </div>
                    <div>
                        <img style={ item.imgStyle } src={ item.imgPath } alt="Get Started" />
                    </div>
                </Carousel.Item>
            );
        })

        let bgTexture = ODS_S3_BUCKET + 'background-texture.webp';
        if(isSafari){
            // Since Safari does not support WebP images, we load PNG
            bgTexture = ODS_S3_BUCKET + 'background-texture.png';
        }

        return(
            <div className="adjustTop adjustContent content" 
                style={{ backgroundImage: 'url(' + bgTexture + ')'}}>
                <Carousel className="carouselStyle">
                    {carouselItems}

                    <Carousel.Item style={ carouselItemStyle }>
                        <div style={{ fontSize: '2em', color: 'white', margin: '10%' }}>
                            Like it already?
                            <br/>
                            Let's start by creating your free OneDataShare account now!
                            <br/> <br/>
                            <Button variant="contained" color="primary" style={{ backgroundColor : 'rgb(45, 46, 48)', fontSize: '1.5rem'}}>
                                <Link to="/account/register" style={{ color:'white' }}>
                                    Register
                                </Link>
                            </Button>
                        </div>
                    </Carousel.Item>
                </Carousel>
            </div>
        )
    }
}
