import styles from './LoadingCard.module.css';

export default function LoadingCard({ title = "Loading..." }) {
    return (
        <div className={styles.card}>
            <div className={styles.spinner}></div>
            <p className={styles.title}>{title}</p>
        </div>
    );
}
