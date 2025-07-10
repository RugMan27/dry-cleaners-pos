import styles from './QuickInfoSection.module.css';

export default function QuickInfoSection({ children }) {
    return <div className={styles.cardGrid}>{children}</div>;
}
