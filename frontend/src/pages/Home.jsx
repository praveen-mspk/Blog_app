import React, { useState, useEffect, useCallback } from 'react';
import api from '../services/api';
import BlogCard from '../components/BlogCard';
import { Filter, Grid, List as ListIcon, Search } from 'lucide-react';

const Home = () => {
  const [posts, setPosts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [viewMode, setViewMode] = useState('list');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [selectedMonth, setSelectedMonth] = useState('');
  const [selectedYear, setSelectedYear] = useState('');

  const fetchPosts = useCallback(async () => {
    try {
      let url = '/posts?page=0&size=20';
      if (selectedCategory) url += `&categoryId=${selectedCategory}`;
      if (selectedMonth) url += `&month=${selectedMonth}`;
      if (selectedYear) url += `&year=${selectedYear}`;
      
      const response = await api.get(url);
      setPosts(Array.isArray(response.data?.content) ? response.data.content : []);
    } catch (error) {
      console.error("Error fetching posts:", error);
      setPosts([]);
    } finally {
      setLoading(false);
    }
  }, [selectedCategory, selectedMonth, selectedYear]);

  const fetchCategories = useCallback(async () => {
    try {
      const response = await api.get('/categories/main');
      setCategories(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error("Error fetching categories:", error);
      setCategories([]);
    }
  }, []);

  useEffect(() => {
    fetchPosts();
    fetchCategories();
  }, [fetchPosts, fetchCategories, selectedCategory, selectedMonth, selectedYear]);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) {
      fetchPosts();
      return;
    }
    setLoading(true);
    try {
      const response = await api.get(`/posts/search?query=${searchQuery}`);
      setPosts(Array.isArray(response.data?.content) ? response.data.content : []);
    } catch (error) {
      console.error("Error searching posts:", error);
      setPosts([]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-6 py-12">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-16">
        {/* Main Feed */}
        <div className="lg:col-span-8">
          <div className="flex items-center justify-between mb-10 border-b border-gray-100 pb-4">
            <h1 className="text-3xl font-bold font-sans tracking-tight">Latest Stories</h1>
            <div className="flex items-center space-x-4">
              <button 
                onClick={() => setViewMode('list')}
                className={`p-1.5 rounded ${viewMode === 'list' ? 'bg-gray-100 text-black' : 'text-gray-400'}`}
              >
                <ListIcon className="w-5 h-5" />
              </button>
              <button 
                onClick={() => setViewMode('grid')}
                className={`p-1.5 rounded ${viewMode === 'grid' ? 'bg-gray-100 text-black' : 'text-gray-400'}`}
              >
                <Grid className="w-5 h-5" />
              </button>
            </div>
          </div>

          {loading ? (
            <div className="space-y-12">
              {[1, 2, 3].map(i => (
                <div key={i} className="animate-pulse flex flex-col md:flex-row gap-8">
                  <div className="flex-1 space-y-4">
                    <div className="h-4 bg-gray-100 rounded w-1/4"></div>
                    <div className="h-8 bg-gray-100 rounded w-3/4"></div>
                    <div className="h-20 bg-gray-100 rounded w-full"></div>
                  </div>
                  <div className="md:w-52 h-36 bg-gray-100 rounded-md"></div>
                </div>
              ))}
            </div>
          ) : posts.length > 0 ? (
            <div className={viewMode === 'grid' ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-8' : 'space-y-4'}>
              {Array.isArray(posts) && posts.map(post => (
                <BlogCard key={post.id} post={post} viewMode={viewMode} />
              ))}
            </div>
          ) : (
            <div className="text-center py-20">
              <p className="text-gray-500 font-serif italic text-lg">No stories found. Try a different search or filter.</p>
            </div>
          )}
        </div>

        {/* Sidebar */}
        <aside className="lg:col-span-4 space-y-12">
          {/* Search Box */}
          <section>
            <form onSubmit={handleSearch} className="relative">
              <input
                type="text"
                placeholder="Search BlogHub"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2 bg-gray-50 border-none rounded-full text-sm focus:ring-1 focus:ring-black"
              />
              <Search className="w-4 h-4 text-gray-400 absolute left-3.5 top-2.5" />
            </form>
          </section>

          {/* Categories */}
          <section>
            <h3 className="text-sm font-bold uppercase tracking-wider mb-6 text-gray-900 flex items-center">
              <Filter className="w-4 h-4 mr-2" />
              Topics for you
            </h3>
            <div className="flex flex-wrap gap-2">
              {Array.isArray(categories) && categories.map(cat => (
                <button
                  key={cat.id}
                  onClick={() => setSelectedCategory(cat.id)}
                  className={`px-4 py-2 rounded-full text-sm transition-colors ${
                    selectedCategory === cat.id 
                    ? 'bg-black text-white' 
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  }`}
                >
                  {cat.name}
                </button>
              ))}
            </div>
          </section>

          {/* Date Filters */}
          <section>
            <h3 className="text-sm font-bold uppercase tracking-wider mb-6 text-gray-900 flex items-center">
              <Filter className="w-4 h-4 mr-2" />
              Filter by Date
            </h3>
            <div className="flex gap-4">
              <select 
                className="flex-1 bg-gray-50 border-none rounded-lg text-sm focus:ring-1 focus:ring-black"
                value={selectedMonth}
                onChange={(e) => setSelectedMonth(e.target.value)}
              >
                <option value="">Month</option>
                {['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'].map((m, i) => (
                  <option key={m} value={i + 1}>{m}</option>
                ))}
              </select>
              <select 
                className="flex-1 bg-gray-50 border-none rounded-lg text-sm focus:ring-1 focus:ring-black"
                value={selectedYear}
                onChange={(e) => setSelectedYear(e.target.value)}
              >
                <option value="">Year</option>
                {[2024, 2025, 2026].map(y => (
                  <option key={y} value={y}>{y}</option>
                ))}
              </select>
            </div>
            {(selectedCategory || selectedMonth || selectedYear) && (
              <button 
                onClick={() => {
                  setSelectedCategory(null);
                  setSelectedMonth('');
                  setSelectedYear('');
                }}
                className="mt-4 text-xs font-bold text-red-500 hover:text-red-600 uppercase tracking-widest"
              >
                Clear all filters
              </button>
            )}
          </section>

          {/* Featured Posts (Static Placeholder for now) */}
          <section>
            <h3 className="text-sm font-bold uppercase tracking-wider mb-6 text-gray-900">Featured</h3>
            <div className="space-y-6">
              {Array.isArray(posts) && posts.slice(0, 3).map((post, i) => (
                <div key={post.id} className="flex gap-4 group">
                  <span className="text-3xl font-bold text-gray-100 group-hover:text-gray-200 transition-colors">
                    0{i + 1}
                  </span>
                  <div>
                    <h4 className="text-sm font-bold font-sans line-clamp-2 leading-snug group-hover:underline">
                      {post.title}
                    </h4>
                    <p className="text-xs text-gray-500 mt-1">{post.author?.name || 'Anonymous'}</p>
                  </div>
                </div>
              ))}
            </div>
          </section>
        </aside>
      </div>
    </div>
  );
};

export default Home;