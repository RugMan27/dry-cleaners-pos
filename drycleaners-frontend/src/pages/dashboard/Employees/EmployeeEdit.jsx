import { useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { apiClient } from "../../../utils/apiClient.js";
import Styles from "./EmployeeEdit.module.css";
import DashboardCard from "../../../components/DashboardCard/DashboardCard.jsx";
import NoBgDashboardCard from "../../../components/DashboardCard/NoBgDashboardCard.jsx";


function fileToBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result.split(",")[1]); // remove data:*/*;base64,
        reader.onerror = error => reject(error);
    });
}

export default function EmployeeEdit() {
    const location = useLocation();
    const navigate = useNavigate();

    const params = new URLSearchParams(location.search);
    const employeeId = params.get("eid");

    const [employee, setEmployee] = useState(null);
    const [originalEmployee, setOriginalEmployee] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [deleting, setdeleting] = useState(false);
    const [error, setError] = useState(null);

    const [photoFile, setPhotoFile] = useState(null);
    const [photoPreview, setPhotoPreview] = useState("");

    const [password, setPassword] = useState("");
    const [employeeTypes, setEmployeeTypes] = useState([]);

    const [newEmployee, setNewEmployee] = useState(false);

    const [dashboardCardTitle, setDashboardCardTitle] = useState("Create Employee");
    // Replace this with actual logged-in user's role


    useEffect(() => {
        async function updateAssignableEmployeeTypes() {
            const me = await apiClient.get("/employees/me");
            const currentUserRole = me.employee_type;

            const allRoles = ["EMPLOYEE", "CLERK", "MANAGER", "ADMIN", "OWNER"];
            const roleLabels = {
                EMPLOYEE: "Employee",
                CLERK: "Clerk",
                MANAGER: "Manager",
                ADMIN: "Admin",
                OWNER: "Owner"
            };

            console.log(currentUserRole);
            const currentIndex = allRoles.indexOf(currentUserRole);
            if (currentIndex === -1) return;

            let allowedRoles;

            if (currentUserRole === "ADMIN" || currentUserRole === "OWNER") {
                allowedRoles = allRoles;
            } else {
                allowedRoles = allRoles.slice(0, currentIndex + 1);
            }

            const mapped = allowedRoles.map(role => ({
                label: roleLabels[role],
                value: role,
            }));

            setEmployeeTypes(mapped);
        }

        updateAssignableEmployeeTypes()
        if (!employeeId) {
            setError("No employee ID specified");
            setLoading(false);
            return;
        }

        async function fetchEmployee() {
            if(employeeId === "NEW"){
                setNewEmployee(true);
                setOriginalEmployee({
                    first_name: "",
                    last_name: "",
                    username: "",
                    email: "",
                    phone: "",
                    employee_type: "",
                    enabled: true,
                    extra_data: {
                        notes: ""
                    }
                });
                setLoading(false);
            }else {
                try {
                    setLoading(true);
                    const emp =  await apiClient.get(`/employees/search?id=${employeeId}`);
                    setEmployee(emp);
                    setOriginalEmployee(emp);
                    setPhotoPreview(emp.photo_url || "");
                    console.log("Employee found");
                    console.log(emp);
                    setDashboardCardTitle(`Edit Employee: ${emp.first_name} ${emp.last_name}`);
                } catch (e) {
                    console.error("Load failed:", e);
                    setError("Failed to Load Employee Data");
                } finally {
                    setLoading(false);
                }
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
        const file = e.target.files?.[0];
        if (!file) return;

        const img = new Image();
        const objectUrl = URL.createObjectURL(file);

        img.onload = () => {
            if (img.width === 400 && img.height === 400) {
                // Valid image — safe to use
                setPhotoPreview(objectUrl);
                // Save file for upload later (if you have state for it)
                setPhotoFile(file);
            } else {
                alert("Image must be exactly 400x400 pixels.");
                URL.revokeObjectURL(objectUrl); // cleanup
                e.target.value = ""; // clear file input
            }
        };

        img.onerror = () => {
            alert("Invalid image file.");
            URL.revokeObjectURL(objectUrl);
            e.target.value = "";
        };

        img.src = objectUrl;
    }


    async function handleSave() {
        setSaving(true);
        setError(null);

        try {
            const body = {
                last_name:
                    employee.last_name !== originalEmployee.last_name ? employee.last_name : null,
                first_name:
                    employee.first_name !== originalEmployee.first_name ? employee.first_name : null,
                phone: employee.phone !== originalEmployee.phone ? employee.phone : null,
                email: employee.email !== originalEmployee.email ? employee.email : null,
                username:
                    employee.username !== originalEmployee.username ? employee.username : null,
                password: password.trim() !== "" ? password : null,
                enabled:
                    employee.enabled !== originalEmployee.enabled ? employee.enabled : null,
                employee_type:
                    employee.employee_type !== originalEmployee.employee_type
                        ? employee.employee_type
                        : null,
                extra_data: {
                    notes:
                        employee.extra_data?.notes !== originalEmployee.extra_data?.notes
                            ? employee.extra_data?.notes
                            : null,
                },
                image_base64: null,
            };

            if (photoFile) {
                console.log("PGOTOING")
                body.image_base64 = await fileToBase64(photoFile);
            }

            console.log(body);

            if(newEmployee) {
                await apiClient.post("/employees/create", body);
            }else{
                await apiClient.put(`/employees/update?id=${employeeId}`, body);
            }
            
            navigate("/dashboard/employees");
        } catch (e) {
            console.error("Save failed:", e);
            setError("Failed to save changes yk");
        } finally {
            setSaving(false);
        }

    }
    async function handleDelete() {
        setdeleting(true);
        setError(null);
        try{
            console.log("Deleting Employee");
            await apiClient.delete(`/employees/delete?id=${employeeId}`);
            navigate("/dashboard/employees");
        } catch (e) {
            console.error("Delete failed:", e);
            setError("Failed to delete employee");
        } finally {
            setdeleting(false);

        }
    }

    if (loading) return <p>Loading employee data...</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;
    if (!employee && !newEmployee) return <p>Employee not found</p>;





    return (
        <div>
            <DashboardCard title={dashboardCardTitle} direction="row">

                <form
                    onSubmit={e => {
                        e.preventDefault();
                        handleSave();
                    }}
                    className={Styles.formGrid}
                >
                    <div className={Styles.formContainer}>

                    <div className={`${Styles.formSection} ${Styles.formSectionLeft}`}>
                        <label className={Styles.formLabel}>
                            First Name
                            <input
                                type="text"
                                value={employee?.first_name ?? ""}
                                onChange={e => handleChange("first_name", e.target.value)}
                                className={`defaultInput`}
                                required
                            />
                        </label>

                        <label className={Styles.formLabel}>
                            Last Name
                            <input
                                type="text"
                                value={employee?.last_name ?? ""}
                                onChange={e => handleChange("last_name", e.target.value)}
                                className={`defaultInput`}
                                required
                            />
                        </label>
                        {!newEmployee && (
                            <label className={Styles.formLabel}>
                                Username
                                <input
                                    type="text"
                                    value={employee?.username ?? ""}
                                    onChange={e => handleChange("username", e.target.value)}
                                    className={`defaultInput`}
                                    required
                                />
                            </label>
                        )}


                        <label className={Styles.formLabel}>
                            Employee Type
                            <select
                                value={employee?.employee_type ?? ""}
                                onChange={e => handleChange("employee_type", e.target.value)}
                                className={`defaultInput`}
                            >
                                <option value="" disabled>Select type</option>
                                {employeeTypes.map(type => (
                                    <option key={type.value} value={type.value}>{type.label}</option>
                                ))}
                            </select>
                        </label>

                        <label className={Styles.formLabel}>
                            Email
                            <input
                                type="email"
                                value={employee?.email ?? ""}
                                onChange={e => handleChange("email", e.target.value)}
                                className={`defaultInput`}
                            />
                        </label>

                        <label className={Styles.formLabel}>
                            Phone
                            <input
                                type="tel"
                                value={employee?.phone ?? ""}
                                onChange={e => handleChange("phone", e.target.value)}
                                className={`defaultInput`}
                            />
                        </label>
                    </div>

                    <div className={`${Styles.formSection} ${Styles.formSectionRight}`}>
                        <label className={Styles.formLabel}>
                            Notes
                            <textarea

                                value={employee?.extra_data?.notes ?? ""}
                                onChange={e => handleChange("notes", e.target.value)}
                                className={`${Styles.textareaField} ${Styles.notesField}`}
                            />
                        </label>

                        <label className={`${Styles.enabledCheckboxLabel} ${Styles.formLabel}`}>
                            <span>Enabled</span>
                            <input
                                type="checkbox"
                                checked={employee?.enabled ?? true}
                                onChange={e => handleChange("enabled", e.target.checked)}
                                className={Styles.enabledButton}

                            />
                        </label>

                        <label className={Styles.formLabel}>
                            {newEmployee ? "Password" : "New Password (leave blank to keep current)"}
                            <input
                                type="password"
                                value={password}
                                onChange={e => setPassword(e.target.value)}
                                className={`defaultInput`}
                                required={newEmployee}
                            />
                        </label>

                        <label className={Styles.formLabel}>
                            Profile Picture (400x400 JPG)
                            <input type="file" accept=".jpg" onChange={handlePhotoChange} />
                        </label>

                        {photoPreview && (
                            <img
                                src={photoPreview}
                                alt="Profile Preview"
                                className={Styles.previewImage}
                            />
                        )}
                    </div>

                    <div className={Styles.buttonContainer}>
                        <button type="submit" disabled={saving} className={Styles.submitButton}>
                            {saving ? "Saving..." : "Save Changes"}
                        </button>
                        <button
                            type="button"
                            disabled={deleting}
                            className={Styles.deleteButton}
                            onClick={() => handleDelete()}
                        >
                            {deleting ? "Deleting..." : "Delete Employee"}
                        </button>
                    </div>




                    </div>
                </form>

            </DashboardCard>

        </div>

    );
}
