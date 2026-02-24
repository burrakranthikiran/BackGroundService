import { WebPlugin } from '@capacitor/core';
export class BackgroundServiceWeb extends WebPlugin {
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
//# sourceMappingURL=web.js.map