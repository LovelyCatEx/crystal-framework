/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Spin, Tag} from "antd";
import {useEffect, useState} from "react";
import {CurrencyManagerController} from "@/api/economy/currency.api.ts";
import type {CurrencyEntity} from "@/types/economy/economy.types.ts";
import {formatCurrencyUnits} from "@/utils/currency.utils.ts";

export function CurrencyAmountDisplay({currencyId, units}: { currencyId: string; units: string }) {
    const [currency, setCurrency] = useState<CurrencyEntity | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let mounted = true;
        setLoading(true);
        CurrencyManagerController.getById(currencyId)
            .then((c) => {
                if (mounted) {
                    setCurrency(c);
                    setLoading(false);
                }
            })
            .catch(() => {
                if (mounted) setLoading(false);
            });
        return () => {
            mounted = false;
        };
    }, [currencyId]);

    if (loading) return <Spin size="small" />;
    if (!currency) return <Tag color="red">Unknown</Tag>;

    return <span>{formatCurrencyUnits(units, currency)}</span>;
}
