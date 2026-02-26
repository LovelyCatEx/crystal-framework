import {get} from "./request.ts";
import {getUserAuthentication} from "../utils/token.utils.ts";
import type {ActuatorMetricResult} from "../types/actuator.types.ts";

export function getAvailableMetricsList() {
    return get<{ names: string[] }>(
        '/api/actuator/metrics',
        {},
        { 'Authorization': 'Bearer ' + getUserAuthentication()?.token }
    );
}

export function getMetric(type: string) {
    return get<ActuatorMetricResult>(
        `/api/actuator/metrics/${type}`,
        {},
        { 'Authorization': 'Bearer ' + getUserAuthentication()?.token }
    );
}