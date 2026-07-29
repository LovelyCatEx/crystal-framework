import {useTranslation} from "react-i18next";
import {EntityIdsSelector} from "../EntityIdsSelector.tsx";
import {useMessageChannelTableColumns} from "@/components/columns/MessageChannelEntityColumns.tsx";
import {MessageChannelManagerController} from "@/api/message-channel/message-channel.api.ts";
import type {MessageChannel} from "@/types/message-channel/message-channel.types.ts";
import {MessageChannelChip} from "@/components/chip/MessageChannelChip.tsx";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";

interface SystemMessageChannelIdsSelectorProps {
    value?: string[] | null;
    onChange?: (value: string[]) => void;
}

const SYSTEM_SCOPE_ID = '0';

export function SystemMessageChannelIdsSelector({ value, onChange }: SystemMessageChannelIdsSelectorProps) {
    const { t } = useTranslation();
    const columns = useMessageChannelTableColumns();

    return (
        <EntityIdsSelector<MessageChannel>
            value={value}
            onChange={onChange}
            entityName={t('entityNames.messageChannel')}
            columns={columns}
            query={async (params) => (await MessageChannelManagerController.query({
                ...params,
                scope: ResourceScope.SYSTEM,
                scopeId: SYSTEM_SCOPE_ID,
            })).data!}
            getById={(id) => MessageChannelManagerController.getById(id, {scope: ResourceScope.SYSTEM, scopeId: SYSTEM_SCOPE_ID})}
            renderItem={(channel) => (
                <MessageChannelChip channel={channel} />
            )}
        />
    );
}
