import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { Users, Plus } from 'lucide-react';

const CommunitiesHub = () => {
  const [communities, setCommunities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newCommunity, setNewCommunity] = useState({ name: '', description: '', type: 'OPEN' });

  useEffect(() => {
    const fetchCommunities = async () => {
      try {
        const response = await api.get('/communities');
        setCommunities(response.data);
      } catch (err) {
        console.error('Error fetching communities:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchCommunities();
  }, []);

  const handleCreateCommunity = async (e) => {
    e.preventDefault();
    try {
      const res = await api.post('/communities', newCommunity);
      setCommunities([res.data, ...communities]);
      setShowCreateModal(false);
      setNewCommunity({ name: '', description: '', type: 'OPEN' });
    } catch (err) {
      console.error(err);
      alert('Failed to create community');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 pt-24 pb-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto animate-fade-in">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-10 gap-4">
          <div>
            <h1 className="text-4xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 to-purple-600 tracking-tight">Communities Hub</h1>
            <p className="text-gray-500 mt-2 text-lg font-medium">Join discussions, share knowledge, and connect with like-minded people.</p>
          </div>
          <button onClick={() => setShowCreateModal(true)} className="bg-indigo-600 text-white px-6 py-3 flex items-center gap-2 rounded-xl font-bold shadow-lg shadow-indigo-500/30 hover:shadow-indigo-500/50 hover:-translate-y-0.5 transition-all">
            <Plus size={20} />
            Create Community
          </button>
        </div>

        {loading ? (
          <div className="flex justify-center py-20">
            <div className="animate-spin rounded-full h-12 w-12 border-4 border-indigo-500 border-t-transparent"></div>
          </div>
        ) : communities.length === 0 ? (
          <div className="text-center py-24 bg-white rounded-3xl border border-gray-100 shadow-sm">
            <Users size={64} className="mx-auto mb-6 text-gray-300" />
            <h3 className="text-2xl font-bold text-gray-900 mb-2">No Communities Yet</h3>
            <p className="text-gray-500 text-lg">Be the first to create an amazing space.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {communities.map(c => (
              <Link to={`/c/${c.id}`} key={c.id} className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 hover:shadow-xl hover:-translate-y-1 transition-all duration-300 cursor-pointer flex flex-col h-full">
                <div className="flex justify-between items-start mb-4">
                  <h3 className="text-xl font-bold text-gray-900">{c.name}</h3>
                  <span className="text-xs font-bold px-3 py-1 bg-indigo-50 text-indigo-600 rounded-full">{c.type}</span>
                </div>
                <p className="text-gray-600 text-sm mb-6 flex-grow">{c.description}</p>
                <div className="flex items-center justify-between border-t border-gray-50 pt-4 mt-auto">
                  <div className="flex items-center text-sm font-semibold text-gray-500 gap-2">
                    <Users size={18} className="text-indigo-400" />
                    <span>{c.memberCount} members</span>
                  </div>
                  <span className="text-sm font-semibold text-indigo-600 hover:text-indigo-700">Enter &rarr;</span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>

      {showCreateModal && (
        <div className="fixed inset-0 bg-black/40 z-50 flex justify-center items-center p-4 backdrop-blur-sm transition-all">
          <div className="bg-white rounded-2xl w-full max-w-md p-8 shadow-2xl relative animate-fade-in border border-gray-100">
            <button onClick={() => setShowCreateModal(false)} className="absolute top-5 right-5 text-gray-400 hover:text-gray-800 transition-colors">
              <Plus size={24} className="rotate-45" />
            </button>
            <h2 className="text-2xl font-black text-gray-900 mb-6 tracking-tight">Create Community</h2>
            <form onSubmit={handleCreateCommunity} className="space-y-5">
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-1.5">Community Name</label>
                <input required type="text" value={newCommunity.name} onChange={e => setNewCommunity({...newCommunity, name: e.target.value})} className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-indigo-500 transition-all font-medium" placeholder="e.g. React Developers" />
              </div>
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-1.5">Description</label>
                <textarea required value={newCommunity.description} onChange={e => setNewCommunity({...newCommunity, description: e.target.value})} className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-indigo-500 transition-all resize-none h-28 font-medium" placeholder="What is this community about?"></textarea>
              </div>
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-1.5">Privacy Setting</label>
                <select value={newCommunity.type} onChange={e => setNewCommunity({...newCommunity, type: e.target.value})} className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-indigo-500 transition-all font-medium cursor-pointer">
                  <option value="OPEN">Open (Anyone can join)</option>
                  <option value="MODERATED">Moderated (Requires approval)</option>
                  <option value="PRIVATE">Private (Invite only)</option>
                </select>
              </div>
              <button type="submit" className="w-full bg-indigo-600 text-white font-bold py-3.5 rounded-xl shadow-[0_4px_14px_0_rgb(79,70,229,0.39)] hover:shadow-[0_6px_20px_rgba(79,70,229,0.23)] hover:-translate-y-0.5 transition-all text-lg mt-4">
                Launch Space
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default CommunitiesHub;
