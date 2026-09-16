/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {useSWRComposition} from "@/compositions/use-swr.ts";
import type {CurrencyEntity} from "@/types/economy/economy.types.ts";
import {CurrencyManagerController} from "@/api/economy/currency.api.ts";

export const useCurrency = (currencyId?: string | null) => {
    const {data: currency, isLoading} = useSWRComposition<CurrencyEntity | null>(
        currencyId ? ['currency', currencyId] : undefined,
        () => CurrencyManagerController.getById(currencyId!),
        // Currency is a shared config; failures are surfaced per-component as an "Unknown" tag.
        () => undefined
    );

    return {currency, isLoading};
};
