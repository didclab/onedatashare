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


export function humanReadableSpeed(size) {
    if (size < 1000) 
        return parseFloat(size.toFixed(2)) + ' bit/s';
    
    let i = 0;
    let base = 1000;
    let num = size;
    
    // Keep dividing size by 1000 until it's less than 1000
    while (num >= base) {
        num /= base;
        i++;
    }

    // Round number to nearest two digits
    num = num.toFixed(2);
    
    // Units array for return to choose from
    const units = ['bit/s', 'Kbit/s', 'Mbit/s', 'Gbit/s', 'Tbit/s', 'Pbit/s', 'Ebit/s', 'Zbit/s', 'Ybit/s'];

    return `${num} ${units[i]}`;
}