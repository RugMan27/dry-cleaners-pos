import React from 'react';
import ReactDOM from 'react-dom';
import styles from './YesNoModal.module.css';

const Modal = ({ isOpen, title, children, onCancel, onConfirm }) => {
    if (!isOpen) return null;

    return ReactDOM.createPortal(
        <div className={styles.overlay} onClick={onCancel}>
            <div className={styles.modal} onClick={e => e.stopPropagation()}>
                {title && <h3 className={styles.title}>{title}</h3>}
                <div className={styles.body}>{children}</div>
                <div className={styles.footer}>
                    <button className={styles.button} onClick={onCancel}>No</button>
                    <button className={styles.button} onClick={onConfirm}>Yes</button>
                </div>
            </div>
        </div>,
        document.body
    );
};

export default Modal;
