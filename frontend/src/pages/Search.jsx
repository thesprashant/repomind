import React, { useState } from 'react';
import { Search as SearchIcon, Loader2, FileCode, Activity } from 'lucide-react';

const Search = () => {
  const [query, setQuery] = useState('');
  const [repo, setRepo] = useState('spring-projects/spring-boot');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!query.trim() || !repo.trim()) return;

    setLoading(true);
    setError(null);
    setResults([]);

    try {
      const API_BASE = import.meta.env.VITE_API_BASE_URL || '';
      const response = await fetch(`${API_BASE}/api/repomind/search`, {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'ngrok-skip-browser-warning': 'true'
        },
        body: JSON.stringify({ query, repo, top_k: 5 }),
      });

      if (!response.ok) {
        throw new Error('Search failed. Ensure your Spring Boot backend is running.');
      }

      const data = await response.json();
      setResults(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-4xl mx-auto mt-12 fade-in pb-20">
      <div className="text-center mb-10">
        <h1 className="text-5xl font-extrabold text-white mb-6 tracking-tight">
          Semantic Code <span className="text-transparent bg-clip-text bg-gradient-to-r from-primary to-secondary">Search</span>
        </h1>
        <p className="text-slate-400 text-lg">Ask natural language questions about your codebase.</p>
      </div>

      <form onSubmit={handleSearch} className="glass-panel p-4 rounded-3xl mb-12 flex flex-col md:flex-row gap-4">
        <div className="flex-1">
          <label className="block text-xs font-semibold text-slate-400 mb-2 ml-2 uppercase tracking-wider">Repository</label>
          <input 
            type="text" 
            value={repo}
            onChange={(e) => setRepo(e.target.value)}
            placeholder="owner/repo"
            className="w-full bg-dark-900/50 border border-white/10 rounded-2xl px-5 py-4 text-white focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all placeholder:text-slate-600"
          />
        </div>
        <div className="flex-[2]">
          <label className="block text-xs font-semibold text-slate-400 mb-2 ml-2 uppercase tracking-wider">Question</label>
          <div className="relative">
            <input 
              type="text" 
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="e.g., What is the license for this repository?"
              className="w-full bg-dark-900/50 border border-white/10 rounded-2xl pl-5 pr-14 py-4 text-white focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all placeholder:text-slate-600"
            />
            <button 
              type="submit" 
              disabled={loading}
              className="absolute right-2 top-2 bottom-2 aspect-square bg-gradient-to-br from-primary to-secondary rounded-xl text-white flex items-center justify-center hover:opacity-90 transition-opacity disabled:opacity-50"
            >
              {loading ? <Loader2 className="w-5 h-5 animate-spin" /> : <SearchIcon className="w-5 h-5" />}
            </button>
          </div>
        </div>
      </form>

      {error && (
        <div className="bg-red-500/10 border border-red-500/20 text-red-400 px-6 py-4 rounded-2xl mb-8 text-center font-medium">
          {error}
        </div>
      )}

      <div className="space-y-6">
        {results.map((result, idx) => (
          <div key={idx} className="glass-panel rounded-2xl overflow-hidden fade-in" style={{ animationDelay: `${idx * 0.1}s` }}>
            <div className="bg-white/5 border-b border-white/5 px-6 py-4 flex items-center justify-between">
              <div className="flex items-center gap-3 text-slate-300 font-medium text-sm">
                <FileCode className="w-4 h-4 text-primary" />
                {result.file_path}
              </div>
              <div className="flex items-center gap-1.5 px-3 py-1 bg-primary/10 border border-primary/20 rounded-full text-xs font-bold text-primary">
                <Activity className="w-3 h-3" />
                {(result.similarity * 100).toFixed(1)}% Match
              </div>
            </div>
            <div className="p-6 overflow-x-auto bg-[#0d1117]">
              <pre className="text-sm text-slate-300 font-mono leading-relaxed">
                <code>{result.content}</code>
              </pre>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Search;
