import type {ApiResponse} from "../api/system-request.ts";
import React, {useEffect, useState} from "react";
import {message} from "antd";
import useSWR, {type KeyedMutator, type SWRConfiguration} from "swr";

export function useSWRComposition<T>(
    key: string | undefined,
    func: () => Promise<T>,
    onError?: (error: Error) => void,
    options?: SWRConfiguration<T>
) {
    const { data, isLoading, error, mutate } = useSWR(key, async () => func(), options);

    useEffect(() => {
        if (error) {
            if (onError) {
                onError(error);
            } else {
                void message.error("Could not fetch data");
            }
        }
    }, [error]);

    return {
        data,
        isLoading,
        error,
        mutate
    }
}

export function useSWRState<T>(
    key: string | undefined,
    func: () => Promise<ApiResponse<T>>,
    onError?: (error: Error) => void,
    options?: SWRConfiguration<ApiResponse<T>>
): [T | null, React.Dispatch<React.SetStateAction<T | null>>, boolean, KeyedMutator<ApiResponse<T>>] {
    const { data, isLoading, mutate } = useSWRComposition<ApiResponse<T>>(key, func, onError, options);

    const [value, setValue] = useState<T | null>(null);
    const [syncedData, setSyncedData] = useState(data);
    if (data !== syncedData) {
        setSyncedData(data);
        if (data?.data) {
            setValue(data.data);
        }
    }

    return [value, setValue, isLoading, mutate];
}