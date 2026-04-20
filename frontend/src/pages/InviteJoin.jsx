import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';

const InviteJoin = () => {
  const { code } = useParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState('loading'); // loading, success, error
  const [community, setCommunity] = useState(null);
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    const joinViaCommunityInvite = async () => {
      try {
        const res = await api.post(`/communities/join-via-invite/${code}`);
        setCommunity(res.data);
        setStatus('success');
      } catch (err) {
        setErrorMsg(err.response?.data?.message || err.message || 'Invalid or expired invite link');
        setStatus('error');
      }
    };
    joinViaCommunityInvite();
  }, [code]);

  if (status === 'loading') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-4 border-indigo-500 border-t-transparent mx-auto mb-4"></div>
          <p className="text-lg font-semibold text-gray-600">Joining community...</p>
        </div>
      </div>
    );
  }

  if (status === 'error') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="bg-white rounded-2xl p-8 shadow-md border border-red-100 max-w-md text-center">
          <h2 className="text-2xl font-bold text-red-600 mb-4">Unable to Join</h2>
          <p className="text-gray-600 mb-6">{errorMsg}</p>
          <button onClick={() => navigate('/communities')} className="bg-indigo-600 text-white px-6 py-3 rounded-xl font-bold">
            Browse Communities
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="bg-white rounded-2xl p-8 shadow-md border border-green-100 max-w-md text-center">
        <div className="text-5xl mb-4">🎉</div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Welcome!</h2>
        <p className="text-gray-600 mb-6">You have successfully joined <strong>{community?.name}</strong>!</p>
        <button onClick={() => navigate(`/c/${community?.id}`)} className="bg-indigo-600 text-white px-6 py-3 rounded-xl font-bold shadow-lg shadow-indigo-500/30 hover:-translate-y-0.5 transition-all">
          Enter Community
        </button>
      </div>
    </div>
  );
};

export default InviteJoin;
