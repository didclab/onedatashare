export const LOGIN = 'LOGIN';
export function loginAction(username, hash, publicKey) {
  return {
    type: LOGIN,
    credential: {
    	email: username,
    	hash: hash,
      publicKey: publicKey
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



export const UPDATE_HASH = 'UPDATE_HASH';

export function updateHashAction(hash) {
  return {
    type: UPDATE_HASH,
    hash: hash
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

export const ENDPOINT_LOGOUT = 'ENDPOINT_LOGOUT';

export function endpointLogout(side) {
  return {
    type: ENDPOINT_LOGOUT,
    side,
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

export const transferOptimizations = {
  None : "NONE",
  SecondOrder : "2ndOrder",
  PCP : "PCP"
}

