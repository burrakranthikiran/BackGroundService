var capacitorBackgroundService = (function (exports, core) {
    'use strict';

    const BackgroundService = core.registerPlugin('BackgroundService', {
        web: () => Promise.resolve().then(function () { return web; }).then((m) => new m.BackgroundServiceWeb()),
    });

    class BackgroundServiceWeb extends core.WebPlugin {
        async echo(options) {
            console.log('[Web] echo:', options.value);
            return { value: options.value };
        }
        async start() {
            console.warn('[Web] Background services are not supported in browser');
        }
        async stop() {
            console.warn('[Web] Background services are not supported in browser');
        }
        async checkLocationPermission() {
            console.warn('[Web] Background services are not supported in browser');
            return { value: 'granted' };
        }
        async requestLocationPermission() {
            console.warn('[Web] Background services are not supported in browser');
            return { value: 'granted' };
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
