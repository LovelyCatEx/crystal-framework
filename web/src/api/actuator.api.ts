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

export function getMetric(
    type: string,
    tags: { tagName: string, optionName: string }[] = []
) {
    return get<ActuatorMetricResult>(
        tags.length > 0
            ? `/api/actuator/metrics/${type}?${tags.map((tag) => `tag=${tag.tagName}:${tag.optionName}`).join('&')}`
            : `/api/actuator/metrics/${type}`,
        {},
        { 'Authorization': 'Bearer ' + getUserAuthentication()?.token }
    );
}