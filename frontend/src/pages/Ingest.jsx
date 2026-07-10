import React, { useState, useEffect } from 'react';
import { Database, Loader2, CheckCircle, GitBranch, AlertTriangle } from 'lucide-react';

const Ingest = () => {
  const [owner, setOwner] = useState('');
  const [repo, setRepo] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(null);
  const [error, setError] = useState(null);
  const [jobStatus, setJobStatus] = useState(null);

  const handleIngest = async (e) => {
    e.preventDefault();
    if (!owner.trim() || !repo.trim()) return;

    setLoading(true);
    setError(null);
    setSuccess(null);
    setJobStatus(null);

    try {
      const API_BASE = import.meta.env.VITE_API_BASE_URL || '';
      const response = await fetch(`${API_BASE}/api/repomind/ingest`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ owner, repo }),
      });

      if (!response.ok) {
        throw new Error('Ingestion request failed. Please check the repository details or your GitHub API token limit.');
      }

      const data = await response.json();
      setSuccess({
        message: data.status,
        jobId: data.jobId
      });
      // Do not clear owner/repo immediately so user knows what is processing
    } catch (err) {
      setError(err.message);
      setLoading(false);
    }
  };

  useEffect(() => {
    let intervalId;
    if (success && success.jobId) {
      intervalId = setInterval(async () => {
        try {
          const API_BASE = import.meta.env.VITE_API_BASE_URL || '';
          const res = await fetch(`${API_BASE}/api/repomind/ingest/status/${success.jobId}`);
          if (res.ok) {
            const data = await res.json();
            setJobStatus(data);
            
            if (data.status === 'COMPLETED' || data.status === 'FAILED') {
              clearInterval(intervalId);
              setLoading(false);
              if (data.status === 'COMPLETED') {
                 setOwner('');
                 setRepo('');
              }
            }
          }
        } catch (err) {
          console.error("Polling error:", err);
        }
      }, 1500); // Poll every 1.5s
    }

    return () => {
      if (intervalId) clearInterval(intervalId);
    };
  }, [success]);

  // Calculate percentage
  let progressPercentage = 0;
  if (jobStatus && jobStatus.totalFiles > 0) {
    progressPercentage = Math.round((jobStatus.processedFiles / jobStatus.totalFiles) * 100);
  }

  return (
    <div className="w-full max-w-3xl mx-auto mt-12 fade-in pb-20">
      <div className="text-center mb-10">
        <h1 className="text-5xl font-extrabold text-white mb-6 tracking-tight">
          Repository <span className="text-transparent bg-clip-text bg-gradient-to-r from-primary to-secondary">Ingestion</span>
        </h1>
        <p className="text-slate-400 text-lg">
          Process a GitHub repository through our local AI engine to enable semantic search.
        </p>
      </div>

      <div className="glass-panel p-8 rounded-3xl relative overflow-hidden">
        <GitBranch className="absolute -bottom-10 -right-10 w-64 h-64 text-white/5 pointer-events-none" />

        <form onSubmit={handleIngest} className="relative z-10 space-y-6">
          <div className="flex flex-col md:flex-row gap-6">
            <div className="flex-1">
              <label className="block text-xs font-semibold text-slate-400 mb-2 ml-2 uppercase tracking-wider">Owner / Organization</label>
              <input 
                type="text" 
                value={owner}
                onChange={(e) => setOwner(e.target.value)}
                placeholder="e.g., spring-projects"
                className="w-full bg-dark-900/50 border border-white/10 rounded-2xl px-5 py-4 text-white focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all placeholder:text-slate-600 disabled:opacity-50"
                required
                disabled={loading}
              />
            </div>
            <div className="flex-1">
              <label className="block text-xs font-semibold text-slate-400 mb-2 ml-2 uppercase tracking-wider">Repository Name</label>
              <input 
                type="text" 
                value={repo}
                onChange={(e) => setRepo(e.target.value)}
                placeholder="e.g., spring-boot"
                className="w-full bg-dark-900/50 border border-white/10 rounded-2xl px-5 py-4 text-white focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all placeholder:text-slate-600 disabled:opacity-50"
                required
                disabled={loading}
              />
            </div>
          </div>

          {!loading && !jobStatus && (
            <button 
              type="submit" 
              disabled={!owner.trim() || !repo.trim()}
              className="w-full py-4 bg-gradient-to-r from-primary to-secondary rounded-2xl text-white font-bold text-lg flex items-center justify-center gap-3 hover:opacity-90 transition-opacity shadow-lg shadow-primary/20 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Database className="w-6 h-6" />
              Start Ingestion
            </button>
          )}
        </form>

        {error && !loading && !jobStatus && (
          <div className="mt-8 bg-red-500/10 border border-red-500/20 text-red-400 px-6 py-4 rounded-2xl text-center font-medium fade-in">
            {error}
          </div>
        )}

        {/* Progress State */}
        {jobStatus && (jobStatus.status === 'INITIALIZING' || jobStatus.status === 'IN_PROGRESS') && (
          <div className="mt-8 bg-dark-900/60 border border-white/10 px-6 py-8 rounded-2xl text-center fade-in">
            <Loader2 className="w-10 h-10 text-primary animate-spin mx-auto mb-4" />
            <h3 className="text-xl font-bold text-white mb-2">Vectorizing Repository...</h3>
            <p className="text-slate-400 text-sm mb-6">Parsing markdown files and generating AI vectors</p>
            
            <div className="w-full bg-dark-800 rounded-full h-3 mb-2 overflow-hidden border border-white/5 relative">
              <div 
                className="bg-gradient-to-r from-primary to-secondary h-3 rounded-full transition-all duration-500 ease-out absolute left-0 top-0" 
                style={{ width: `${progressPercentage}%` }}
              ></div>
            </div>
            
            <div className="flex justify-between text-xs font-semibold text-slate-500 uppercase tracking-wider">
              <span>{progressPercentage}% Complete</span>
              <span>{jobStatus.processedFiles} / {jobStatus.totalFiles} Files</span>
            </div>
          </div>
        )}

        {/* Success State */}
        {jobStatus && jobStatus.status === 'COMPLETED' && (
          <div className="mt-8 bg-green-500/10 border border-green-500/20 px-6 py-8 rounded-2xl text-center fade-in">
            <CheckCircle className="w-12 h-12 text-green-400 mx-auto mb-4" />
            <h3 className="text-xl font-bold text-green-400 mb-2">Ingestion Completed!</h3>
            <p className="text-green-300/80 text-sm mb-6">
              Successfully processed {jobStatus.totalFiles} files into the vector database.
            </p>
            <div className="inline-block bg-dark-900/80 px-4 py-2 rounded-xl text-xs text-slate-400 font-mono border border-white/5 shadow-inner">
              Job ID: {success?.jobId}
            </div>
          </div>
        )}

        {/* Failed State */}
        {jobStatus && jobStatus.status === 'FAILED' && (
          <div className="mt-8 bg-red-500/10 border border-red-500/20 px-6 py-8 rounded-2xl text-center fade-in">
            <AlertTriangle className="w-12 h-12 text-red-400 mx-auto mb-4" />
            <h3 className="text-xl font-bold text-red-400 mb-2">Ingestion Failed</h3>
            <p className="text-red-300/80 text-sm">
              An error occurred while parsing the repository. Please verify the repository name is correct and try again.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Ingest;
