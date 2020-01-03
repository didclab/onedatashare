import React from 'react';
import * as actions from '../actions';

describe('Actions handler', () => {
  it('handles loginAction', () => {
    const username = 'admin';
    const hash = 'xdcffffsss';
    const remember = true;
    const saveOAuthTokens = true;
    const compactViewEnabled = false;
    const action = actions.loginAction(username, hash, remember, saveOAuthTokens, compactViewEnabled);
    expect(action).toEqual({
      type: actions.LOGIN,
      credential: {
        email: username,
        hash: hash,
        saveOAuthTokens: saveOAuthTokens,
        compactViewEnabled: compactViewEnabled
      }
    });
  });

  it('handles register action', () => {
    const username = 'admin';
    const email = 'abc@gmail.com';
    const password = 'dghggdh';
    const action = actions.register(email, username, password);
    expect(action).toEqual({
      type: actions.REGISTER,
      credential: {
      	email: email,
      	username: username,
      	password: password
      }
    });
  });

  it('handles updateHashAction', () => {
    const hash = 'abhbdhdg';
    const action = actions.updateHashAction(hash);
    expect(action).toEqual({
      type: actions.UPDATE_HASH,
      hash: hash
    });
  });

  it('handles compactViewPreference action', () => {
    const compactViewEnabled = true;
    const action = actions.compactViewPreference(compactViewEnabled);
    expect(action).toEqual({
      type: actions.COMPACT_VIEW_PREFERENCE,
      compactViewEnabled: compactViewEnabled
    });
  });

  it('handles isAdminAction', () => {
    const action = actions.isAdminAction();
    expect(action).toEqual({
      type: actions.PROMOTE
    });
  });

  it('handles logoutAction', () => {
    const action = actions.logoutAction();
    expect(action).toEqual({
      type: actions.LOGOUT
    });
  });

  it('handles accountPreferenceToggledAction Action', () => {
    const newState = true;
    const action = actions.accountPreferenceToggledAction(newState);
    expect(action).toEqual({
      type: actions.ACCOUNT_PREFERENCE_TOGGLED,
      saveOAuthTokens : newState,
    });
  });

  it('handles endpointProgress Action', () => {
    const progress = true;
    const side = 'left';
    const action = actions.endpointProgress(progress, side);
    expect(action).toEqual({
      type: actions.ENDPOINT_PROGRESS,
      side: side,
      progress: progress
    });
  });

  it('handles endpointLogin Action', () => {
    const credential = 'assc';
    const loginType = 'admin';
    const side = 'left';
    const action = actions.endpointLogin(loginType, side, credential);
    expect(action).toEqual({
      type: actions.ENDPOINT_LOGIN,
      side: side,
      loginType: loginType,
      credential: credential
    });
  });

  it('handles endpointTransfer Action', () => {
    const sourceSide = 'left';
    const action = actions.endpointTransfer(sourceSide);
    expect(action).toEqual({
      type: actions.ENDPOINT_TRANSFER,
      sourceSide
    });
  });

  it('handles endpointUpdate Action', () => {
    const side = 'left';
    const endpoint = 'new';
    const action = actions.endpointUpdate(side, endpoint);
    expect(action).toEqual({
      type: actions.ENDPOINT_UPDATE,
      side: side,
      endpoint: endpoint
    });
  });

  it('handles endpointSelect Action', () => {
    const side = 'left';
    const file = 'text.txt';
    const action = actions.endpointSelect(side, file);
    expect(action).toEqual({
      type: actions.ENDPOINT_SELECT,
      side,
      file
    });
  });

  it('handles endpointUnselect Action', () => {
    const side = 'left';
    const file = 'text.txt';
    const action = actions.endpointUnselect(side, file);
    expect(action).toEqual({
      type: actions.ENDPOINT_UNSELECT,
      side,
      file
    });
  });

  it('handles transfer Action', () => {
    const username = 'admin';
    const email = 'abc@gmail.com';
    const password = 'dghggdh';
    const action = actions.transfer(email, username, password);
    expect(action).toEqual({
      type: actions.SETTING_CHANGED,
      credential: {
      	email: email,
      	username: username,
      	password: password
      }
    });
  });

  it('handles settingsChanged Action', () => {
    const settings = {};
    const action = actions.settingsChanged(settings);
    expect(action).toEqual({
      type: actions.SETTING_CHANGED,
      settings: settings
    });
  });
});
