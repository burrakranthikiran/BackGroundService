import { WebPlugin } from '@capacitor/core';
import type { BackgroundServicePlugin } from './definitions';
export declare class BackgroundServiceWeb extends WebPlugin implements BackgroundServicePlugin {
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
    start(): Promise<void>;
    stop(): Promise<void>;
    checkLocationPermission(): Promise<{
        value: string;
    }>;
}
