/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Spin, Tag} from "antd";
import {useCurrency} from "@/compositions/use-currency.ts";

export function CurrencyCodeDisplay({currencyId}: { currencyId: string }) {
    const {currency, isLoading} = useCurrency(currencyId);

    if (isLoading) return <Spin size="small" />;
    if (!currency) return <Tag color="red">Unknown</Tag>;

    return (
        <span className="inline-flex items-center gap-1">
            <span className="font-mono text-xs">{currency.symbol}</span>
            <span className="text-xs text-gray-500">{currency.code}</span>
        </span>
    );
}
