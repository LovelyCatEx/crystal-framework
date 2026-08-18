import {mutate as globalMutate} from "swr";

export const MESSAGE_SWR_KEY_PREFIX = 'message:';

export function clearMessageSWRCache(): Promise<unknown> {
    return globalMutate(
        (key) => Array.isArray(key) && typeof key[0] === 'string' && key[0].startsWith(MESSAGE_SWR_KEY_PREFIX),
        undefined,
        {revalidate: false},
    );
}
