export interface BackgroundServicePlugin {
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
    requestLocationPermission(): Promise<{
        value: string;
    }>;
}
