import { useState, useEffect } from "react";
import { apiClient } from "../../../utils/apiClient.js";
import Styles from "./DashboardScheduling.module.css";
import DashboardCard from "../../../components/DashboardCard/DashboardCard.jsx";
import { createDragAndDropPlugin } from '@schedule-x/drag-and-drop';
import { createResizePlugin } from '@schedule-x/resize';
import { useCalendarApp, ScheduleXCalendar } from '@schedule-x/react';
import 'react-datepicker/dist/react-datepicker.css';
import DatePicker from 'react-datepicker';
import {
    createViewDay,
    createViewMonthAgenda,
    createViewMonthGrid,
    createViewWeek,
} from '@schedule-x/calendar';
import { createEventsServicePlugin } from '@schedule-x/events-service';
import './DashboardScheduleCalender.css';
import '../../../index.css';
import ScheduleEventModal from "../../../components/ScheduleEventModal/ScheduleEventModal.jsx";
import { FaPlus } from "react-icons/fa";
import NoBgDashboardCard from "../../../components/DashboardCard/NoBgDashboardCard.jsx";
import Modal from "../../../components/Modal/Modal.jsx";
import Select from 'react-select';


const CalendarColorStyles = {};

function formatTime(date) {
    if (!date) return '';
    let hours = date.getHours();
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const ampm = hours >= 12 ? 'PM' : 'AM';

    hours = hours % 12;
    hours = hours === 0 ? 12 : hours; // 0 becomes 12

    return `${hours}:${minutes} ${ampm}`;
}



