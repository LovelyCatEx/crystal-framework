import { OAuthPlatform } from "../types/oauth-account.types";

export const PLATFORM_REGISTRATION_ID_MAP: Record<OAuthPlatform, string> = {
    [OAuthPlatform.GITHUB]: "github",
    [OAuthPlatform.GOOGLE]: "google",
    [OAuthPlatform.OICQ]: "oicq"
};