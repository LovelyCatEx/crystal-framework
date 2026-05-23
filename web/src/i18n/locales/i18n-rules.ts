export interface I18nRules {
    pages: {
        [key: string]: object;
    },
    components: {
        columns: { [key: string]: object };
        popCard: { [key: string]: object };
        [key: string]: object;
    },
    enums: {
        [key: string]: string | ({ [key: number | string]: string });
    },
    entityNames: {
        [key: string]: string;
    },
    menu: {
        pub: { [key: string]: string; };
        myTenant: { [key: string]: string; };
        admin: { [key: string]: string; };
        groups: {
            [key: string]: string;
        };
    },
    api: {
        [key: string]: string | object;
    }
}