export default function DashboardScheduling() {
    const eventsService = useState(() => createEventsServicePlugin())[0];
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [employeeList, setEmployeeList] = useState([]);
    const [selectedEmployeeId, setSelectedEmployeeId] = useState("");

    const [addEventModal, setAddEventModal] = useState(false);
    const [addShiftModal, setAddShiftModal] = useState(false);
    const [selectedShiftEmployees, setSelectedShiftEmployees] = useState([]);

    const [eventColor, setEventColor] = useState("#f9d71c"); // default color




    const [shiftStart, setShiftStart] = useState(null);
    const [shiftEnd, setShiftEnd] = useState(null);
    const [selectedBusiness, setSelectedBusiness] = useState(null);
    const [eventTitle, setEventTitle] = useState("");

    const businessOptions = [
        { value: 'mr_tuxedo', label: 'Mr Tuxedo' },
        { value: 'mr_kleaners', label: "Mr Kellys Kleaners" }
    ];


    const calendar = useCalendarApp({
        views: [createViewDay(), createViewWeek(), createViewMonthGrid(), createViewMonthAgenda()],
        events: [],
        defaultView: createViewWeek().name,
        plugins: [
            eventsService,
            createResizePlugin(),
            createDragAndDropPlugin(),
        ],
        calendars: {
            main: {
                colorName: 'personal',
                lightColors: {
                    main: '#f9d71c',
                    container: '#fff5aa',
                    onContainer: '#594800',
                },
                darkColors: {
                    main: '#fff5c0',
                    onContainer: '#fff5de',
                    container: '#a29742',
                },
            },
        },
        isDarkMode: true,
        isDragging: false,

        callbacks: {
            onEventClick(event) {
                setSelectedEvent(event);
            },
            onEventUpdate(updatedEvent) {
                apiClient.put("/events/update?id=" + updatedEvent.id, updatedEvent);
                console.log('onEventUpdate', updatedEvent);
            },
            onBeforeEventUpdate(oldEvent, newEvent) {
                const start = new Date(newEvent.start);
                const end = new Date(newEvent.end);
                const isSameDay =
                    start.getFullYear() === end.getFullYear() &&
                    start.getMonth() === end.getMonth() &&
                    start.getDate() === end.getDate();
                return isSameDay;
            }
        }
    });

    async function fetchData() {
        try {
            const range = calendar.$app.calendarState.range.value;
            console.log('Calendar rendered. Current date range:', range.start, 'to', range.end);

            let eventsEndpoint = `/events/search?from=${range.start}&to=${range.end}`;

            if (selectedEmployeeId.length > 0) {
                eventsEndpoint += `&employeeId=${selectedEmployeeId}`;
            }

            const employeeRes = await apiClient.get("/employees");
            const employeeMap = {};
            employeeRes.forEach(emp => {
                employeeMap[emp.id] = `${emp.first_name} ${emp.last_name}`;
            });
            setEmployeeList(employeeRes);

            const eventsRes = await apiClient.get(eventsEndpoint);
            eventsService.set([]);

            eventsRes.forEach(event => {
                const hex = event.extra?.color?.replace("#", "") || "cccccc";
                const className = `eventColor${hex}`;

                if (!CalendarColorStyles[className]) {
                    const styleElement = document.createElement("style");
                    styleElement.innerHTML = `
                        .${className} {
                            background-color: #${hex} !important;
                        }
                    `;
                    document.head.appendChild(styleElement);
                    CalendarColorStyles[className] = true;
                }

                if (Array.isArray(event.people)) {
                    event.people = event.people.map(id => employeeMap[id] || id);
                }

                event._options = event._options || {};
                event._options.additionalClasses = event._options.additionalClasses || [];
                event._options.additionalClasses.push(className);

                eventsService.eventsFacade.add(event);
            });

        } catch (err) {
            console.error("Failed to load employees or events:", err);
        }
    }

    useEffect(() => {
        fetchData();
    }, [calendar, eventsService, selectedEmployeeId]);

    function handleRefresh() {
        fetchData();
    }

    function handleAddEvent() {
        setAddEventModal(true);
    }

    function handleAddShift() {
        if(!addShiftModal){
            setAddShiftModal(true)

            return;
        }

        let finalEventTitle = eventTitle;
        if (!finalEventTitle || finalEventTitle.trim() === "") {
            if (selectedShiftEmployees.length > 0 && shiftStart && shiftEnd) {
                finalEventTitle = `${employeeOptions
                    .filter(opt => selectedShiftEmployees.includes(opt.value))
                    .map(opt => opt.label.split(' ')[0])
                    .join(', ')} – ${formatTime(shiftStart)}–${formatTime(shiftEnd)}${selectedBusiness ? ` @ ${selectedBusiness.label}` : ''}`;
            } else {
                finalEventTitle = "New Shift";
            }
        }

        console.log("Employees selected:", selectedShiftEmployees);
        console.log("Selected business:", selectedBusiness);
        console.log("Event title:", finalEventTitle);
        console.log("Shift start:", shiftStart);
        console.log("Shift end:", shiftEnd);

        setAddShiftModal(false);
    }

    function handleEmployeeChange(e) {
        setSelectedEmployeeId(e.target.value);
    }

    const employeeOptions = employeeList.map((employee) => ({
        value: employee.id,
        label: `${employee.first_name} ${employee.last_name} (${employee.employee_type})`
    }));

    return (
        <div className={Styles.container}>
            <Modal isOpen={addEventModal} onClose={() => setAddEventModal(false)} title="Add Event">
                <p>No employee selection needed for events.</p>
            </Modal>

            <Modal isOpen={addShiftModal} onClose={() => setAddShiftModal(false)} title="Add Shift">
                <form
                    onSubmit={(e) => {
                        e.preventDefault(); // prevent default form submit reload
                        handleAddShift();   // your submit handler
                    }}
                >
                    <label htmlFor="shiftEmployeeSelect">👤 Employee(s):</label>
                    <Select
                        id="shiftEmployeeSelect"
                        isMulti
                        options={employeeOptions}
                        value={employeeOptions.filter(opt => selectedShiftEmployees.includes(opt.value))}
                        onChange={(selected) => {
                            const ids = selected ? selected.map(opt => opt.value) : [];
                            setSelectedShiftEmployees(ids);
                        }}
                        className="react-select-container"
                        classNamePrefix="react-select"
                    />

                    <label htmlFor="businessSelect" style={{ marginTop: '1rem' }}>🏢 Business:</label>
                    <Select
                        id="businessSelect"
                        options={businessOptions}
                        value={selectedBusiness}
                        onChange={setSelectedBusiness}
                        className="react-select-container"
                        classNamePrefix="react-select"
                        placeholder="Select a business..."
                    />

                    <label htmlFor="eventTitle" style={{ marginTop: '1rem' }}>📝 Event Title (optional):</label>
                    <input
                        id="eventTitle"
                        type="text"
                        value={eventTitle}
                        onChange={(e) => setEventTitle(e.target.value)}
                        placeholder={
                            selectedShiftEmployees.length > 0 && shiftStart && shiftEnd
                                ? `${employeeOptions
                                    .filter(opt => selectedShiftEmployees.includes(opt.value))
                                    .map(opt => opt.label.split(' ')[0])
                                    .join(', ')} – ${formatTime(shiftStart)}–${formatTime(shiftEnd)}${selectedBusiness ? ` @ ${selectedBusiness.label}` : ''}`
                                : 'Enter event title'
                        }
                        className="defaultInput"
                        style={{ width: '100%', marginTop: '0.25rem' }}
                    />

                    <label htmlFor="shiftStart" style={{ marginTop: '1rem' }}>🕒 Shift Start:</label>
                    <DatePicker
                        id="shiftStart"
                        selected={shiftStart}
                        onChange={(date) => setShiftStart(date)}
                        showTimeSelect
                        timeFormat="h:mm aa"
                        timeIntervals={15}
                        dateFormat="MMMM d, yyyy h:mm aa"
                        placeholderText="Select start date and time"
                        className="defaultInput"
                        wrapperClassName="datePickerWrapper"
                        style={{ marginTop: '0.25rem' }}
                    />

                    <label htmlFor="shiftEnd" style={{ marginTop: '1rem' }}>🕒 Shift End:</label>
                    <DatePicker
                        id="shiftEnd"
                        selected={shiftEnd}
                        onChange={(date) => setShiftEnd(date)}
                        showTimeSelect
                        timeFormat="h:mm aa"
                        timeIntervals={15}
                        dateFormat="MMMM d, yyyy h:mm aa"
                        placeholderText="Select end date and time"
                        className="defaultInput"
                        wrapperClassName="datePickerWrapper"
                        style={{ marginTop: '0.25rem' }}
                        minDate={shiftStart}
                    />
                    <label htmlFor="eventColor" style={{ marginTop: '1rem' }}>🎨 Event Color:</label>
                    <input
                        type="color"
                        id="eventColor"
                        value={eventColor}
                        onChange={(e) => setEventColor(e.target.value)}
                        style={{ width: '100%', height: '2.5rem', border: 'none', padding: 0, marginTop: '0.25rem', cursor: 'pointer' }}
                    />

                    <button
                        type="submit"
                        className="defaultSubmitButton"
                        style={{ marginTop: '10px' }}
                    >
                        Submit
                    </button>
                </form>
            </Modal>



            <DashboardCard title="Employee Scheduling" direction="column">
                <div className={Styles.calendarControls}>
                    <div className={Styles.buttonGroup}>
                        <NoBgDashboardCard direction="row">
                            <button onClick={handleRefresh} className="defaultButton">🔄 Refresh</button>
                            <button onClick={handleAddEvent} className="defaultButton">➕ Add Event</button>
                            <button onClick={handleAddShift} className="defaultButton">🕒 Add Shift</button>

                            <div className={Styles.employeeSelector}>
                                <label htmlFor="employeeSelect">Employee Filter:</label>
                                <select
                                    id="employeeSelect"
                                    value={selectedEmployeeId}
                                    onChange={handleEmployeeChange}
                                    className="defaultInput"
                                >
                                    <option value="">All Employees</option>
                                    {employeeList.map((employee) => (
                                        <option key={employee.id} value={employee.id}>
                                            {employee.first_name} {employee.last_name} ({employee.employee_type})
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </NoBgDashboardCard>
                    </div>
                </div>

                <div className={Styles.calendarWrapper}>
                    <ScheduleXCalendar calendarApp={calendar} />
                </div>
            </DashboardCard>

            <ScheduleEventModal
                event={selectedEvent}
                isOpen={!!selectedEvent}
                onClose={() => setSelectedEvent(null)}
            />
        </div>
    );
}
