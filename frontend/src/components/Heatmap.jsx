import React from 'react';
import { format, eachDayOfInterval, startOfYear, endOfYear, startOfWeek, endOfWeek, isSameYear } from 'date-fns';

const Heatmap = ({ data, type }) => {
  // Generate current calendar year aligned to Sunday-Saturday
  const currentYearDate = new Date();
  const start = startOfWeek(startOfYear(currentYearDate));
  const end = endOfWeek(endOfYear(currentYearDate));
  const days = eachDayOfInterval({ start, end });

  // Determine color based on type and count
  const getColor = (count) => {
    if (!count || count === 0) return 'bg-gray-100 border border-gray-200';
    if (type === 'reader') {
      if (count >= 10) return 'bg-green-500 shadow-[0_0_8px_rgba(34,197,94,0.3)]';
      if (count >= 5) return 'bg-green-400';
      return 'bg-green-300';
    } else { // writer
      if (count >= 1000) return 'bg-green-500 shadow-[0_0_8px_rgba(34,197,94,0.3)]';
      if (count >= 500) return 'bg-green-400';
      if (count >= 100) return 'bg-green-300';
      return 'bg-green-200';
    }
  };

  const getTooltipContent = (date, count) => {
    const formattedDate = format(date, 'MMM do, yyyy');
    if (!count) return `No activity on ${formattedDate}`;
    if (type === 'reader') return `${count} articles read on ${formattedDate}`;
    return `${count} words written on ${formattedDate}`;
  };

  return (
    <div className="w-full overflow-x-auto pb-4 custom-scrollbar">
      <div className="min-w-[800px]">
        <div className="flex gap-1">
          {/* Group days into weeks for column layout */}
          {Array.from({ length: Math.ceil(days.length / 7) }).map((_, weekIndex) => (
            <div key={weekIndex} className="flex flex-col gap-1">
              {days.slice(weekIndex * 7, (weekIndex + 1) * 7).map((day, dayIndex) => {
                const dateKey = format(day, 'yyyy-MM-dd');
                const count = data[dateKey] || 0;
                const isCurrentYear = isSameYear(day, currentYearDate);
                
                return (
                  <div
                    key={`${dateKey}-${dayIndex}`}
                    className={`w-3.5 h-3.5 rounded-[2px] relative ${isCurrentYear ? `cursor-pointer transition-all duration-200 hover:ring-2 hover:ring-black/20 group ${getColor(count)}` : 'opacity-0 pointer-events-none'}`}
                  >
                    {/* Tooltip */}
                    {isCurrentYear && (
                      <div className="absolute opacity-0 group-hover:opacity-100 transition-opacity bottom-full left-1/2 -translate-x-1/2 mb-2 px-2 py-1 bg-black text-white text-xs rounded whitespace-nowrap z-10 pointer-events-none">
                        {getTooltipContent(day, count)}
                        <div className="absolute top-full left-1/2 -translate-x-1/2 border-4 border-transparent border-t-black"></div>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          ))}
        </div>
        
        <div className="flex justify-end items-center gap-2 mt-4 text-xs text-gray-500">
          <span>Less</span>
          <div className="w-3 h-3 rounded-[2px] bg-gray-100 border border-gray-200"></div>
          <div className="w-3 h-3 rounded-[2px] bg-green-200"></div>
          <div className="w-3 h-3 rounded-[2px] bg-green-300"></div>
          <div className="w-3 h-3 rounded-[2px] bg-green-400"></div>
          <div className="w-3 h-3 rounded-[2px] bg-green-500"></div>
          <span>More</span>
        </div>
      </div>
    </div>
  );
};

export default Heatmap;