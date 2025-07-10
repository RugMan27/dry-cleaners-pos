import styles from './QuickInfoCard.module.css';

export default function QuickInfoCard({ title, value, icon }) {
    return (
        <div className={styles.card}>
            <div className={styles.icon}>{icon}</div>
            <div className={styles.info}>
                <h4 className={styles.title}>{title}</h4>
                <p className={styles.value}>{value}</p>
            </div>
        </div>
    );
}
