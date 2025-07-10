// components/EmployeeCard/EmployeeCard.jsx
import styles from './EmployeeCard.module.css';
import { FaPlus } from "react-icons/fa";
import { Link } from "react-router-dom";

export default function EmployeeCard({ employee }) {
    return (
        <div className={styles.card}>
            <img
                src={
                    employee.photo_url
                        ? `${employee.photo_url}?t=${employee.updated_at || Date.now()}`
                        : '/assets/no-pfp.png'
                }
                alt="Employee"
                className={employee.enabled ? styles.enabled : styles.disabled}
            />

            <div className={`${styles.details} ${styles.name}`}>
                <h3>{employee.first_name} {employee.last_name}</h3>
            </div>

            <div className={`${styles.details} ${styles.type}`}>
                <h3>Role</h3>
                <p>{employee.employee_type.replace(/_/g, ' ').toLowerCase()}</p>
            </div>

            <div className={`${styles.details} ${styles.username}`}>
                <h3>Username</h3>
                <p>{employee.username}</p>
            </div>

            <div className={`${styles.details} ${styles.id}`}>
                <h3>ID</h3>
                <p>{employee.id}</p>
            </div>

            <div className={`${styles.details} ${styles.email}`}>
                <h3>Email</h3>
                <p>{employee.email}</p>
            </div>

            <div className={`${styles.details} ${styles.phone}`}>
                <h3>Phone</h3>
                <p>{employee.phone}</p>
            </div>

            <div className={`${styles.details} ${styles.notes}`}>
                <h3>Notes</h3>
                <p>{employee.extra_data.notes || "No Notes"}</p>
            </div>

            <Link to={`edit?eid=${employee.id}`} className={styles.moreButton}>
                <FaPlus />
            </Link>
        </div>
    );
}
