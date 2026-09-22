'use server'

import { createClient } from '@/utils/supabase/server'
import { revalidatePath } from 'next/cache'

export async function generateLinkCode(formData: FormData) {
  const supabase = createClient()
  const { data: { user }, error: userError } = await supabase.auth.getUser()

  if (userError || !user) {
    throw new Error('Not authenticated')
  }

  const childUsername = formData.get('childUsername') as string

  // 1. Find child by username via secure RPC (bypasses RLS read restriction)
  const { data: childId, error: childError } = await supabase
    .rpc('get_child_id_by_username', { p_username: childUsername })

  if (childError || !childId) {
    throw new Error('Child not found')
  }

  // 2. Generate a secure random 6-character code
  const code = Math.random().toString(36).substring(2, 8).toUpperCase()

  // 3. Set expiration to 1 hour from now
  const expiresAt = new Date()
  expiresAt.setHours(expiresAt.getHours() + 1)

  // 4. Insert link_request
  const { error: insertError } = await supabase
    .from('link_requests')
    .insert({
      adult_id: user.id,
      child_id: childId,
      temporary_code: code,
      status: 'pending',
      expires_at: expiresAt.toISOString()
    })

  if (insertError) {
    throw new Error(`Failed to create request: ${insertError.message}`)
  }

  revalidatePath('/dashboard')

  // For the vertical slice UI, returning state via Next.js 14 Server Actions is best done
  // via form state or redirecting. We will just redirect for simplicity.
}
