import type {ApiResponse} from "../api/system-request.ts";
import React, {useEffect, useState} from "react";
import {message} from "antd";
import useSWR, {type KeyedMutator} from "swr";

export function useSWRComposition<T>(
    key: string | undefined,
    func: () => Promise<T>,
    onError?: (error: Error) => void
) {
    const { data, isLoading, error, mutate } = useSWR(key, async () => func());

    useEffect(() => {
        if (error) {
            if (onError) {
                onError(error);
            } else {
                void message.error("无法获取数据");
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
    onError?: (error: Error) => void
): [T | null, React.Dispatch<React.SetStateAction<T | null>>, boolean, KeyedMutator<ApiResponse<T>>] {
    const [value, setValue] = useState<T | null>(null);

    const { data, isLoading, mutate } = useSWRComposition<ApiResponse<T>>(key, func, onError);
    useEffect(() => {
        if (data?.data) {
            setValue(data.data);
        }
    }, [data]);

    return [value, setValue, isLoading, mutate];
}