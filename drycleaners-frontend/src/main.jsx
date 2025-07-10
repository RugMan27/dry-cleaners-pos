import React, { useEffect, useState } from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import App from './App';
import Login from './pages/login/Login.jsx';
import './index.css';
import { validateLocalToken } from './utils/auth';
import DashboardLayout from "./pages/dashboard/DashboardLayout.jsx";
import DashboardHome from "./pages/dashboard/DashboardHome.jsx";
import DashboardReports from "./pages/dashboard/DashboardReports.jsx";
import DashboardSettings from "./pages/dashboard/DashboardSettings.jsx";
import DashboardCustomers from "./pages/dashboard/DashboardSettings.jsx";
import DashboardEmployees from "./pages/dashboard/Employees/DashboardEmployees.jsx";
import EmployeeEdit from "./pages/dashboard/Employees/EmployeeEdit.jsx";
import DashboardScheduling from "./pages/dashboard/Employees/DashboardScheduling.jsx";




// eslint-disable-next-line react-refresh/only-export-components
function Root() {
    const [token, setToken] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function checkToken() {
            const validToken = await validateLocalToken();
            setToken(validToken);
            setLoading(false);
        }

        checkToken();

        // 🔔 Listen for auth changes (after login or logout)
        window.addEventListener("auth-changed", checkToken);

        // 🧹 Clean up listener on unmount
        return () => window.removeEventListener("auth-changed", checkToken);
    }, []);


    if (loading) return <div>Loading...</div>;

    return (
        <BrowserRouter>
            <Routes>
                {!token ? (
                    <>
                        <Route path="/login" element={<Login />} />
                        <Route path="*" element={<Navigate to="/login" replace />} />
                    </>
                ) : (
                    <>
                        <Route path="/dashboard" element={<DashboardLayout />}>
                            <Route index element={<DashboardHome />} />
                            <Route path="reports" element={<DashboardReports />} />
                            <Route path="customers" element={<DashboardCustomers />} />
                            <Route path="employees" element={<DashboardEmployees />} />
                            <Route path="settings" element={<DashboardSettings />} />

                            <Route path="employees/edit" element={<EmployeeEdit />} />
                            <Route path="employees/schedule" element={<DashboardScheduling />} />
                        </Route>
                        <Route path="*" element={<Navigate to="/dashboard" replace />} />
                    </>
                )}
            </Routes>
        </BrowserRouter>
    );
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<Root />);
