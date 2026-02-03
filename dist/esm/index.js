import { registerPlugin } from '@capacitor/core';
const BackgroundService = registerPlugin('BackgroundService', {
    web: () => import('./web').then((m) => new m.BackgroundServiceWeb()),
});
export * from './definitions';
export { BackgroundService };
//# sourceMappingURL=index.js.map