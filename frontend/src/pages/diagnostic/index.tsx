import React from "react";
import { Routes, Route } from "react-router-dom";
import DiagnosticReportList from "./DiagnosticReportList";
import DiagnosticRuleList from "./DiagnosticRuleList";

const DiagnosticPage: React.FC = () => {
  return (
    <Routes>
      <Route path="reports" element={<DiagnosticReportList />} />
      <Route path="rules" element={<DiagnosticRuleList />} />
      <Route path="/" element={<DiagnosticReportList />} />
    </Routes>
  );
};

export default DiagnosticPage;
