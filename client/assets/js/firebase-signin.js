var uiConfig = {
  signInSuccessUrl: '/app/',
  signInOptions: [
    {provider: firebase.auth.EmailAuthProvider.PROVIDER_ID,
      requireDisplayName: false},
    firebase.auth.GoogleAuthProvider.PROVIDER_ID,
  ],
  tosUrl: '/tos.txt',
  privacyPolicyUrl: '/privacy.txt',
  credentialHelper: firebaseui.auth.CredentialHelper.NONE
};

var ui = new firebaseui.auth.AuthUI(firebase.auth());
ui.start('#firebaseui-auth-container', uiConfig);
