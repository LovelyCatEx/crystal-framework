import {createContext, type ReactNode, useCallback, useContext, useEffect, useRef, useState} from "react";
import {Button, Modal} from "antd";
import {EditOutlined, ExclamationCircleFilled, LockOutlined, ReadOutlined, UnlockOutlined} from "@ant-design/icons";
import {BaseManagerController} from "@/api/BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";
import {type ApiResponse, handleApiResponse} from "@/api/system-request.ts";
import {useTranslation} from "react-i18next";

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type AnyController = BaseManagerController<any, any, any, any, any>;

interface ProtectedControllerContextValue {
    controller: AnyController;
    isReadonly: boolean;
}

const ProtectedControllerContext = createContext<ProtectedControllerContextValue | null>(null);

export function useProtectedController<
    ENTITY = unknown,
    C extends object = object,
    R extends BaseManagerReadDTO = BaseManagerReadDTO,
    U extends BaseManagerUpdateDTO = BaseManagerUpdateDTO,
    D extends BaseManagerDeleteDTO = BaseManagerDeleteDTO
>() {
    const context = useContext(ProtectedControllerContext);
    if (!context) {
        throw new Error("usePermissionController must be used within PermissionWarningWrapper");
    }
    return {
        controller: context.controller as BaseManagerController<ENTITY, C, R, U, D>,
        isReadonly: context.isReadonly
    };
}

class ProtectedControllerWrapper<
    ENTITY,
    C extends object,
    R extends BaseManagerReadDTO = BaseManagerReadDTO,
    U extends BaseManagerUpdateDTO = BaseManagerUpdateDTO,
    D extends BaseManagerDeleteDTO = BaseManagerDeleteDTO
> extends BaseManagerController<ENTITY, C, R, U, D> {
    constructor(
        private readonly parentController: BaseManagerController<ENTITY, C, R, U, D>,
        private readonly isReadonly: () => boolean,
        private readonly readonlyErrorMessage: string
    ) {
        super("");
    }

    async getById(id: string, additional: Record<string, string> = {}) {
        return this.parentController.getById(id, additional);
    }

    list(queryParams: Record<string, string> = {}) {
        return this.parentController.list(queryParams);
    }

    query(dto: R) {
        return this.parentController.query(dto);
    }

    create(dto: C): Promise<ApiResponse<unknown>> {
        if (this.isReadonly()) {
            const res = { code: 403, data: null, message: this.readonlyErrorMessage };
            handleApiResponse(res);
            return Promise.resolve(res);
        }
        return this.parentController.create(dto);
    }

    update(dto: U): Promise<ApiResponse<unknown>> {
        if (this.isReadonly()) {
            const res = { code: 403, data: null, message: this.readonlyErrorMessage };
            handleApiResponse(res);
            return Promise.resolve(res);
        }
        return this.parentController.update(dto);
    }

    delete(dto: D): Promise<ApiResponse<unknown>> {
        if (this.isReadonly()) {
            const res = { code: 403, data: null, message: this.readonlyErrorMessage };
            handleApiResponse(res);
            return Promise.resolve(res);
        }
        return this.parentController.delete(dto);
    }
}

interface ProtectedControllerWarningWrapperProps {
    children: ReactNode;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    controller: BaseManagerController<any, any, any, any, any>;
    title?: string;
    content?: string;
}

export function ProtectedControllerWarningWrapper({
    children,
    controller,
    title,
    content
}: ProtectedControllerWarningWrapperProps) {
    const {t} = useTranslation();
    const [modal, contextHolder] = Modal.useModal();
    const [isReadonly, setIsReadonly] = useState<boolean>(true);
    const [hasConfirmed, setHasConfirmed] = useState(false);
    const isReadonlyRef = useRef(true);

    useEffect(() => {
        isReadonlyRef.current = isReadonly;
    }, [isReadonly]);

    const getIsReadonly = useCallback(() => isReadonlyRef.current, []);

    const wrappedController = useCallback(() => {
        return new ProtectedControllerWrapper(
            controller,
            getIsReadonly,
            t('components.protectedController.readonlyError')
        ) as unknown as AnyController;
    }, [controller, getIsReadonly, t]);

    const [controllerInstance] = useState(() => wrappedController());

    useEffect(() => {
        if (hasConfirmed) return;

        modal.warning({
            title: title ?? t('components.protectedController.warningTitle'),
            icon: <ExclamationCircleFilled />,
            content: (
                <div className="py-2">
                    <p className="mb-4">{content ?? t('components.protectedController.warningContent')}</p>
                    <div className="flex gap-3">
                        <Button
                            onClick={() => {
                                setIsReadonly(false);
                                setHasConfirmed(true);
                                Modal.destroyAll();
                            }}
                            className="flex-1 px-4 py-2 rounded-lg flex items-center justify-center gap-2 transition-colors"
                        >
                            <EditOutlined />
                            {t('components.protectedController.editMode')}
                        </Button>
                        <Button
                            type="primary"
                            onClick={() => {
                                setIsReadonly(true);
                                setHasConfirmed(true);
                                Modal.destroyAll();
                            }}
                            className="flex-1 px-4 py-2 rounded-lg flex items-center justify-center gap-2 transition-colors"
                        >
                            <ReadOutlined />
                            {t('components.protectedController.readonlyMode')}
                        </Button>
                    </div>
                </div>
            ),
            okButtonProps: { style: { display: 'none' } },
            width: 520,
            centered: true,
            closable: false,
            maskClosable: false,
        });
    }, [hasConfirmed, title, content, modal, t]);

    const handleReopenModal = useCallback(() => {
        setHasConfirmed(false);
    }, []);

    const overlayDivRef = useRef<HTMLDivElement | null>(null);
    const [overlayWidth, setOverlayWidth] = useState<number>(0);

    useEffect(() => {
        if (!overlayDivRef.current) return;
        const resizeObserver = new ResizeObserver((entries) => {
            for (const entry of entries) {
                setOverlayWidth(entry.contentRect.width);
            }
        });
        resizeObserver.observe(overlayDivRef.current);
        return () => {
            resizeObserver.disconnect();
        };
    }, []);


    return (
        <ProtectedControllerContext.Provider value={{ controller: controllerInstance, isReadonly }}>
            {contextHolder}
            <div className="flex flex-col relative size-full" ref={overlayDivRef}>
                {children}
                <div className="flex flex-col h-[calc(100vh-64px-48px)] fixed pointer-events-none" style={{ width: overlayWidth }}>
                    <div className="absolute bottom-6 left-1/2 -translate-x-1/2 z-50 pointer-events-auto">
                        {isReadonly ? (
                            <button
                                onClick={handleReopenModal}
                                className="flex items-center gap-2 px-4 py-2 bg-green-100/80 hover:bg-green-200/80 backdrop-blur-sm text-green-800 rounded-full shadow-lg transition-colors cursor-pointer"
                            >
                                <span className="text-sm font-medium">{t('components.protectedController.readonlyBadge')}</span>
                                <LockOutlined />
                            </button>
                        ) : (
                            <button
                                onClick={handleReopenModal}
                                className="flex items-center gap-2 px-4 py-2 bg-red-100/80 hover:bg-red-200/80 backdrop-blur-sm text-red-800 rounded-full shadow-lg transition-colors cursor-pointer"
                            >
                                <span className="text-sm font-medium">{t('components.protectedController.editModeBadge')}</span>
                                <UnlockOutlined className="text-sm" />
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </ProtectedControllerContext.Provider>
    );
}
