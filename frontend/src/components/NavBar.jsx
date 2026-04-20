import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LogOut, PenSquare, Search, User as UserIcon } from 'lucide-react';

const Navbar = () => {
  const { user, logout } = useAuth();

  return (
    <nav className="sticky top-0 z-50 bg-white border-b border-gray-100 px-6 py-3">
      <div className="max-w-7xl mx-auto flex items-center justify-between">
        <div className="flex items-center space-x-8">
          <Link to="/" className="text-2xl font-bold tracking-tighter text-black">
            BlogHub
          </Link>
          <div className="hidden md:flex items-center bg-gray-50 rounded-full px-4 py-1.5 border border-gray-100 group focus-within:bg-white focus-within:ring-2 focus-within:ring-gray-100 transition-all">
            <Search className="w-4 h-4 text-gray-400" />
            <input
              type="text"
              placeholder="Search"
              className="bg-transparent border-none focus:ring-0 text-sm ml-2 w-48"
            />
          </div>
        </div>

        <div className="flex items-center space-x-6">
          <Link to="/" className="text-sm font-medium text-gray-600 hover:text-black transition-colors">
            Home
          </Link>
          <Link to="/communities" className="text-sm font-medium text-gray-600 hover:text-black transition-colors">
            Communities
          </Link>
          <Link to="/analytics" className="text-sm font-medium text-gray-600 hover:text-black transition-colors">
            Analytics
          </Link>
          <Link to="/membership" className={`text-sm font-bold transition-colors ${user?.isMember ? 'text-indigo-600' : 'text-gray-600 hover:text-black'}`}>
            {user?.isMember ? 'Membership' : 'Upgrade'}
          </Link>
          
          {user ? (
            <>
              <Link 
                to="/create" 
                className="flex items-center space-x-2 text-sm font-medium text-gray-600 hover:text-black"
              >
                <PenSquare className="w-4 h-4" />
                <span>Write</span>
              </Link>
              <div className="relative group">
                <button className="flex items-center space-x-1 focus:outline-none relative">
                  {user.profileImage ? (
                    <img src={user.profileImage} alt="" className="w-8 h-8 rounded-full object-cover" />
                  ) : (
                    <div className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center">
                      <UserIcon className="w-4 h-4 text-gray-500" />
                    </div>
                  )}
                  {user.isMember && (
                    <div className="absolute -top-1 -right-1 w-3 h-3 bg-indigo-600 border-2 border-white rounded-full" title="Member" />
                  )}
                </button>
                <div className="absolute right-0 mt-2 w-48 bg-white border border-gray-100 rounded-lg shadow-lg py-2 hidden group-hover:block animate-in fade-in slide-in-from-top-2 duration-200">
                  <div className="px-4 py-2 border-b border-gray-50 mb-2">
                    <p className="text-sm font-bold truncate">{user.name}</p>
                    <p className="text-xs text-gray-500 truncate">{user.email}</p>
                  </div>
                  <Link to="/profile" className="block px-4 py-2 text-sm text-gray-600 hover:bg-gray-50 hover:text-black">
                    Profile
                  </Link>
                  <Link to="/my-blogs" className="block px-4 py-2 text-sm text-gray-600 hover:bg-gray-50 hover:text-black">
                    My Blogs
                  </Link>
                  <Link to="/dashboard" className="block px-4 py-2 text-sm text-gray-600 hover:bg-gray-50 hover:text-black">
                    Activity Dashboard
                  </Link>
                  <button 
                    onClick={logout}
                    className="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 flex items-center space-x-2"
                  >
                    <LogOut className="w-4 h-4" />
                    <span>Sign Out</span>
                  </button>
                </div>
              </div>
            </>
          ) : (
            <>
              <Link to="/login" className="text-sm font-medium text-gray-600 hover:text-black">
                Sign In
              </Link>
              <Link 
                to="/register" 
                className="btn btn-primary text-sm px-6"
              >
                Get Started
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;