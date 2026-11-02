import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AppShell } from './app/AppShell'
import { LoginPage } from './features/auth/LoginPage'
import { ForgotPasswordPage } from './features/auth/ForgotPasswordPage'
import { ResetPasswordPage } from './features/auth/ResetPasswordPage'
import { useAuth } from './features/auth/useAuth'
import { LandingPage } from './features/public/LandingPage'
import { PublicQuotePage } from './features/public/PublicQuotePage'
import { PartnerPublicPage } from './features/public/PartnerPublicPage'
import { PartnerPortalPage } from './features/parceiros/PartnerPortalPage'

const queryClient = new QueryClient()

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { meQuery } = useAuth()
  if (meQuery.isLoading) {
    return (
      <div style={{ padding: '2rem', color: 'var(--color-text-muted)', fontFamily: 'var(--font-sans)' }}>
        Carregando sessão…
      </div>
    )
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
          <Route path="/login/esqueci-senha" element={<ForgotPasswordPage />} />
          <Route path="/login/redefinir-senha" element={<ResetPasswordPage />} />
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
