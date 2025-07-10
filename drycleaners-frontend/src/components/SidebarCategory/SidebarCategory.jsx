import styles from './SidebarCategory.module.css'; // Optional for styling

export default function SidebarCategory({ title, children }) {
    return (
        <div className={styles.category}>
            <div className={styles.title}>{title}</div>
            <div className={styles.links}>
                {children}
            </div>
        </div>
    );
}
