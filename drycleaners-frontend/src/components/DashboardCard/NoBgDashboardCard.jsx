import React from 'react';
import styles from './DashboardCard.module.css';
import clsx from 'clsx';

const NoBgDashboardCard = ({ title, children, direction = 'column', height }) => {
    return (
        <div
            className={clsx(styles.dashboardCardWrapper)}
            style={height ? { height } : undefined}
        >
            {title && <h2 className={styles.title}>{title}</h2>}
            <div
                className={clsx(styles.dashboardCard, styles.content, {
                    [styles.row]: direction === 'row',
                    [styles.column]: direction === 'column',
                })}
            >
                {children}
            </div>
        </div>
    );
};

export default NoBgDashboardCard;
