import React, { useEffect, useRef } from 'react';
import { Dropdown } from 'antd';
import type { MenuProps } from 'antd';

const isMac = /mac/i.test(navigator.userAgent);

export interface ContextMenuActionItem {
    key: string;
    label: React.ReactNode;
    icon?: React.ReactNode;
    disabled?: boolean;
    danger?: boolean;
    shortcut?: {
        mac: string;
        win: string;
        binding: string;
    };
}

export interface ContextMenuDivider {
    key: string;
    divider: true;
}

export type ContextMenuItem = ContextMenuActionItem | ContextMenuDivider;

interface ContextMenuProps {
    items: ContextMenuItem[];
    onAction: (key: string) => void;
    children: React.ReactNode;
}

function isActionItem(item: ContextMenuItem): item is ContextMenuActionItem {
    return !('divider' in item && item.divider);
}

function matchesBinding(binding: string, e: KeyboardEvent): boolean {
    const parts = binding.toLowerCase().split('+');
    const keyPart = parts.pop()!;

    const hasCtrl = parts.includes('ctrl') || (isMac && parts.includes('mod'));
    const hasAlt = parts.includes('alt') || (!isMac && parts.includes('mod'));
    const hasShift = parts.includes('shift');
    const hasMeta = parts.includes('meta');

    if (e.ctrlKey !== hasCtrl) return false;
    if (e.altKey !== hasAlt) return false;
    if (e.shiftKey !== hasShift) return false;
    if (e.metaKey !== hasMeta) return false;

    return e.key.toLowerCase() === keyPart || e.code.toLowerCase() === keyPart;
}

export function ContextMenu({ items, onAction, children }: ContextMenuProps) {
    const itemsRef = useRef(items);
    itemsRef.current = items;

    const onActionRef = useRef(onAction);
    onActionRef.current = onAction;

    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            const target = e.target as HTMLElement;
            if (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable) {
                return;
            }

            for (const item of itemsRef.current) {
                if (!isActionItem(item)) continue;
                if (item.disabled) continue;
                if (!item.shortcut) continue;

                if (matchesBinding(item.shortcut.binding, e)) {
                    e.preventDefault();
                    onActionRef.current(item.key);
                    return;
                }
            }
        };

        document.addEventListener('keydown', handleKeyDown);
        return () => document.removeEventListener('keydown', handleKeyDown);
    }, []);

    const menuItems: MenuProps['items'] = items.map(item => {
        if ('divider' in item && item.divider) {
            return { type: 'divider' as const, key: item.key };
        }

        const actionItem = item as ContextMenuActionItem;
        return {
            key: actionItem.key,
            label: actionItem.shortcut ? (
                <div className="flex items-center justify-between gap-8 w-full">
                    <span>{actionItem.label}</span>
                    <span className="text-gray-400 text-xs whitespace-nowrap">
                        {isMac ? actionItem.shortcut.mac : actionItem.shortcut.win}
                    </span>
                </div>
            ) : actionItem.label,
            icon: actionItem.icon,
            disabled: actionItem.disabled,
            danger: actionItem.danger,
        };
    });

    return (
        <Dropdown
            trigger={['contextMenu']}
            menu={{
                items: menuItems,
                onClick: ({ key }) => onAction(key),
            }}
        >
            {children}
        </Dropdown>
    );
}
