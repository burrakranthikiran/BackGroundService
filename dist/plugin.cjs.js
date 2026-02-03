'use strict';

var core = require('@capacitor/core');

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
}

var web = /*#__PURE__*/Object.freeze({
    __proto__: null,
    BackgroundServiceWeb: BackgroundServiceWeb
});

exports.BackgroundService = BackgroundService;
//# sourceMappingURL=plugin.cjs.js.map
