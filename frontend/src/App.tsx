import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Layout from "./components/Layout";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import InvoiceList from "./components/InvoiceList";
import CreateInvoice from "./components/CreateInvoice";
import InvoiceDetail from "./pages/InvoiceDetail";
import ProtectedRoute from "./components/ProtectedRoute";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/invoices" element={<InvoiceList />} />
          <Route path="/invoices/new" element={<CreateInvoice />} />
          <Route path="/invoices/:id" element={<InvoiceDetail />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
