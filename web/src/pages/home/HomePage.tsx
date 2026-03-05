import {useNavigate} from "react-router-dom";
import {useEffect} from "react";
import {ProjectDisplayName} from "../../global/global-settings.ts";
import {getUserAuthentication} from "../../utils/token.utils.ts";
import {menuPathLogin} from "../../router";

export function HomePage() {
    const navigate = useNavigate();

    useEffect(() => {
        document.title = ProjectDisplayName
    }, []);

    useEffect(() => {
        const auth = getUserAuthentication();
        if (!auth || auth.expired) {
            navigate(menuPathLogin);
        }
    }, [navigate]);

    return (
        <>
        </>
    )
}