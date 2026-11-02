import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AppShell } from './app/AppShell'
import { LoginPage } from './features/auth/LoginPage'
import { useAuth } from './features/auth/useAuth'
import { LandingPage } from './features/public/LandingPage'
import { PublicQuotePage } from './features/public/PublicQuotePage'
import { PartnerPublicPage } from './features/public/PartnerPublicPage'
import { PartnerPortalPage } from './features/parceiros/PartnerPortalPage'

const queryClient = new QueryClient()

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { meQuery } = useAuth()
  if (meQuery.isLoading) {
    return <p style={{ color: '#e2e8f0', padding: '2rem' }}>Carregando…</p>
  }
  if (meQuery.isError) {
    return <Navigate to="/login" replace />
  }
  return children
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/solicitar-viagem" element={<PublicQuotePage />} />
          <Route path="/p/:slug" element={<PartnerPublicPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <AppShell />
              </ProtectedRoute>
            }
          />
          <Route
            path="/solicitacoes"
            element={
              <ProtectedRoute>
                <AppShell />
              </ProtectedRoute>
            }
          />
          <Route
            path="/cotacoes"
            element={
              <ProtectedRoute>
                <AppShell />
              </ProtectedRoute>
            }
          />
          <Route
            path="/operacoes"
            element={
              <ProtectedRoute>
                <AppShell />
              </ProtectedRoute>
            }
          />
          <Route
            path="/parceiros"
            element={
              <ProtectedRoute>
                <AppShell />
              </ProtectedRoute>
            }
          />
          <Route
            path="/parceiro"
            element={
              <ProtectedRoute>
                <PartnerPortalPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/promocoes"
            element={
              <ProtectedRoute>
                <AppShell />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
