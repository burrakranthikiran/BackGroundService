var capacitorBackgroundService = (function (exports, core) {
    'use strict';

    const BackgroundService = core.registerPlugin('BackgroundService', {
        web: () => Promise.resolve().then(function () { return web; }).then((m) => new m.BackgroundServiceWeb()),
    });

    class BackgroundServiceWeb extends core.WebPlugin {
        async echo(options) {
            console.log('ECHO', options);
            return options;
        }
    }

    var web = /*#__PURE__*/Object.freeze({
        __proto__: null,
        BackgroundServiceWeb: BackgroundServiceWeb
    });

    exports.BackgroundService = BackgroundService;

    return exports;

})({}, capacitorExports);
//# sourceMappingURL=plugin.js.map
