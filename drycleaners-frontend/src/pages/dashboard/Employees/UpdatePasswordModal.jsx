import { useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { apiClient } from "../../../utils/apiClient.js";
import { FaTimes } from "react-icons/fa";

function UpdatePasswordModal({ employeeId, onClose }) {
    const [password, setPassword] = useState("");
    const [confirm, setConfirm] = useState("");
    const [error, setError] = useState(null);
    const [saving, setSaving] = useState(false);

    async function handleSubmit(e) {
        e.preventDefault();
        setError(null);

        if (password !== confirm) {
            setError("Passwords do not match");
            return;
        }

        setSaving(true);
        try {
            await apiClient.put(`/employees/${employeeId}/password`, { password });
            onClose();
        } catch {
            setError("Failed to update password");
        } finally {
            setSaving(false);
        }
    }

    return (
        <div style={modalOverlayStyle}>
            <div style={modalContentStyle}>
                <button
                    onClick={onClose}
                    style={{ position: "absolute", top: 10, right: 10, cursor: "pointer", background: "none", border: "none", fontSize: 20 }}
                    aria-label="Close"
                >
                    <FaTimes />
                </button>
                <h3>Update Password</h3>
                {error && <p style={{ color: "red" }}>{error}</p>}
                <form onSubmit={handleSubmit}>
                    <label>
                        New Password
                        <input
                            type="password"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                            required
                            minLength={6}
                            style={inputStyle}
                        />
                    </label>
                    <br />
                    <label>
                        Confirm Password
                        <input
                            type="password"
                            value={confirm}
                            onChange={e => setConfirm(e.target.value)}
                            required
                            minLength={6}
                            style={inputStyle}
                        />
                    </label>
                    <br />
                    <button type="submit" disabled={saving} style={buttonStyle}>
                        {saving ? "Saving..." : "Update Password"}
                    </button>
                    <button type="button" onClick={onClose} disabled={saving} style={{ ...buttonStyle, marginLeft: 10 }}>
                        Cancel
                    </button>
                </form>
            </div>
        </div>
    );
}

export default function EmployeeEdit() {
    const location = useLocation();
    const navigate = useNavigate();

    const params = new URLSearchParams(location.search);
    const employeeId = params.get("eid");

    const [employee, setEmployee] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);

    const [photoFile, setPhotoFile] = useState(null);
    const [photoPreview, setPhotoPreview] = useState("");
    const [showPasswordModal, setShowPasswordModal] = useState(false);

    const employeeTypes = [
        { label: "Admin", value: "ADMIN" },
        { label: "User", value: "USER" },
        { label: "Manager", value: "MANAGER" },
        // Add more as needed
    ];

    useEffect(() => {
        if (!employeeId) {
            setError("No employee ID specified");
            setLoading(false);
            return;
        }

        async function fetchEmployee() {
            try {
                setLoading(true);
                const response = await apiClient.get(`/employees/search?id=${employeeId}`);
                const emp = response.data.data;
                setEmployee(emp);
                setPhotoPreview(emp.photo_url || "");
            } catch {
                setError("Failed to load employee data");
            } finally {
                setLoading(false);
            }
        }

        fetchEmployee();
    }, [employeeId]);

    function handleChange(field, value) {
        setEmployee(prev => {
            if (field === "notes") {
                return {
                    ...prev,
                    extra_data: {
                        ...prev.extra_data,
                        notes: value,
                    },
                };
            }
            return { ...prev, [field]: value };
        });
    }

    function handlePhotoChange(e) {
        const file = e.target.files[0];
        if (!file) return;

        if (file.type !== "image/jpeg") {
            alert("Only JPG files allowed");
            return;
        }

        setPhotoFile(file);
        setPhotoPreview(URL.createObjectURL(file));
    }

    async function uploadPhoto() {
        if (!photoFile) return;

        const formData = new FormData();
        formData.append("photo", photoFile);

        try {
            await apiClient.post(`/employees/${employeeId}/photo`, formData, {
                headers: { "Content-Type": "multipart/form-data" },
            });
            alert("Photo uploaded!");
            setPhotoFile(null);
        } catch {
            alert("Failed to upload photo");
        }
    }

    async function handleSave() {
        setSaving(true);
        setError(null);

        try {
            await apiClient.put(`/employees/${employeeId}`, employee);
            navigate("/dashboard/employees");
        } catch {
            setError("Failed to save changes");
        } finally {
            setSaving(false);
        }
    }

    if (loading) return <p>Loading employee data...</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;
    if (!employee) return <p>Employee not found</p>;

    return (
        <div style={{ maxWidth: 600, margin: "auto" }}>
            <h2>Edit Employee: {employee.first_name} {employee.last_name}</h2>

            <form
                onSubmit={e => {
                    e.preventDefault();
                    handleSave();
                }}
            >
                <label>
                    First Name
                    <input
                        type="text"
                        value={employee.first_name || ""}
                        onChange={e => handleChange("first_name", e.target.value)}
                        required
                        style={inputStyle}
                    />
                </label>
                <br />

                <label>
                    Last Name
                    <input
                        type="text"
                        value={employee.last_name || ""}
                        onChange={e => handleChange("last_name", e.target.value)}
                        required
                        style={inputStyle}
                    />
                </label>
                <br />

                <label>
                    Username
                    <input
                        type="text"
                        value={employee.username || ""}
                        onChange={e => handleChange("username", e.target.value)}
                        required
                        style={inputStyle}
                    />
                </label>
                <br />

                <label>
                    Employee Type
                    <select
                        value={employee.employee_type || ""}
                        onChange={e => handleChange("employee_type", e.target.value)}
                        style={inputStyle}
                    >
                        <option value="" disabled>
                            Select type
                        </option>
                        {employeeTypes.map(type => (
                            <option key={type.value} value={type.value}>
                                {type.label}
                            </option>
                        ))}
                    </select>
                </label>
                <br />

                <label>
                    Email
                    <input
                        type="email"
                        value={employee.email || ""}
                        onChange={e => handleChange("email", e.target.value)}
                        style={inputStyle}
                    />
                </label>
                <br />

                <label>
                    Phone
                    <input
                        type="tel"
                        value={employee.phone || ""}
                        onChange={e => handleChange("phone", e.target.value)}
                        style={inputStyle}
                    />
                </label>
                <br />

                <label>
                    Notes
                    <textarea
                        value={employee.extra_data?.notes || ""}
                        onChange={e => handleChange("notes", e.target.value)}
                        style={{ ...inputStyle, height: 80 }}
                    />
                </label>
                <br />

                <label>
                    Profile Picture (400x400 JPG)
                    <input type="file" accept=".jpg" onChange={handlePhotoChange} />
                </label>
                <br />
                {photoPreview && (
                    <img
                        src={photoPreview}
                        alt="Profile Preview"
                        style={{ width: 100, height: 100, objectFit: "cover", borderRadius: "50%" }}
                    />
                )}
                <br />
                <button type="button" onClick={uploadPhoto} disabled={!photoFile} style={buttonStyle}>
                    Upload Photo
                </button>
                <br />
                <br />

                <button type="button" onClick={() => setShowPasswordModal(true)} style={buttonStyle}>
                    Change Password
                </button>

                <br />
                <br />

                <button type="submit" disabled={saving} style={buttonStyle}>
                    {saving ? "Saving..." : "Save Changes"}
                </button>
            </form>

            {showPasswordModal && (
                <UpdatePasswordModal employeeId={employeeId} onClose={() => setShowPasswordModal(false)} />
            )}
        </div>
    );
}

// Simple inline styles you can replace with CSS modules or your own styles
const inputStyle = {
    display: "block",
    width: "100%",
    padding: "0.5rem",
    marginTop: "0.25rem",
    marginBottom: "1rem",
    fontSize: "1rem",
};

const buttonStyle = {
    padding: "0.5rem 1rem",
    fontSize: "1rem",
    cursor: "pointer",
};

const modalOverlayStyle = {
    position: "fixed",
    top: 0,
    left: 0,
    width: "100vw",
    height: "100vh",
    backgroundColor: "rgba(0,0,0,0.5)",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    zIndex: 1000,
};

const modalContentStyle = {
    backgroundColor: "#fff",
    padding: "1.5rem",
    borderRadius: 8,
    position: "relative",
    width: "90%",
    maxWidth: 400,
};
