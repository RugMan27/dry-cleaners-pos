import React, {useEffect, useState} from 'react';
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    Legend,
    ResponsiveContainer,
} from 'recharts';





function DailyRevenueLineChart({ data }) {

    const [colors, setColors] = useState([]);

    useEffect(() => {
        const rootStyles = getComputedStyle(document.documentElement);

        const green = rootStyles.getPropertyValue('--theme-green').trim();
        const blue = rootStyles.getPropertyValue('--theme-blue').trim();

        const selectedColors = [green, blue].filter(color => color);

        if (selectedColors.length > 0) {
            setColors(selectedColors);
        }
    }, []);
    return (
        <ResponsiveContainer height="100%" width="100%">
            <LineChart
                data={data}
                margin={{
                    top: 20,
                    right: 30,
                    left: 20,
                    bottom: 5,
                }}
            >
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip
                    contentStyle={{
                        backgroundColor: 'var(--dropdown-hover)',
                        border: 'none',
                        borderRadius: '8px',
                        boxShadow: '0 2px 6px rgba(0, 0, 0, 0.15)',
                    }}
                    labelStyle={{
                        color: 'var(--header-text)',
                        fontWeight: 'bold',
                        marginBottom: '6px',
                    }}
                />
                <Legend />
                <Line
                    type="monotone"
                    dataKey="revenue"
                    stroke={colors[0] || "#0f0"}
                    activeDot={{ r: 8 }}
                    name="Revenue ($)"
                    strokeWidth={2}
                />
                <Line
                    type="monotone"
                    dataKey="invoice_count"
                    stroke={colors[1] || "#00f"}
                    activeDot={{ r: 8 }}
                    name="Invoice Count"
                    strokeWidth={2}
                />
            </LineChart>
        </ResponsiveContainer>
    );
}

export default DailyRevenueLineChart;
