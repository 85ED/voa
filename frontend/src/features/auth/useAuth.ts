import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { fetchMe, login, type User } from '../../shared/api/client'

export function useAuth() {
  const queryClient = useQueryClient()
  const meQuery = useQuery({
    queryKey: ['auth', 'me'],
    queryFn: fetchMe,
    retry: false,
  })

  const loginMutation = useMutation({
    mutationFn: ({ email, password }: { email: string; password: string }) =>
      login(email, password),
    onSuccess: (data: { user: User; expiresIn: number }) => {
      queryClient.setQueryData(['auth', 'me'], data.user)
    },
  })

  return { meQuery, loginMutation }
}
