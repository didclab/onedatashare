
/*accountInformation
[credentials]
credentials:{key: validation}
*/

/*
queue
[{endpoint1: string, endpoint2: string, speed: number}]
*/

import { LOGIN, LOGOUT, PROMOTE, ENDPOINT_PROGRESS, ENDPOINT_UPDATE } from './actions';
import { transferOptimizations } from "./actions";

export const cookies = require("js-cookie");

export const beforeLogin = 0;
export const duringLogin = 1;
export const afterLogin = 2;
const initialState = {
	login: cookies.get('email') ? true : false,
	admin: false,
	email: cookies.get('email') || "noemail" ,
    publicKey: cookies.get('publicKey') || null ,
	hash: cookies.get('hash') || null,

	endpoint1: cookies.get('endpoint1') ? JSON.parse(cookies.get('endpoint1')) : {
		login: false,
		credential: {},
		uri: "",
        side: "left"
	},

	endpoint2: cookies.get('endpoint2') ? JSON.parse(cookies.get('endpoint2')) : {
    login: false,
		credential: {},
		uri: "",
        side: "right"
	},

	queue: [],
	transferOptions : {
		useTransferOptimization : transferOptimizations.None,
		overwriteExistingFiles : true,
		verifyFileInterity : false,
		encryptDataChannel : false,
		compressDataChannel : false
	}
}



export function onedatashareModel(state = initialState, action) {
  // For now, don't handle any actions
  // and just return the state given to us.
  switch (action.type) {
    case LOGIN:
    	console.log('logging in')
   		const {email, hash, publicKey} = action.credential;
      console.log(email);
    	cookies.set('email', email, {maxAge: 7200});
		  cookies.set('hash', hash, {maxAge: 7200});
      cookies.set('publicKey', publicKey, {maxAge: 7200});
    	return Object.assign({}, state, {
    		login: true,
    		email: email,
    		hash: hash,
        publicKey: publicKey
    	});
    case LOGOUT:
      console.log("logging out");
      cookies.remove('email');
      cookies.remove('hash');
      cookies.remove('admin');
      cookies.remove('endpoint1');
      cookies.remove('endpoint2');
      cookies.remove("publicKey");
      window.location.replace('/');

      return Object.assign({}, state, {
        login: false,
        admin: false,
        hash: "",
        email: "noemail"
      });
    case PROMOTE:
      return Object.assign({}, state, {
        admin: true,
      });
    case ENDPOINT_PROGRESS:
      if(action.side == "left")
        return Object.assign({}, state, {
          endpoint1: {...state.endpoint1, loginProgress: action.progress},
        });
      else
        return Object.assign({}, state, {
          endpoint2: {...state.endpoint2, loginProgress: action.progress},
        });
    case ENDPOINT_UPDATE:
      if(action.side == "left"){
        console.log(JSON.stringify({...state.endpoint1, ...action.endpoint}));
        cookies.set('endpoint1', JSON.stringify({...state.endpoint1, ...action.endpoint}), {maxAge: 7200});
          return Object.assign({}, state, {
            endpoint1: {...state.endpoint1, ...action.endpoint},
          });
        }
      else{
        cookies.set('endpoint2', JSON.stringify({...state.endpoint2, ...action.endpoint}), {maxAge: 7200});
        return Object.assign({}, state, {
          endpoint2: {...state.endpoint2, ...action.endpoint},
        });
      }
    default:
      return state
  }
}