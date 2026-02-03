import { WebPlugin } from '@capacitor/core';
import type { BackgroundServicePlugin } from './definitions';

export class BackgroundServiceWeb
  extends WebPlugin
  implements BackgroundServicePlugin {

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('[Web] echo:', options.value);
    return { value: options.value };
  }

  async start(): Promise<void> {
    console.warn('[Web] Background services are not supported in browser');
  }

  async stop(): Promise<void> {
    console.warn('[Web] Background services are not supported in browser');
  }
}
