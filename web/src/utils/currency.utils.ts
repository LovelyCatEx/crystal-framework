/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {CurrencySymbolPosition, type CurrencyEntity} from "@/types/economy/economy.types.ts";

/**
 * Formats a raw balance unit (an integer stored at `10^-(precision + 2)` of the currency) into a
 * human-readable amount, applying the currency's symbol position, decimal separator and thousands
 * separator.
 *
 * The storage keeps 2 extra decimal places beyond `precision` as rounding headroom; display
 * truncates (never rounds) those headroom digits. When the amount is non-zero but still too small
 * to show at display precision (integer part 0 and all precision digits 0), the full magnitude is
 * shown instead of collapsing to "0.00".
 */
export function formatCurrencyUnits(units: string, currency: CurrencyEntity): string {
    const { precision, symbol, symbolPosition, decimalSeparator, thousandsSeparator } = currency;
    const value = Number(units);
    const negative = value < 0;
    const abs = Math.abs(value);
    const sign = negative ? '-' : '';

    // Split into integer + decimal digits (without separators).
    let intDigits: string;
    let decDigits: string;
    if (precision > 0 && abs > 0 && abs < 100) {
        intDigits = '0';
        const headroom = String(abs).padStart(2, '0').replace(/0+$/, '');
        decDigits = '0'.repeat(precision) + headroom;
    } else {
        const truncated = Math.trunc(abs / 100);
        const divisor = Math.pow(10, precision);
        intDigits = String(Math.floor(truncated / divisor));
        decDigits = precision === 0 ? '' : String(truncated % divisor).padStart(precision, '0');
    }

    // Group the integer digits with the thousands separator.
    const groupedInt = intDigits.replace(/\B(?=(\d{3})+(?!\d))/g, thousandsSeparator);

    // Assemble the magnitude; the symbol may replace the decimal separator.
    let magnitude: string;
    if (precision === 0) {
        magnitude = groupedInt;
    } else if (symbolPosition === CurrencySymbolPosition.REPLACE_DECIMAL) {
        magnitude = `${groupedInt}${symbol}${decDigits}`;
    } else {
        magnitude = `${groupedInt}${decimalSeparator}${decDigits}`;
    }

    if (symbolPosition === CurrencySymbolPosition.PREFIX) {
        return `${sign}${symbol}${magnitude}`;
    }
    if (symbolPosition === CurrencySymbolPosition.SUFFIX) {
        return `${sign}${magnitude}${symbol}`;
    }
    return `${sign}${magnitude}`;
}
