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


export const LOGIN = 'LOGIN';
export function loginAction(email, token, remember, saveOAuthTokens, compactViewEnabled, admin, expiresIn) {
  return {
    type: LOGIN,
    credential: {
    	email: email,
      token: token,
      saveOAuthTokens: saveOAuthTokens,
      compactViewEnabled: compactViewEnabled,
      admin: admin,
      expiresIn: expiresIn
    }
  }
}

export const REGISTER = 'REGISTER';
export function register(email, username, password) {
  return {
    type: REGISTER,
    credential: {
    	email: email,
    	username: username,
    	password: password
    }
  }
}

export const COMPACT_VIEW_PREFERENCE = 'COMPACT_VIEW_PREFERENCE';
export function compactViewPreference(compactViewEnabled) {
  return {
    type: COMPACT_VIEW_PREFERENCE,
    compactViewEnabled: compactViewEnabled
  }
}


export const PROMOTE = 'PROMOTE';
export function isAdminAction() {
  return {
    type: PROMOTE
  }
}

export const LOGOUT = 'LOGOUT';
export function logoutAction() {
  return {
    type: LOGOUT
  }
}

export const SETTING_CHANGED = 'SETTING_CHANGED';
export function settingsChanged(settings) {
  return {
    type: SETTING_CHANGED,
    settings: settings
  }
}

export const ENDPOINT_PROGRESS = 'ENDPOINT_PROGRESS';
export function endpointProgress(progress, side) {
  return {
    type: ENDPOINT_PROGRESS,
    side: side,
    progress: progress
  }
}

export const ENDPOINT_LOGIN = 'ENDPOINT_LOGIN';
export function endpointLogin(loginType, side, credential) {
  return {
    type: ENDPOINT_LOGIN,
    side: side,
    loginType: loginType,
    credential: credential
  }
}

export const ENDPOINT_TRANSFER = 'ENDPOINT_TRANSFER';
export function endpointTransfer(sourceSide) {
  return {
    type: ENDPOINT_TRANSFER,
    sourceSide,
  }
}

export const ENDPOINT_UPDATE = 'ENDPOINT_UPDATE';
export function endpointUpdate(side, endpoint) {
  return {
    type: ENDPOINT_UPDATE,
    side: side,
    endpoint: endpoint
  }
}

export const ENDPOINT_SELECT = 'ENDPOINT_SELECT';
export function endpointSelect(side, file) {
  return {
    type: ENDPOINT_SELECT,
    side,
    file: file
  }
}

export const ENDPOINT_UNSELECT = 'ENDPOINT_UNSELECT';
export function endpointUnselect(side, file) {
  return {
    type: ENDPOINT_UNSELECT,
    side,
    file: file
  }
}


export const TRANSFER = 'TRANSFER';
export function transfer(email, username, password) {
  return {
    type: SETTING_CHANGED,
    credential: {
    	email: email,
    	username: username,
    	password: password
    }
  }
}

export const ACCOUNT_PREFERENCE_TOGGLED = 'ACCOUNT_PREFERENCE_TOGGLED';
export function accountPreferenceToggledAction(newState) {
  return {
    type: ACCOUNT_PREFERENCE_TOGGLED,
    saveOAuthTokens : newState,
  }
}

export const transferOptimizations = {
  None : "NONE",
  SecondOrder : "2ndOrder",
  PCP : "PCP"
}

export const PROGRESS_FILE_UPDATE = 'PROGRESS_FILE_UPDATE';
export function progressFilesUpdate(progressFileUpdate) {
  return {
    type: PROGRESS_FILE_UPDATE,
    progressUpdate : progressFileUpdate,
  }
}