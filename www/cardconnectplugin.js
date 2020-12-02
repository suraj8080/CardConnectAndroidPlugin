var exec = require('cordova/exec');

exports.initlisePayment = function (arg0, arg1, arg2, success, error) {
    exec(success, error, 'cardconnectplugin', 'actionInitlisePayment', [arg0, arg1, arg2]);
};
