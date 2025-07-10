import React, { useState, useEffect } from 'react';
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';

function OutstandingOrdersPieChart({ data = [] }) {
    const [colors, setColors] = useState([]);

    useEffect(() => {
        const rootStyles = getComputedStyle(document.documentElement);
        const dynamicColors = [
            rootStyles.getPropertyValue('--theme-red').trim(),      // Dry Cleaning
            rootStyles.getPropertyValue('--theme-orange').trim(),   // Laundry
            rootStyles.getPropertyValue('--theme-yellow').trim(),   // Alterations
            rootStyles.getPropertyValue('--theme-green').trim(),    // Leather Work
            rootStyles.getPropertyValue('--theme-blue').trim(),     // Tuxedos
            rootStyles.getPropertyValue('--theme-indigo').trim(),   // Wedding
        ].filter(color => color);

        if (dynamicColors.length > 0) {
            setColors(dynamicColors);
        }
    }, []);

    if (colors.length === 0) {
        return <div>Loading chart colors...</div>;
    }

    return (
        <ResponsiveContainer height="100%" width="100%">
            <PieChart>
                <Pie
                    data={data}
                    dataKey="value"
                    nameKey="name"
                    cx="50%"
                    cy="50%"
                    outerRadius={120}
                    label
                >
                    {data.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={colors[index % colors.length]} />
                    ))}
                </Pie>
                <Tooltip formatter={(value, name) => [`${value} orders`, name]} />
                <Legend layout="vertical" align="right" verticalAlign="middle" />
            </PieChart>
        </ResponsiveContainer>
    );
}

export default OutstandingOrdersPieChart;
