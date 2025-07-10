import { useLocation, Link } from 'react-router-dom';
import styles from './SidebarLink.module.css';

const SidebarLink = ({ to, children, icon }) => {
    const location = useLocation();
    const isActive = location.pathname === to;

    return (
        <Link
            to={to}
            className={`${styles.link} ${isActive ? styles.active : ''}`}
        >
            {icon && <span className={styles.icon}>{icon}</span>}
            <span className={styles.text}>{children}</span>
        </Link>
    );
};

export default SidebarLink;
