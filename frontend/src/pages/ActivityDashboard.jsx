import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import Heatmap from '../components/Heatmap';
import { BookOpen, PenTool, Award, Clock, Calendar, CheckCircle, TrendingUp, Star, Sparkles, ShieldCheck, RefreshCcw } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const StatCard = ({ title, value, icon, subtitle }) => (
  <div className="bg-gray-50 border border-gray-100 rounded-xl p-6 transition-all hover:bg-gray-100">
    <div className="flex items-start justify-between">
      <div>
        <p className="text-gray-500 text-sm font-medium mb-1">{title}</p>
        <h3 className="text-3xl font-bold text-black mb-1">{value}</h3>
        {subtitle && <p className="text-xs text-gray-500">{subtitle}</p>}
      </div>
      <div className="p-3 bg-indigo-100 text-indigo-600 rounded-lg">
        {icon}
      </div>
    </div>
  </div>
);

const AchievementBadge = ({ type, isReader }) => {
  const getBadgeInfo = () => {
    switch (type) {
      case 'FIRST_READ': return { title: 'First Read', icon: <BookOpen size={20} />, color: 'bg-blue-500/20 text-blue-400 border-blue-500/30' };
      case 'SEVEN_DAY_STREAK': return { title: '7-Day Streak', icon: <TrendingUp size={20} />, color: 'bg-orange-500/20 text-orange-400 border-orange-500/30' };
      case 'THIRTY_DAY_STREAK': return { title: '30-Day Streak', icon: <Award size={20} />, color: 'bg-purple-500/20 text-purple-400 border-purple-500/30' };
      case 'EXPLORER': return { title: 'Explorer', icon: <CheckCircle size={20} />, color: 'bg-teal-500/20 text-teal-400 border-teal-500/30' };
      case 'BOOKWORM': return { title: 'Bookworm', icon: <BookOpen size={20} />, color: 'bg-green-500/20 text-green-400 border-green-500/30' };
      case 'NIGHT_OWL': return { title: 'Night Owl', icon: <Clock size={20} />, color: 'bg-indigo-500/20 text-indigo-400 border-indigo-500/30' };
      case 'WEEKEND_WARRIOR': return { title: 'Weekend Warrior', icon: <Calendar size={20} />, color: 'bg-pink-500/20 text-pink-400 border-pink-500/30' };
      case 'FIRST_DRAFT': return { title: 'First Draft', icon: <PenTool size={20} />, color: 'bg-blue-500/20 text-blue-400 border-blue-500/30' };
      case 'CONSISTENT_WRITER': return { title: 'Consistent Writer', icon: <TrendingUp size={20} />, color: 'bg-orange-500/20 text-orange-400 border-orange-500/30' };
      case 'PROLIFIC': return { title: 'Prolific', icon: <CheckCircle size={20} />, color: 'bg-purple-500/20 text-purple-400 border-purple-500/30' };
      case 'WORD_MASTER': return { title: 'Word Master', icon: <Award size={20} />, color: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30' };
      case 'EDITORS_CHOICE': return { title: "Editor's Choice", icon: <Award size={20} />, color: 'bg-red-500/20 text-red-400 border-red-500/30' };
      case 'NIGHT_WRITER': return { title: 'Night Writer', icon: <Clock size={20} />, color: 'bg-indigo-500/20 text-indigo-400 border-indigo-500/30' };
      case 'WEEKEND_CREATOR': return { title: 'Weekend Creator', icon: <Calendar size={20} />, color: 'bg-pink-500/20 text-pink-400 border-pink-500/30' };
      default: return { title: type.replace(/_/g, ' '), icon: <Award size={20} />, color: 'bg-gray-500/20 text-gray-400 border-gray-500/30' };
    }
  };

  const info = getBadgeInfo();
  return (
    <div className={`flex items-center gap-3 p-3 rounded-xl border ${info.color} bg-white`}>
      <div className="p-2 rounded-lg bg-gray-50">
        {info.icon}
      </div>
      <span className="font-semibold text-sm text-black">{info.title}</span>
    </div>
  );
};

const ActivityDashboard = () => {
  const { user, refreshUser } = useAuth();
  const [activeTab, setActiveTab] = useState('reader');
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  // Robust check for member status
  const isMember = user?.isMember || user?.member;

  useEffect(() => {
    console.log('ActivityDashboard: Current user state:', user);
  }, [user]);

  useEffect(() => {
    const fetchDashboard = async () => {
      setLoading(true);
      try {
        const response = await api.get(`/activity/dashboard/${activeTab}`);
        setData(response.data);
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
        const errMsg = error.response?.data?.message || error.response?.data || error.message;
        setData({ error: true, message: typeof errMsg === 'string' ? errMsg : JSON.stringify(errMsg) });
      } finally {
        setLoading(false);
      }
    };
    fetchDashboard();
  }, [activeTab]);

  const handleRefreshStatus = async () => {
    setRefreshing(true);
    try {
      await refreshUser();
    } finally {
      setRefreshing(false);
    }
  };

  return (
    <div className="min-h-screen bg-white text-black pt-24 pb-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-4xl font-black bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 text-transparent bg-clip-text mb-2">
            Activity Dashboard
          </h1>
          <p className="text-gray-500 text-lg">Track your reading and writing journey over time.</p>
        </div>

        {/* Membership Status Card */}
        <div className={`mb-12 p-8 rounded-3xl border ${isMember ? 'bg-slate-900 border-slate-800 text-white shadow-2xl' : 'bg-indigo-50 border-indigo-100 text-indigo-900 shadow-xl'}`}>
            <div className="flex flex-col md:flex-row items-center justify-between gap-8">
                <div className="flex items-center gap-6">
                    <div className={`w-16 h-16 rounded-2xl flex items-center justify-center shadow-lg ${isMember ? 'bg-indigo-600 text-white' : 'bg-white text-indigo-600'}`}>
                        {isMember ? <ShieldCheck size={32} /> : <Star size={32} />}
                    </div>
                    <div>
                        <div className="flex items-center gap-3 mb-1">
                          <h2 className="text-2xl font-black">
                              {isMember ? 'Premium Member' : 'Free Reader'}
                          </h2>
                          <button 
                            onClick={handleRefreshStatus}
                            className={`p-1.5 rounded-full hover:bg-black/10 transition-colors ${refreshing ? 'animate-spin' : ''}`}
                            title="Refresh Membership Status"
                          >
                            <RefreshCcw size={16} />
                          </button>
                        </div>
                        <div className="flex items-center gap-3">
                            <span className={`px-2.5 py-0.5 rounded-full text-xs font-bold uppercase tracking-wider ${isMember ? 'bg-indigo-500/20 text-indigo-300' : 'bg-indigo-100 text-indigo-600'}`}>
                                {user?.subscriptionType || 'FREE'} PLAN
                            </span>
                            {!isMember && (
                                <span className="text-sm font-medium opacity-70">
                                    • {user?.freeStoriesRemaining ?? 3} stories left this month
                                </span>
                            )}
                        </div>
                    </div>
                </div>
                {!isMember && (
                    <Link 
                        to="/membership" 
                        className="w-full md:w-auto px-8 py-4 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl font-bold flex items-center justify-center gap-2 transition-all shadow-lg shadow-indigo-500/30 group"
                    >
                        <Sparkles size={18} className="group-hover:rotate-12 transition-transform" />
                        Get Unlimited Access
                    </Link>
                )}
            </div>
        </div>

        {/* Custom Tabs */}
        <div className="flex space-x-1 bg-gray-50 p-1 rounded-xl w-full max-w-md mx-auto mb-12 border border-gray-100">
          <button
            onClick={() => setActiveTab('reader')}
            className={`flex-1 py-3 px-6 rounded-lg text-sm font-semibold transition-all duration-300 flex items-center justify-center gap-2 ${
              activeTab === 'reader'
                ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-500/25'
                : 'text-gray-500 hover:text-black hover:bg-gray-100'
            }`}
          >
            <BookOpen size={18} />
            Reader Stats
          </button>
          <button
            onClick={() => setActiveTab('writer')}
            className={`flex-1 py-3 px-6 rounded-lg text-sm font-semibold transition-all duration-300 flex items-center justify-center gap-2 ${
              activeTab === 'writer'
                ? 'bg-purple-600 text-white shadow-lg shadow-purple-500/25'
                : 'text-gray-500 hover:text-black hover:bg-gray-100'
            }`}
          >
            <PenTool size={18} />
            Writer Stats
          </button>
        </div>

        {loading || !data ? (
          <div className="flex justify-center py-20">
            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-600"></div>
          </div>
        ) : data.error ? (
          <div className="text-center py-20 text-red-500">
            <h3 className="font-bold mb-2">Failed to load data. Please refresh the page and try again.</h3>
            <p className="text-sm font-mono bg-red-50/50 p-4 rounded-lg inline-block w-full max-w-2xl overflow-auto text-left whitespace-pre-wrap">{data.message}</p>
          </div>
        ) : (
          <div className="space-y-8 animate-fade-in">
            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              <StatCard 
                title="Current Streak" 
                value={`${data.currentStreak || 0} Days`} 
                icon={<TrendingUp size={24} />} 
              />
              <StatCard 
                title={activeTab === 'reader' ? "Total Reading Days" : "Total Writing Days"} 
                value={activeTab === 'reader' ? (data.totalReadingDays || 0) : (data.totalWritingDays || 0)} 
                icon={<Calendar size={24} />} 
              />
              <StatCard 
                title={activeTab === 'reader' ? "Articles Read" : "Articles Published"} 
                value={activeTab === 'reader' ? (data.totalArticlesRead || 0) : (data.totalArticlesPublished || 0)} 
                icon={activeTab === 'reader' ? <BookOpen size={24} /> : <PenTool size={24} />} 
              />
              <StatCard 
                title={activeTab === 'reader' ? "Reading Time" : "Words Written"} 
                value={activeTab === 'reader' ? `${Math.round((data.totalReadingMinutes || 0) / 60)}h ${(data.totalReadingMinutes || 0) % 60}m` : (data.totalWordsWritten || 0).toLocaleString()} 
                icon={<Clock size={24} />} 
              />
            </div>

            {/* Heatmap Section */}
            <div className="bg-gray-50 border border-gray-100 rounded-2xl p-6 lg:p-8">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-xl font-bold text-black">Contribution Graph</h2>
              </div>
              <Heatmap data={data.heatmapData || {}} type={activeTab} />
            </div>

            {/* Achievements Section */}
            <div className="bg-gray-50 border border-gray-100 rounded-2xl p-6 lg:p-8">
              <h2 className="text-xl font-bold mb-6 flex items-center gap-2 text-black">
                <Award className="text-yellow-400" />
                Achievements
              </h2>
              {data.achievements && data.achievements.length > 0 ? (
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
                  {data.achievements.map((ach) => (
                    <AchievementBadge key={ach} type={ach} isReader={activeTab === 'reader'} />
                  ))}
                </div>
              ) : (
                <div className="text-center py-8 text-gray-500 bg-gray-100 rounded-xl border border-gray-200">
                  <Award size={40} className="mx-auto mb-3 opacity-20" />
                  <p>Check back as you dive into your {activeTab} journey to unlock achievements!</p>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ActivityDashboard;