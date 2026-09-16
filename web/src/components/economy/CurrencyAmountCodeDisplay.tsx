/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Spin, Tag} from "antd";
import {useCurrency} from "@/compositions/use-currency.ts";
import {formatCurrencyAmount} from "@/utils/currency.utils.ts";

export function CurrencyAmountCodeDisplay({currencyId, amount}: {
    currencyId: string;
    amount: string | number | null | undefined;
}) {
    const {currency, isLoading} = useCurrency(currencyId);

    if (isLoading) return <Spin size="small" />;
    if (!currency) return <Tag color="red">Unknown</Tag>;

    return (
        <span className="whitespace-nowrap font-mono text-xs">
            {formatCurrencyAmount(amount ?? 0, currency)} {currency.code}
        </span>
    );
}
