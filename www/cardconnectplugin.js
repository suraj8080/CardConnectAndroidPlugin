cordova.define("cordova-plugin-cardconnectplugin.cardconnectplugin", function(require, exports, module) {
var exec = require('cordova/exec');

exports.initliseManualPayment = function (arg0, arg1, arg2, success, error) {
    exec(success, error, 'cardconnectplugin', 'actionInitliseMannualPayment', [arg0, arg1, arg2]);
};

exports.initliseCardPayment = function (arg0, arg1, arg2, success, error) {
    exec(success, error, 'cardconnectplugin', 'actionInitliseCardPayment', [arg0, arg1, arg2]);
};

exports.closePaymentView = function (arg0, success, error) {
    exec(success, error, 'cardconnectplugin', 'actionClosePaymentView', [arg0]);
};

});

