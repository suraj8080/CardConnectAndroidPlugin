var exec = require('cordova/exec');

exports.initlisePayment = function (arg0, success, error) {
    exec(success, error, 'cardconnectplugin', 'actionInitlisePayment', [arg0]);
};
