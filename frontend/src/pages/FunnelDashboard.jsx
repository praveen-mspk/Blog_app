import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Globe, Monitor, TrendingDown, Clock, Users, BarChart2, Smartphone, Tablet, Laptop, Layers, RefreshCw } from 'lucide-react';

const TABS = [
  { key: 'demographics', label: 'Demographics', icon: Globe },
  { key: 'devices', label: 'Devices & Tech', icon: Monitor },
  { key: 'journey', label: 'Reader Journey', icon: TrendingDown },
  { key: 'time', label: 'Time Patterns', icon: Clock },
  { key: 'retention', label: 'Retention', icon: Users },
  { key: 'activity', label: 'Recent Activity', icon: BarChart2 },
];

const BarChart = ({ items, color = 'indigo', maxItems = 8 }) => {
  const data = items.slice(0, maxItems);
  const maxVal = Math.max(...data.map(d => d.count || d.percentage || 0), 1);
  return (
    <div className="space-y-3">
      {data.map((item, i) => (
        <div key={i} className="flex items-center gap-4">
          <span className="text-sm font-semibold text-gray-700 w-32 truncate text-right">{item.name || item.label}</span>
          <div className="flex-grow bg-gray-100 rounded-full h-6 overflow-hidden">
            <div
              className={`h-full rounded-full bg-${color}-500 transition-all duration-700 ease-out flex items-center justify-end pr-3`}
              style={{ width: `${Math.max(4, ((item.percentage || item.count) / maxVal) * 100)}%`, backgroundColor: getColor(color, i) }}
            >
              <span className="text-xs font-bold text-white whitespace-nowrap">
                {item.percentage != null ? `${item.percentage}%` : ''} {item.count != null ? `(${item.count})` : ''}
              </span>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

function getColor(base, index) {
  const palettes = {
    indigo: ['#6366f1', '#818cf8', '#a5b4fc', '#c7d2fe', '#e0e7ff', '#4f46e5', '#4338ca', '#3730a3'],
    emerald: ['#10b981', '#34d399', '#6ee7b7', '#a7f3d0', '#d1fae5', '#059669', '#047857', '#065f46'],
    purple: ['#8b5cf6', '#a78bfa', '#c4b5fd', '#ddd6fe', '#ede9fe', '#7c3aed', '#6d28d9', '#5b21b6'],
    amber: ['#f59e0b', '#fbbf24', '#fcd34d', '#fde68a', '#fef3c7', '#d97706', '#b45309', '#92400e'],
    rose: ['#f43f5e', '#fb7185', '#fda4af', '#fecdd3', '#ffe4e6', '#e11d48', '#be123c', '#9f1239'],
  };
  return (palettes[base] || palettes.indigo)[index % 8];
}

const StatCard = ({ label, value, sub, icon: Icon, color = 'indigo' }) => (
  <div className={`bg-white rounded-2xl p-6 border border-gray-100 shadow-sm hover:shadow-md transition-shadow`}>
    <div className="flex items-center justify-between mb-3">
      <span className="text-sm font-semibold text-gray-500">{label}</span>
      {Icon && <Icon size={20} className={`text-${color}-500`} style={{ color: getColor(color, 0) }} />}
    </div>
    <div className="text-3xl font-black text-gray-900">{value}</div>
    {sub && <div className="text-sm text-gray-500 mt-1 font-medium">{sub}</div>}
  </div>
);

const FunnelStep = ({ label, value, width, color }) => (
  <div className="flex flex-col items-center" style={{ width: `${width}%` }}>
    <div className="w-full rounded-xl py-4 text-center font-black text-white text-xl shadow-lg transition-all hover:scale-105" style={{ backgroundColor: color }}>
      {typeof value === 'number' ? value.toLocaleString() : value}
    </div>
    <div className="text-sm font-bold text-gray-600 mt-2">{label}</div>
  </div>
);

const FunnelDashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('demographics');
  const [data, setData] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) { navigate('/login'); return; }

    const fetchAll = async () => {
      try {
        const endpoints = {
          demographics: '/analytics/funnel/demographics',
          devices: '/analytics/funnel/devices',
          journey: '/analytics/funnel/journey',
          'time-patterns': '/analytics/funnel/time-patterns',
          retention: '/analytics/funnel/retention',
          activity: '/analytics/funnel/recent-activity',
        };
        const results = {};
        await Promise.all(
          Object.entries(endpoints).map(async ([key, url]) => {
            const res = await api.get(url);
            results[key] = res.data;
          })
        );
        setData(results);
      } catch (err) {
        console.error('Failed to load analytics:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchAll();
  }, [user, navigate]);

  const formatDuration = (seconds) => {
    if (!seconds) return '0s';
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return m > 0 ? `${m}m ${s}s` : `${s}s`;
  };

  if (loading) return (
    <div className="min-h-screen bg-gray-50 pt-24 flex items-center justify-center">
      <div className="text-center">
        <RefreshCw size={40} className="animate-spin text-indigo-500 mx-auto mb-4" />
        <p className="text-lg font-bold text-gray-600">Loading Analytics...</p>
      </div>
    </div>
  );

  const demo = data.demographics || {};
  const devices = data.devices || {};
  const journey = data.journey || {};
  const time = data['time-patterns'] || {};
  const retention = data.retention || {};
  const recentActivity = data.activity || [];

  return (
    <div className="min-h-screen bg-gray-50 pt-24 pb-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="flex items-center justify-between mb-10">
          <div>
            <h1 className="text-4xl font-black text-gray-900 tracking-tight">Funnel Matrix Analytics</h1>
            <p className="text-gray-500 mt-2 font-medium">Deep insights into your audience demographics and behavior</p>
          </div>
          <BarChart2 size={40} className="text-indigo-500" />
        </div>

        {/* Tabs */}
        <div className="flex gap-2 mb-8 overflow-x-auto pb-2">
          {TABS.map(tab => {
            const Icon = tab.icon;
            return (
              <button
                key={tab.key}
                onClick={() => setActiveTab(tab.key)}
                className={`flex items-center gap-2 px-5 py-3 rounded-xl font-bold text-sm transition-all whitespace-nowrap ${
                  activeTab === tab.key
                    ? 'bg-gray-900 text-white shadow-lg shadow-gray-900/20'
                    : 'bg-white text-gray-600 border border-gray-200 hover:bg-gray-100'
                }`}
              >
                <Icon size={18} />
                {tab.label}
              </button>
            );
          })}
        </div>

        {/* TAB CONTENT */}
        <div className="animate-fade-in">

          {/* A. Demographics */}
          {activeTab === 'demographics' && (
            <div className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                <StatCard label="Total Views" value={demo.totalViews?.toLocaleString() || 0} icon={Globe} color="indigo" />
                <StatCard label="Countries" value={demo.countries?.length || 0} icon={Globe} color="emerald" />
                <StatCard label="Cities" value={demo.cities?.length || 0} icon={Globe} color="purple" />
              </div>
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <div className="bg-white rounded-3xl p-6 border border-gray-100 shadow-sm">
                  <h3 className="text-lg font-bold text-gray-900 mb-6">📍 Location Distribution</h3>
                  {demo.countries?.length > 0 ? (
                    <BarChart items={demo.countries} color="indigo" />
                  ) : (
                    <p className="text-gray-400 text-center py-8">No location data yet. Views will populate this chart.</p>
                  )}
                </div>
                <div className="bg-white rounded-3xl p-6 border border-gray-100 shadow-sm">
                  <h3 className="text-lg font-bold text-gray-900 mb-6">🏙️ City-Level Insights</h3>
                  {demo.cities?.length > 0 ? (
                    <BarChart items={demo.cities} color="emerald" />
                  ) : (
                    <p className="text-gray-400 text-center py-8">No city data yet.</p>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* B. Devices & Tech */}
          {activeTab === 'devices' && (
            <div className="space-y-6">
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <div className="bg-white rounded-3xl p-6 border border-gray-100 shadow-sm">
                  <h3 className="text-lg font-bold text-gray-900 mb-6 flex items-center gap-2"><Laptop size={20} className="text-indigo-500" /> Device Types</h3>
                  {devices.deviceTypes?.length > 0 ? (
                    <BarChart items={devices.deviceTypes} color="indigo" />
                  ) : (
                    <p className="text-gray-400 text-center py-8">No device data yet.</p>
                  )}
                </div>
                <div className="bg-white rounded-3xl p-6 border border-gray-100 shadow-sm">
                  <h3 className="text-lg font-bold text-gray-900 mb-6 flex items-center gap-2"><Layers size={20} className="text-emerald-500" /> Operating Systems</h3>
                  {devices.operatingSystems?.length > 0 ? (
                    <BarChart items={devices.operatingSystems} color="emerald" />
                  ) : (
                    <p className="text-gray-400 text-center py-8">No OS data yet.</p>
                  )}
                </div>
                <div className="bg-white rounded-3xl p-6 border border-gray-100 shadow-sm">
                  <h3 className="text-lg font-bold text-gray-900 mb-6 flex items-center gap-2"><Globe size={20} className="text-purple-500" /> Browsers</h3>
                  {devices.browsers?.length > 0 ? (
                    <BarChart items={devices.browsers} color="purple" />
                  ) : (
                    <p className="text-gray-400 text-center py-8">No browser data yet.</p>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* C. Reader Journey Funnel */}
          {activeTab === 'journey' && (
            <div className="space-y-6">
              <div className="bg-white rounded-3xl p-8 border border-gray-100 shadow-sm">
                <h3 className="text-lg font-bold text-gray-900 mb-8">🔻 Reader Journey Funnel</h3>
                <div className="flex items-end justify-center gap-4 mb-10">
                  <FunnelStep label="Views" value={journey.views || 0} width={100} color="#6366f1" />
                  <div className="text-2xl font-black text-gray-300 pb-8">→</div>
                  <FunnelStep label="Readers" value={journey.readers || 0} width={75} color="#8b5cf6" />
                  <div className="text-2xl font-black text-gray-300 pb-8">→</div>
                  <FunnelStep label="Engaged" value={journey.engaged || 0} width={55} color="#a855f7" />
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="bg-indigo-50 rounded-2xl p-5 border border-indigo-100">
                    <p className="text-sm font-semibold text-indigo-600">Views → Readers</p>
                    <p className="text-3xl font-black text-indigo-700">{journey.viewsToReaders || 0}%</p>
                  </div>
                  <div className="bg-purple-50 rounded-2xl p-5 border border-purple-100">
                    <p className="text-sm font-semibold text-purple-600">Readers → Engaged</p>
                    <p className="text-3xl font-black text-purple-700">{journey.readersToEngaged || 0}%</p>
                  </div>
                </div>
              </div>

              {journey.dropOff && Object.keys(journey.dropOff).length > 0 && (
                <div className="bg-white rounded-3xl p-6 border border-gray-100 shadow-sm">
                  <h3 className="text-lg font-bold text-gray-900 mb-6">📉 Drop-off Analysis</h3>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    {Object.entries(journey.dropOff).map(([key, val]) => (
                      <div key={key} className="bg-gray-50 rounded-2xl p-5 text-center border border-gray-100">
                        <p className="text-sm font-semibold text-gray-500 capitalize">Exited at {key}</p>
                        <p className="text-3xl font-black text-gray-900">{val}%</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* D. Time Patterns */}
          {activeTab === 'time' && (
            <div className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-2">
                <StatCard
                  label="Avg. Reading Duration"
                  value={formatDuration(time.avgDurationSeconds || 0)}
                  icon={Clock}
                  color="amber"
                />
              </div>
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <div className="bg-white rounded-3xl p-6 border border-gray-100 shadow-sm">
                  <h3 className="text-lg font-bold text-gray-900 mb-6">⏰ Peak Reading Times</h3>
                  {time.peakHours?.length > 0 ? (
                    <div className="space-y-3">
                      {time.peakHours.map((h, i) => (
                        <div key={i} className="flex items-center justify-between p-4 bg-amber-50 rounded-2xl border border-amber-100">
                          <span className="font-bold text-amber-800">{h.hour}</span>
                          <span className="font-black text-amber-600">{h.count} readers</span>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-gray-400 text-center py-8">No time data yet.</p>
                  )}
                </div>
                <div className="bg-white rounded-3xl p-6 border border-gray-100 shadow-sm">
                  <h3 className="text-lg font-bold text-gray-900 mb-6">📅 Most Active Days</h3>
                  {time.activeDays?.length > 0 ? (
                    <div className="space-y-3">
                      {time.activeDays.map((d, i) => (
                        <div key={i} className="flex items-center justify-between p-4 bg-emerald-50 rounded-2xl border border-emerald-100">
                          <span className="font-bold text-emerald-800">{d.day}</span>
                          <span className="font-black text-emerald-600">{d.count} readers</span>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-gray-400 text-center py-8">No daily data yet.</p>
                  )}
                </div>
              </div>
              {time.categoryDurations?.length > 0 && (
                <div className="bg-white rounded-3xl p-6 border border-gray-100 shadow-sm">
                  <h3 className="text-lg font-bold text-gray-900 mb-6">📚 Avg. Duration by Category</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                    {time.categoryDurations.map((c, i) => (
                      <div key={i} className="bg-gray-50 rounded-2xl p-5 text-center border border-gray-100">
                        <p className="text-sm font-semibold text-gray-500">{c.category}</p>
                        <p className="text-2xl font-black text-gray-900 mt-1">{formatDuration(c.avgSeconds)}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* E. Retention */}
          {activeTab === 'retention' && (
            <div className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-2">
                <StatCard label="Total Readers" value={retention.totalReaders?.toLocaleString() || 0} icon={Users} color="indigo" />
                <StatCard
                  label="Returning Readers"
                  value={retention.returningReaders?.toLocaleString() || 0}
                  sub={`${retention.returningPercentage || 0}% of total`}
                  icon={RefreshCw}
                  color="emerald"
                />
                <StatCard
                  label="New Readers"
                  value={retention.newReaders?.toLocaleString() || 0}
                  sub={`${retention.newPercentage || 0}% of total`}
                  icon={Users}
                  color="purple"
                />
              </div>

              <div className="bg-white rounded-3xl p-6 border border-gray-100 shadow-sm">
                <h3 className="text-lg font-bold text-gray-900 mb-6">⚠️ Churn Risk Factors</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="bg-amber-50 rounded-2xl p-6 text-center border border-amber-100">
                    <p className="text-sm font-semibold text-amber-600">30+ Days Inactive</p>
                    <p className="text-4xl font-black text-amber-700 mt-2">{retention.inactive30Days || 0}</p>
                    <p className="text-xs text-amber-500 mt-1 font-medium">readers</p>
                  </div>
                  <div className="bg-orange-50 rounded-2xl p-6 text-center border border-orange-100">
                    <p className="text-sm font-semibold text-orange-600">60+ Days Inactive</p>
                    <p className="text-4xl font-black text-orange-700 mt-2">{retention.inactive60Days || 0}</p>
                    <p className="text-xs text-orange-500 mt-1 font-medium">readers</p>
                  </div>
                  <div className="bg-red-50 rounded-2xl p-6 text-center border border-red-100">
                    <p className="text-sm font-semibold text-red-600">90+ Days Inactive</p>
                    <p className="text-4xl font-black text-red-700 mt-2">{retention.inactive90Days || 0}</p>
                    <p className="text-xs text-red-500 mt-1 font-medium">readers</p>
                  </div>
                </div>
              </div>

              {/* Visual: Returning vs New donut-like */}
              <div className="bg-white rounded-3xl p-6 border border-gray-100 shadow-sm">
                <h3 className="text-lg font-bold text-gray-900 mb-6">🔄 Reader Composition</h3>
                <div className="flex items-center gap-8 justify-center">
                  <div className="w-40 h-40 rounded-full border-8 border-emerald-500 flex items-center justify-center relative"
                    style={{ background: `conic-gradient(#10b981 ${(retention.returningPercentage || 0) * 3.6}deg, #e5e7eb ${(retention.returningPercentage || 0) * 3.6}deg)` }}>
                    <div className="w-28 h-28 rounded-full bg-white flex items-center justify-center">
                      <div className="text-center">
                        <p className="text-2xl font-black text-gray-900">{retention.returningPercentage || 0}%</p>
                        <p className="text-xs font-semibold text-gray-500">Returning</p>
                      </div>
                    </div>
                  </div>
                  <div className="space-y-3">
                    <div className="flex items-center gap-3">
                      <div className="w-4 h-4 rounded-full bg-emerald-500"></div>
                      <span className="font-bold text-gray-700">Returning: {retention.returningReaders || 0}</span>
                    </div>
                    <div className="flex items-center gap-3">
                      <div className="w-4 h-4 rounded-full bg-gray-300"></div>
                      <span className="font-bold text-gray-700">New: {retention.newReaders || 0}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}
          {/* F. Recent Activity Log */}
          {activeTab === 'activity' && (
            <div className="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
              <div className="p-6 border-b border-gray-100 flex items-center justify-between">
                <h3 className="text-lg font-bold text-gray-900">📑 Detailed Reader Activity</h3>
                <span className="text-xs font-bold px-3 py-1 bg-indigo-100 text-indigo-700 rounded-full">Last 50 Visits</span>
              </div>
              <div className="overflow-x-auto">
                <table className="w-full text-left">
                  <thead className="bg-gray-50 text-gray-500 text-xs font-black uppercase tracking-wider">
                    <tr>
                      <th className="px-6 py-4">Reader</th>
                      <th className="px-6 py-4">Post</th>
                      <th className="px-6 py-4">Location</th>
                      <th className="px-6 py-4">Device / OS</th>
                      <th className="px-6 py-4">Engagement</th>
                      <th className="px-6 py-4 text-right">Time</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {recentActivity.length > 0 ? (
                      recentActivity.map((pv) => (
                        <tr key={pv.id} className="hover:bg-gray-50 transition-colors">
                          <td className="px-6 py-4">
                            <div className="flex items-center gap-2">
                              <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold ${pv.readerName === 'Anonymous' ? 'bg-gray-200 text-gray-500' : 'bg-indigo-100 text-indigo-700'}`}>
                                {pv.readerName[0]}
                              </div>
                              <span className="font-bold text-gray-900">{pv.readerName}</span>
                            </div>
                          </td>
                          <td className="px-6 py-4 font-medium text-gray-600 truncate max-w-xs">{pv.postTitle}</td>
                          <td className="px-6 py-4 text-sm text-gray-500">{pv.location}</td>
                          <td className="px-6 py-4">
                            <div className="flex flex-col">
                              <span className="text-xs font-bold text-gray-900 flex items-center gap-1">
                                {pv.device === 'MOBILE' ? <Smartphone size={12} /> : pv.device === 'TABLET' ? <Tablet size={12} /> : <Monitor size={12} />}
                                {pv.browser}
                              </span>
                              <span className="text-[10px] text-gray-400 font-medium">{pv.os}</span>
                            </div>
                          </td>
                          <td className="px-6 py-4">
                            <div className="flex items-center gap-3">
                              <div className="flex flex-col">
                                <span className="text-xs font-black text-gray-700">{formatDuration(pv.duration)}</span>
                                <div className="w-16 bg-gray-100 h-1.5 rounded-full mt-1">
                                  <div className="bg-emerald-500 h-full rounded-full" style={{ width: `${pv.scroll}%` }}></div>
                                </div>
                              </div>
                            </div>
                          </td>
                          <td className="px-6 py-4 text-right">
                            <span className="text-xs font-bold text-gray-400">
                              {new Date(pv.time).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </span>
                            <br />
                            <span className="text-[10px] text-gray-300 font-medium">
                              {new Date(pv.time).toLocaleDateString()}
                            </span>
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="6" className="px-6 py-12 text-center text-gray-400 font-medium">
                          No recent activity recorded yet.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default FunnelDashboard;
