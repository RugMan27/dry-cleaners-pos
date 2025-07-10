import styles from './ScheduleEventModal.module.css';

export default function ScheduleEventModal({ event, onClose, isOpen }) {
    // Always render, control visibility by class
    return (
        <div
            className={`${styles.overlay} ${isOpen ? styles.active : ''}`}
            onClick={onClose}
            aria-hidden={!isOpen}
        >
            <div
                className={styles.modal}
                onClick={e => e.stopPropagation()}
                role="dialog"
                aria-modal="true"
            >
                {event ? (
                    <>
                        <h2>Edit Event</h2>
                        <p><strong>Title:</strong> {event.title}</p>
                        <p><strong>Start:</strong> {event.start}</p>
                        <p><strong>End:</strong> {event.end}</p>
                        <p><strong>EID:</strong> {event.eid}</p>
                    </>
                ) : (
                    <p>No event selected.</p>
                )}

                <button onClick={onClose}>Close</button>
            </div>
        </div>
    );
}
