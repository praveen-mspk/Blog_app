import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useSocket } from '../context/SocketContext';
import { MessageSquare, Users, Activity, BarChart2, Copy, Check, UserPlus, Clock, Shield, Trash2 } from 'lucide-react';

const CommunityView = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { subscribeToCommunity } = useSocket();
  const [community, setCommunity] = useState(null);
  const [discussions, setDiscussions] = useState([]);
  const [analytics, setAnalytics] = useState(null);
  const [newThreadTitle, setNewThreadTitle] = useState('');
  const [newThreadContent, setNewThreadContent] = useState('');
  const [loading, setLoading] = useState(true);

  // Moderated: pending requests
  const [pendingRequests, setPendingRequests] = useState([]);

  // Private: invite link
  const [inviteCode, setInviteCode] = useState('');
  const [copied, setCopied] = useState(false);

  // Members list (for creator)
  const [members, setMembers] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [commRes, discRes, statRes] = await Promise.all([
          api.get(`/communities/${id}`),
          api.get(`/discussions/community/${id}`),
          api.get(`/communities/${id}/analytics`)
        ]);
        setCommunity(commRes.data);
        setDiscussions(discRes.data);
        setAnalytics(statRes.data);

        // If creator of MODERATED, fetch pending requests
        if (commRes.data.isCreator && commRes.data.type === 'MODERATED') {
          const reqRes = await api.get(`/communities/${id}/pending-requests`);
          setPendingRequests(reqRes.data);
        }

        // If creator, fetch members list
        if (commRes.data.isCreator) {
          const membersRes = await api.get(`/communities/${id}/members`);
          setMembers(membersRes.data);
        }
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();

    const sub = subscribeToCommunity(id, (msg) => {
      if (msg.type === 'NEW_DISCUSSION') {
        setDiscussions(prev => [msg.payload, ...prev]);
      }
    });

    return () => { if (sub) sub.unsubscribe(); };
  }, [id, subscribeToCommunity]);

  const handleCreateThread = async (e) => {
    e.preventDefault();
    if (!newThreadTitle.trim() || !newThreadContent.trim()) return;
    try {
      const res = await api.post(`/discussions/community/${id}`, {
        title: newThreadTitle,
        content: newThreadContent,
        type: 'DISCUSSION'
      });
      setDiscussions([res.data, ...discussions]);
      setNewThreadTitle('');
      setNewThreadContent('');
    } catch (err) {
      console.error(err);
    }
  };

  // ─── OPEN join ───
  const joinOpen = async () => {
    try {
      await api.post(`/communities/${id}/join`);
      setCommunity(prev => ({ ...prev, isMember: true, memberCount: prev.memberCount + 1 }));
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to join');
    }
  };

  // ─── MODERATED request ───
  const requestJoin = async () => {
    try {
      await api.post(`/communities/${id}/request-join`);
      setCommunity(prev => ({ ...prev, pendingRequest: true }));
    } catch (err) {
      alert(err.response?.data?.message || err.message);
    }
  };

  const approveRequest = async (requestId) => {
    try {
      await api.post(`/communities/requests/${requestId}/approve`);
      setPendingRequests(prev => prev.filter(r => r.requestId !== requestId));
    } catch (err) {
      alert('Failed to approve');
    }
  };

  const rejectRequest = async (requestId) => {
    try {
      await api.post(`/communities/requests/${requestId}/reject`);
      setPendingRequests(prev => prev.filter(r => r.requestId !== requestId));
    } catch (err) {
      alert('Failed to reject');
    }
  };

  // ─── PRIVATE invite ───
  const generateInvite = async () => {
    try {
      const res = await api.post(`/communities/${id}/generate-invite`);
      setInviteCode(res.data.inviteCode);
    } catch (err) {
      alert('Failed to generate invite');
    }
  };

  const copyInviteLink = () => {
    const link = `${window.location.origin}/invite/${inviteCode}`;
    navigator.clipboard.writeText(link);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  // ─── Creator: remove member ───
  const removeMember = async (memberId) => {
    if (!window.confirm('Are you sure you want to remove this member?')) return;
    try {
      await api.delete(`/communities/${id}/members/${memberId}`);
      setMembers(prev => prev.filter(m => m.memberId !== memberId));
      setCommunity(prev => ({ ...prev, memberCount: prev.memberCount - 1 }));
    } catch (err) {
      alert('Failed to remove member');
    }
  };

  if (loading) return <div className="p-20 text-center text-indigo-500 animate-pulse font-bold text-xl">Loading Community...</div>;
  if (!community) return <div className="p-20 text-center">Community not found.</div>;

  // Determine the join button component based on community type and membership
  const renderJoinButton = () => {
    if (community.isMember) {
      return <span className="bg-green-100 text-green-700 px-5 py-2.5 rounded-xl font-bold flex items-center gap-2"><Check size={18}/> Member</span>;
    }

    switch (community.type) {
      case 'OPEN':
        return (
          <button onClick={joinOpen} className="bg-indigo-600 text-white px-5 py-2.5 rounded-xl font-bold shadow-lg shadow-indigo-500/30 hover:shadow-indigo-500/50 hover:-translate-y-0.5 transition-all flex items-center gap-2">
            <UserPlus size={18}/> Join Community
          </button>
        );
      case 'MODERATED':
        if (community.pendingRequest) {
          return <span className="bg-amber-100 text-amber-700 px-5 py-2.5 rounded-xl font-bold flex items-center gap-2"><Clock size={18}/> Request Pending</span>;
        }
        return (
          <button onClick={requestJoin} className="bg-amber-500 text-white px-5 py-2.5 rounded-xl font-bold shadow-lg shadow-amber-500/30 hover:shadow-amber-500/50 hover:-translate-y-0.5 transition-all flex items-center gap-2">
            <Shield size={18}/> Request to Join
          </button>
        );
      case 'PRIVATE':
        return <span className="bg-gray-200 text-gray-600 px-5 py-2.5 rounded-xl font-bold flex items-center gap-2"><Shield size={18}/> Invite Only</span>;
      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 pt-24 pb-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto flex flex-col lg:flex-row gap-8 animate-fade-in">

        {/* Main Content Feed */}
        <div className="w-full lg:w-2/3 space-y-6">
          <div className="bg-white rounded-2xl p-8 shadow-sm border border-gray-100 mb-8 transform transition hover:shadow-md">
            <div className="flex justify-between items-start">
              <div>
                <div className="flex items-center gap-3 mb-2">
                  <h1 className="text-3xl font-extrabold text-gray-900">{community.name}</h1>
                  <span className="bg-indigo-100 text-indigo-700 px-3 py-1 rounded-full text-xs font-bold">{community.type}</span>
                </div>
                <p className="text-gray-600 mt-2 text-lg">{community.description}</p>
                <p className="text-sm text-gray-400 mt-4">Created by {community.creator?.name}</p>
              </div>
              {renderJoinButton()}
            </div>
          </div>

          {/* Creator: Manage Pending Requests (MODERATED) */}
          {community.isCreator && community.type === 'MODERATED' && pendingRequests.length > 0 && (
            <div className="bg-amber-50 rounded-2xl p-6 border border-amber-200">
              <h3 className="text-lg font-bold text-amber-800 mb-4 flex items-center gap-2"><Clock size={20}/> Pending Join Requests ({pendingRequests.length})</h3>
              <div className="space-y-3">
                {pendingRequests.map(r => (
                  <div key={r.requestId} className="flex items-center justify-between bg-white p-4 rounded-xl border border-amber-100">
                    <div>
                      <p className="font-bold text-gray-900">{r.userName}</p>
                      <p className="text-sm text-gray-500">{r.userEmail} • {new Date(r.requestedAt).toLocaleDateString()}</p>
                    </div>
                    <div className="flex gap-2">
                      <button onClick={() => approveRequest(r.requestId)} className="bg-green-600 text-white px-4 py-2 rounded-lg font-bold text-sm hover:-translate-y-0.5 transition-all shadow-md">Approve</button>
                      <button onClick={() => rejectRequest(r.requestId)} className="bg-red-500 text-white px-4 py-2 rounded-lg font-bold text-sm hover:-translate-y-0.5 transition-all shadow-md">Reject</button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Creator: Generate Invite Link (PRIVATE) */}
          {community.isCreator && community.type === 'PRIVATE' && (
            <div className="bg-purple-50 rounded-2xl p-6 border border-purple-200">
              <h3 className="text-lg font-bold text-purple-800 mb-4 flex items-center gap-2"><UserPlus size={20}/> Invite Members</h3>
              {inviteCode ? (
                <div className="flex items-center gap-3">
                  <input readOnly value={`${window.location.origin}/invite/${inviteCode}`} className="flex-grow px-4 py-3 bg-white border border-purple-200 rounded-xl font-mono text-sm" />
                  <button onClick={copyInviteLink} className="bg-purple-600 text-white px-5 py-3 rounded-xl font-bold flex items-center gap-2 hover:-translate-y-0.5 transition-all shadow-md">
                    {copied ? <><Check size={16}/> Copied!</> : <><Copy size={16}/> Copy Link</>}
                  </button>
                </div>
              ) : (
                <button onClick={generateInvite} className="bg-purple-600 text-white px-6 py-3 rounded-xl font-bold shadow-lg shadow-purple-500/30 hover:shadow-purple-500/50 hover:-translate-y-0.5 transition-all">
                  Generate Invite Link
                </button>
              )}
            </div>
          )}

          {/* Discussion creation - only for members */}
          {community.isMember && (
            <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-200">
              <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
                <MessageSquare size={20} className="text-indigo-500" /> Start a Discussion
              </h3>
              <form onSubmit={handleCreateThread} className="space-y-4">
                <input type="text" placeholder="Thread Title" value={newThreadTitle} onChange={e => setNewThreadTitle(e.target.value)} className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition bg-gray-50 focus:bg-white" />
                <textarea placeholder="What's on your mind? Share with the community..." value={newThreadContent} onChange={e => setNewThreadContent(e.target.value)} className="w-full px-4 py-3 border border-gray-200 rounded-xl h-28 focus:ring-2 focus:ring-indigo-500 outline-none transition bg-gray-50 focus:bg-white resize-none"></textarea>
                <div className="flex justify-end">
                  <button type="submit" className="bg-gray-900 text-white px-6 py-2.5 rounded-xl font-bold shadow-md hover:shadow-lg hover:-translate-y-0.5 transition-all">Post Discussion</button>
                </div>
              </form>
            </div>
          )}

          {/* Not a member notice */}
          {!community.isMember && (
            <div className="bg-gray-100 rounded-2xl p-6 text-center text-gray-500 font-medium">
              You must be a member to start or participate in discussions.
            </div>
          )}

          <div className="space-y-4 mt-8">
            {discussions.length === 0 ? (
              <div className="text-center py-10 bg-white rounded-2xl border border-gray-100"><p className="text-gray-500">No discussions yet. Be the first!</p></div>
            ) : discussions.map(d => (
              <div key={d.id} onClick={() => navigate(`/d/${d.id}`)} className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 hover:shadow-xl hover:border-indigo-100 cursor-pointer transition-all duration-300">
                <div className="flex gap-2 mb-3">
                  <span className="text-xs font-bold px-2 py-1 bg-blue-50 text-blue-600 rounded-md border border-blue-100">{d.type}</span>
                  <span className="text-xs font-bold px-2 py-1 bg-gray-50 text-gray-600 rounded-md border border-gray-100">{new Date(d.createdAt).toLocaleDateString()}</span>
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-2">{d.title}</h3>
                <p className="text-gray-600 text-sm line-clamp-2 mb-4 leading-relaxed">{d.content}</p>
                <div className="flex items-center gap-5 text-sm text-gray-500 font-medium pt-4 border-t border-gray-50">
                  <div className="flex items-center gap-1.5 hover:text-indigo-600 transition-colors"><MessageSquare size={16} /> {d.repliesCount} replies</div>
                  <div className="flex items-center gap-1.5 text-emerald-600 font-bold">↑ {d.upvotes}</div>
                  <div className="flex items-center gap-1.5">👁 {d.views}</div>
                  <div className="ml-auto font-semibold text-gray-900">By {d.author.name}</div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Sidebar Analytics */}
        <div className="w-full lg:w-1/3">
          <div className="bg-white rounded-3xl p-6 shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-gray-100 sticky top-24">
            <h3 className="text-xl font-bold text-gray-900 mb-6 flex items-center gap-2 pb-4 border-b border-gray-100">
              <BarChart2 className="text-indigo-500" /> Community Stats
            </h3>
            {analytics && (
              <div className="space-y-4">
                <div className="flex justify-between items-center p-4 bg-gray-50 rounded-2xl transition hover:bg-gray-100">
                  <span className="text-gray-600 font-semibold flex items-center gap-2"><Users size={18} /> Members</span>
                  <span className="text-2xl font-black text-gray-900">{analytics.totalMembers}</span>
                </div>
                <div className="flex justify-between items-center p-4 bg-emerald-50 rounded-2xl transition hover:bg-emerald-100">
                  <span className="text-emerald-700 font-semibold flex items-center gap-2"><Activity size={18} /> Active Today</span>
                  <span className="text-2xl font-black text-emerald-700">{analytics.activeToday}</span>
                </div>
                <div className="flex justify-between items-center p-4 bg-purple-50 rounded-2xl transition hover:bg-purple-100">
                  <span className="text-purple-700 font-semibold flex items-center gap-2"><MessageSquare size={18} /> Topics</span>
                  <span className="text-2xl font-black text-purple-700">{analytics.totalDiscussions}</span>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Members Panel for Creator - below the main content */}
      {community.isCreator && members.length > 0 && (
        <div className="max-w-7xl mx-auto mt-8 animate-fade-in">
          <div className="bg-white rounded-3xl p-6 shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-gray-100">
            <h3 className="text-xl font-bold text-gray-900 mb-6 flex items-center gap-2 pb-4 border-b border-gray-100">
              <Users className="text-indigo-500" /> Community Members ({members.length})
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
              {members.map(m => (
                <div key={m.memberId} className="flex items-center justify-between p-4 bg-gray-50 rounded-2xl hover:bg-gray-100 transition-colors">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-indigo-500 to-purple-500 flex items-center justify-center text-white font-bold text-sm shadow-md">
                      {m.userName?.charAt(0) || 'U'}
                    </div>
                    <div>
                      <p className="font-bold text-gray-900 text-sm">{m.userName}</p>
                      <p className="text-xs text-gray-500">{m.role === 'CREATOR' ? '👑 Creator' : 'Member'}</p>
                    </div>
                  </div>
                  {m.role !== 'CREATOR' && (
                    <button onClick={() => removeMember(m.memberId)} className="text-red-400 hover:text-red-600 hover:bg-red-50 p-2 rounded-lg transition-all" title="Remove member">
                      <Trash2 size={16} />
                    </button>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default CommunityView;
