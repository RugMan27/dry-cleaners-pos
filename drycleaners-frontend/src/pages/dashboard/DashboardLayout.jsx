import {Outlet, NavLink, useLocation} from 'react-router-dom';
import styles from './Dashboard.module.css';
import SidebarLink from "../../components/SidebarLink/SidebarLink.jsx";
import ThemeToggleButton from "../../components/ThemeToggleButton/ThemeToggleButton.jsx";
import { FaUsers, FaDollarSign, FaIdCard , FaHome, FaChartBar, FaCog, FaUserCircle, FaBell, FaSearch, FaCalendarAlt, FaUserPlus, FaRegClock, FaFileInvoice, FaDatabase, FaUserShield, FaTerminal, FaUserTie, FaUserCheck    } from 'react-icons/fa';
import {useEffect, useState} from "react";
import {logout} from "../../utils/auth.js";
import { useTheme } from '../../hooks/useTheme';
import {apiClient } from "../../utils/apiClient.js";
import YesNoModal from "../../components/YesNoModal/YesNoModal.jsx";
import SidebarCategory from "../../components/SidebarCategory/SidebarCategory.jsx";


export default function DashboardLayout() {


    const location = useLocation();
    const [pageTitle, setPageTitle] = useState('');
    const { theme, toggleTheme } = useTheme();
    const [sidebarOpen, setSidebarOpen] = useState(false);

    const [selfEmployee, setSelfEmployee] = useState(null);

    const [showLogoutModal, setShowLogoutModal] = useState(false);

    useEffect(() => {

        async function fetchEmployeeDetails() {

            const employeeEndpoint = `/employees/me`;
            const employeeData = await apiClient.get(employeeEndpoint);
            console.log(employeeData);
            setSelfEmployee(employeeData)
        }

        fetchEmployeeDetails()


    }, [location]);

    // User dropdown toggle state example
    const [userMenuOpen, setUserMenuOpen] = useState(false);
    const toggleUserMenu = () => setUserMenuOpen(!userMenuOpen);

    function formatEmployeeType(type) {
        if (!type) return '';
        // lowercase, split on underscores, capitalize first letter of each word, join with spaces
        return type
            .toLowerCase()
            .split('_')
            .map(word => word.charAt(0).toUpperCase() + word.slice(1))
            .join(' ');
    }
    return (
        <div className={styles.dashboardLayout}>
            <YesNoModal
                isOpen={showLogoutModal}
                title="Are you sure you want to logout?"
                onCancel={() => setShowLogoutModal(false)}
                onConfirm={() => {
                    logout();
                    setShowLogoutModal(false);
                }}/>
            <header className={styles.header}>
                <h2>{pageTitle}</h2>

                <div className={styles.headerLeft}>
                    <button onClick={() => setSidebarOpen(true)} className={styles.menuButton}>☰</button>
                </div>

                <div className={styles.headerRight}>


                    {/* Search Bar */}
                    <div className={styles.searchBar}>
                        <FaSearch />
                        <input type="text" placeholder="Search..." />
                    </div>

                    {/* Notifications Icon */}
                    <button className={styles.iconButton} aria-label="Notifications">
                        <FaBell />
                    </button>

                    {/* User Dropdown */}
                    <div className={styles.userMenuWrapper}>
                        <button
                            className={styles.iconButton}
                            aria-label="User menu"
                            onClick={toggleUserMenu}
                        >
                            <FaUserCircle />
                        </button>

                        {userMenuOpen && (
                            <div className={styles.userDropdown}>
                                <button onClick={toggleTheme}>
                                    {theme === 'light' ? '🌙 Dark Mode' : '☀️ Light Mode'}
                                </button>
                                <button onClick={() => setShowLogoutModal(true)}>
                                    Logout
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </header>

            <aside className={`${styles.sidebar} ${sidebarOpen ? styles.open : styles.closed}`}>
                <button onClick={() => setSidebarOpen(false)} className={styles.closeButton}>✖</button>
                {selfEmployee ? (
                    <img src={selfEmployee.photo_url || '../../assets/no-pfp.png'} alt="Profile Picture" width="100px" height="100px" />
                ) : (
                    <img src="../../assets/no-pfp.png" alt="Loading..." width="100px" height="100px" />
                )}
                {selfEmployee ? (
                    <>
                        <h3>{`${selfEmployee.first_name} ${selfEmployee.last_name}`}</h3>
                        <h4>{formatEmployeeType(selfEmployee.employee_type)}</h4>
                    </>
                ) : (
                    <>
                        {/* Optional: fallback content while loading */}
                        <h3></h3>
                        <h4>Loading...</h4>

                    </>
                )}
                <nav>
                    <SidebarLink to="/dashboard" icon={<FaHome />}>Home</SidebarLink>
                    <SidebarCategory title="Reports">
                        <SidebarLink to="/dashboard/reports" icon={<FaChartBar />}>Overview</SidebarLink>
                        <SidebarLink to="/dashboard/reports/sales" icon={<FaDollarSign />}>Sales Report</SidebarLink>
                        <SidebarLink to="/dashboard/reports/employees" icon={<FaUserTie />}>Employee Report</SidebarLink>
                        <SidebarLink to="/dashboard/reports/customers" icon={<FaUserCheck />}>Customer Report</SidebarLink>
                    </SidebarCategory>
                    <SidebarCategory title="Customers">
                        <SidebarLink to="/dashboard/customers" icon={<FaUsers />}>All Customers</SidebarLink>
                        <SidebarLink to="/dashboard/customers/new" icon={<FaUserPlus />}>Add Customer</SidebarLink>
                    </SidebarCategory>

                    <SidebarCategory title="Employees">
                        <SidebarLink to="/dashboard/employees" icon={<FaIdCard />}>Management</SidebarLink>
                        <SidebarLink to="/dashboard/employees/schedule" icon={<FaCalendarAlt />}>Scheduling</SidebarLink>
                        <SidebarLink to="/dashboard/employees/shifts" icon={<FaRegClock />}>Shift Overview</SidebarLink>
                    </SidebarCategory>
                    <SidebarCategory title="Admin Tools">
                        <SidebarLink to="/dashboard/dev/logs" icon={<FaTerminal />}>Logs</SidebarLink>
                        <SidebarLink to="/dashboard/dev/db" icon={<FaDatabase />}>Database</SidebarLink>
                    </SidebarCategory>
                    <SidebarCategory title="Settings">
                        <SidebarLink to="/dashboard/settings" icon={<FaCog />}>General</SidebarLink>
                        <SidebarLink to="/dashboard/settings/invoice" icon={<FaFileInvoice />}>Invoice Layout</SidebarLink>
                        <SidebarLink to="/dashboard/settings/notifications" icon={<FaBell />}>Notifications</SidebarLink>
                        <SidebarLink to="/dashboard/settings/users" icon={<FaUserShield />}>User Roles</SidebarLink>
                    </SidebarCategory>
                </nav>
            </aside>
            {/* Sidebar Overlay (click to close) */}
            {sidebarOpen && (
                <div className={styles.sidebarOverlay} onClick={() => setSidebarOpen(false)} />
            )}

            <main className={styles.mainContent}>
                <Outlet /> {/* This is where nested pages will show */}
            </main>
        </div>
    );
}
