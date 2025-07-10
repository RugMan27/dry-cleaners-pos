import { useEffect, useState } from "react";
import { apiClient } from "../../../utils/apiClient.js";
import EmployeeCard from "../../../components/EmployeeCard/EmployeeCard.jsx";
import DashboardCard from "../../../components/DashboardCard/DashboardCard.jsx";
import LoadingCard from "../../../components/LoadingCard/LoadingCard.jsx";  // import your loading component
import Styles from "./DashboardEmployees.module.css";
import {FaPlus, FaSearch} from "react-icons/fa";
import {Link} from "react-router-dom";
import NoBgDashboardCard from "../../../components/DashboardCard/NoBgDashboardCard.jsx";

export default function EmployeeList() {
    const [employees, setEmployees] = useState([]);
    const [loading, setLoading] = useState(true);          // <-- loading state
    const [searchQuery, setSearchQuery] = useState("");

    useEffect(() => {
        async function fetchEmployees() {
            try {
                const res = await apiClient.get("/employees");
                setEmployees(res);
            } catch (error) {
                console.error("Failed to fetch employees", error);
            } finally {
                setLoading(false);  // <-- done loading
            }
        }
        fetchEmployees();
    }, []);

    const filtered = employees.filter(e =>
        `${e.first_name} ${e.last_name}`.toLowerCase().includes(searchQuery.toLowerCase())
    );


    return (
        <div>
            <DashboardCard title="Employees">
                <NoBgDashboardCard direction="row">
                    <div className={Styles.employeeSearchBar}>
                        <FaSearch />
                        <input
                            type="text"
                            placeholder="Search by name"
                            value={searchQuery}
                            onChange={e => setSearchQuery(e.target.value)}
                        />

                    </div>
                    <Link to="edit?eid=NEW" >
                        <button type="button" className={`${Styles.createButton} defaultButton`}>
                            <FaPlus /> Create Employee
                        </button>
                    </Link>
                </NoBgDashboardCard>


                {loading ? (
                    <LoadingCard />    // Show loading while fetching
                ) : filtered.length > 0 ? (
                    filtered.map(emp => <EmployeeCard key={emp.id} employee={emp} />)
                ) : (
                    <p>No employees found</p>
                )}
            </DashboardCard>
        </div>
    );
}

