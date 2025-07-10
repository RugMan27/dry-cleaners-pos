import usePageTitle from '../../hooks/usePageTitle';
import QuickInfoCard from "../../components/QuickInfoCard/QuickInfoCard.jsx";
import QuickInfoSection from "../../components/QuickInfoSection/QuickInfoSection.jsx";
import {FaMoneyBill, FaTshirt, FaUsers, FaUserTie, FaTags  } from "react-icons/fa";
import DashboardCard from "../../components/DashboardCard/DashboardCard.jsx";
import { GiAmpleDress, GiWoodenCrate, GiSewingMachine, GiHanger   } from "react-icons/gi";
import { MdLocalLaundryService, MdFiberNew  } from "react-icons/md";
import DailyRevenueChart from "../../components/Charts/DailyRevenueChart.jsx";
import OutstandingOrdersPieChart from "../../components/Charts/OutstandingOrdersPieChart.jsx";
import {useEffect, useState} from "react";
import {apiClient} from "../../utils/apiClient.js";
import {formatDateToYYYYMMDD} from "../../utils/dateUtils.js";
import NoBgDashboardCard from "../../components/DashboardCard/NoBgDashboardCard.jsx";
import LoadingCard from "../../components/LoadingCard/LoadingCard.jsx";







export default function DashboardHome() {
    usePageTitle("Home");

    // --- State for data ---
    const [totalCustomers, setTotalCustomers] = useState(null);
    const [todayRevenue, setTodayRevenue] = useState(null);
    const [todayOrders, setTodayOrders] = useState(null);
    const [outstandingOrders, setOutstandingOrders] = useState(null);
    const [serviceCountsArray, setServiceCountsArray] = useState([]);
    const [revenueArray, setRevenueArray] = useState([]);

    // --- State for loading and error handling ---
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);


    const today = new Date();
    const formattedToday = formatDateToYYYYMMDD(today); // e.g., "2025-07-04"

    const serviceTypeMap = {
        DRY_CLEANING: 'Dry Cleaning',
        LAUNDRY: 'Laundry',
        ALTERATION: 'Alterations',
        PRESS_ONLY: 'Press Only',
        LEATHER_WORK: 'Leather Work',
        TUXEDO: 'Tuxedos',
        WEDDING: 'Wedding',
    };

    const serviceCounts = {};
    Object.values(serviceTypeMap).forEach(name => {
        serviceCounts[name] = 0;
    });




    // --- useEffect for data fetching ---
    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                setLoading(true); // Start loading

                // Fetch Total Customers
                const customersArray = await apiClient.get('/customers');
                console.log(customersArray);
                setTotalCustomers(customersArray.length);


                // Fetch Today's Revenue
                const todayRevenueEndpoint = `/invoices/cleaning/revenue?pastDays=7`;

                const todayRevenueData = await apiClient.get(todayRevenueEndpoint);
                console.log("todayRevenueData:", todayRevenueData);

                const found = todayRevenueData.revenues.find(item => item.name === formattedToday);
                if (found) {
                    setTodayRevenue(found.revenue)
                }
                setRevenueArray(todayRevenueData.revenues);


                const createdOrdersEndpoint = `/invoices/cleaning/search?createdFromDate=${formattedToday}&createdToDate=${formattedToday}`;
                const createdOrdersArray = await apiClient.get(createdOrdersEndpoint);

                setTodayOrders(createdOrdersArray.length); // Assuming { "todayOrdersCount": 37 }

                const unpaidOrdersEndpoint = `/invoices/cleaning/search?excludePickedUp=true`;
                const unpaidOrdersArray = await apiClient.get(unpaidOrdersEndpoint);



                unpaidOrdersArray.forEach(invoice => {
                    const displayName = serviceTypeMap[invoice.service_type];
                    if (displayName) {
                        serviceCounts[displayName]++;
                    } else {
                        // optionally handle unknown service types here
                        console.warn('Unknown service type:', invoice.service_type);
                    }
                });



                setOutstandingOrders(unpaidOrdersArray.length); // Assuming { "todayOrdersCount": 37 }


                setServiceCountsArray(Object.entries(serviceCounts).map(([name, value]) => ({
                    name,
                    value,
                })));


                const employeeMe = `/employees/me`;
                const employeeMeData = await apiClient.get(employeeMe);
                employeeMeData.profile_pic_base64
                console.log(employeeMeData)


            } catch (err) {
                console.error("Error fetching dashboard data:", err);
                setError("Failed to load dashboard data. Please try again.");
            } finally {
                setLoading(false); // End loading, whether successful or not
            }
        };

        fetchDashboardData();
    }, []); // Empty dependency array means this runs only once on component mount

    // --- Conditional Rendering based on loading/error state ---
    if (loading) {
        return (
            <div>
                <LoadingCard title="Loading Dashboard..." />
            </div>
        );
    }

    if (error) {
        return (
            <div>
                <DashboardCard title="Overview">
                    <QuickInfoSection>
                        <p style={{ color: 'red' }}>{error}</p>
                    </QuickInfoSection>
                </DashboardCard>
            </div>
        );



    }

console.log(serviceCounts);

    return (


        <div>
            <DashboardCard title="Overview">
                <QuickInfoSection>
                    <QuickInfoCard title="Total Customers" value={totalCustomers} icon={<FaUsers />} />
                    <QuickInfoCard title="Today's Revenue" value={"$" + todayRevenue} icon={<FaMoneyBill />} />
                    <QuickInfoCard title="Today's Orders" value={todayOrders} icon={<MdFiberNew  />} />
                    <QuickInfoCard title="Outstanding Orders" value={outstandingOrders} icon={<FaTags  />} />
                </QuickInfoSection>
            </DashboardCard>
            <DashboardCard title="Outstanding Orders">
                <QuickInfoSection>
                    <QuickInfoCard title="Dry Cleaning" value={
                        (serviceCountsArray.find(item => item.name === "Dry Cleaning")?.value) ?? 0
                    } icon={<GiHanger  />} />
                    <QuickInfoCard title="Laundry" value={
                        (serviceCountsArray.find(item => item.name === "Laundry")?.value) ?? 0
                    } icon={<MdLocalLaundryService />} />
                    <QuickInfoCard title="Alterations" value={
                        (serviceCountsArray.find(item => item.name === "Alterations")?.value) ?? 0
                    } icon={<GiSewingMachine  />} />
                    <QuickInfoCard title="Leather Work" value={
                        (serviceCountsArray.find(item => item.name === "Leather Work")?.value) ?? 0
                    } icon={<GiWoodenCrate  />} />
                    <QuickInfoCard title="Tuxedos" value={
                        (serviceCountsArray.find(item => item.name === "Tuxedos")?.value) ?? 0
                    } icon={<FaUserTie  />} />
                    <QuickInfoCard title="Wedding" value={
                        (serviceCountsArray.find(item => item.name === "Wedding")?.value) ?? 0
                    } icon={<GiAmpleDress />} />
                </QuickInfoSection>
            </DashboardCard>

        <DashboardCard direction="row" >
            <NoBgDashboardCard title="Daily Revenue" height="400px">
                    <DailyRevenueChart data={revenueArray}/>
            </NoBgDashboardCard>

            <NoBgDashboardCard title="Oustanding Order Makeup" height="400px">
                    <OutstandingOrdersPieChart data={serviceCountsArray}/>
            </NoBgDashboardCard>
        </DashboardCard>

        </div>


    );
}
