import { createPortal } from 'react-dom';
import styles from './Modal.module.css';

export default function Modal({ isOpen, onClose, title, children }) {
    if (!isOpen) return null;

    function handleOverlayClick(e) {
        if (e.target === e.currentTarget) {
            onClose();
        }
    }

    return createPortal(
        <div className={styles.overlay} onClick={handleOverlayClick}>
            <div className={styles.modal}>
                {title && <h2 className={styles.title}>{title}</h2>}
                <div className={styles.body}>{children}</div>
            </div>
        </div>,
        document.getElementById('modal-root') // where to mount the modal
    );
}
