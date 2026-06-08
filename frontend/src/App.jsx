import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';

function App() {
  return (
    <Router>
      <div className="min-h-screen flex flex-col relative overflow-hidden">
        {/* Background ambient glow */}
        <div className="absolute top-[-20%] left-[-10%] w-[50%] h-[50%] bg-primary/20 blur-[120px] rounded-full pointer-events-none" />
        <div className="absolute bottom-[-20%] right-[-10%] w-[50%] h-[50%] bg-secondary/20 blur-[120px] rounded-full pointer-events-none" />

        <Navbar />
        
        <main className="flex-grow pt-28 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto w-full relative z-10">
          <Routes>
            <Route path="/" element={
              <div className="text-center mt-20 fade-in">
                <h1 className="text-5xl font-extrabold text-white mb-6 tracking-tight">
                  Search your codebase <br/><span className="text-transparent bg-clip-text bg-gradient-to-r from-primary to-secondary">intelligently</span>
                </h1>
                <p className="text-slate-400 text-xl max-w-2xl mx-auto">Semantic Search UI will be built here on Day 9.</p>
              </div>
            } />
            <Route path="/ingest" element={
              <div className="text-center mt-20 fade-in">
                <h1 className="text-5xl font-extrabold text-white mb-6 tracking-tight">
                  Ingest a Repository
                </h1>
                <p className="text-slate-400 text-xl max-w-2xl mx-auto">Ingestion UI will be built here on Day 10.</p>
              </div>
            } />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
