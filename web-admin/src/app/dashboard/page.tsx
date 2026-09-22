import { createClient } from '@/utils/supabase/server'
import { redirect } from 'next/navigation'
import { generateLinkCode } from './actions'

export default async function DashboardPage() {
  const supabase = createClient()
  const { data: { user }, error } = await supabase.auth.getUser()

  if (error || !user) {
    redirect('/login')
  }

  // Fetch linked children
  const { data: links } = await supabase
    .from('adult_child_links')
    .select('child_id, status, children(username)')
    .eq('adult_id', user.id)

  return (
    <div className="min-h-full">
      <header className="bg-white shadow">
        <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
          <h1 className="text-3xl font-bold tracking-tight text-gray-900">Adult Dashboard</h1>
        </div>
      </header>
      <main>
        <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
          <div className="bg-white shadow sm:rounded-lg mb-8">
            <div className="px-4 py-5 sm:p-6">
              <h3 className="text-base font-semibold leading-6 text-gray-900">Linked Children</h3>
              <div className="mt-2 max-w-xl text-sm text-gray-500">
                <p>Manage your linked children accounts below.</p>
              </div>

              <ul role="list" className="divide-y divide-gray-100 mt-4">
                {links?.map((link) => (
                  <li key={link.child_id} className="flex justify-between gap-x-6 py-5">
                    <div className="flex gap-x-4">
                      <div className="min-w-0 flex-auto">
                        <p className="text-sm font-semibold leading-6 text-gray-900">
                          {/* @ts-ignore Supabase returns an array for foreign tables in 1-to-1 implicitly without .single() via relationships */}
                          {Array.isArray(link.children) ? link.children[0]?.username : link.children?.username || 'Unknown'}
                        </p>
                        <p className="mt-1 truncate text-xs leading-5 text-gray-500">Status: {link.status}</p>
                      </div>
                    </div>
                  </li>
                ))}
                {(!links || links.length === 0) && (
                   <li className="py-4 text-sm text-gray-500">No children linked yet.</li>
                )}
              </ul>
            </div>
          </div>

          <div className="bg-white shadow sm:rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <h3 className="text-base font-semibold leading-6 text-gray-900">Link a new child</h3>
              <div className="mt-2 max-w-xl text-sm text-gray-500">
                <p>Enter the child's username to generate a secure linking code.</p>
              </div>
              <form action={generateLinkCode} className="mt-5 sm:flex sm:items-center">
                <div className="w-full sm:max-w-xs">
                  <label htmlFor="childUsername" className="sr-only">Child Username</label>
                  <input
                    type="text"
                    name="childUsername"
                    id="childUsername"
                    className="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                    placeholder="Enter child username"
                  />
                </div>
                <button
                  type="submit"
                  className="mt-3 inline-flex w-full items-center justify-center rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 sm:ml-3 sm:mt-0 sm:w-auto"
                >
                  Generate Code
                </button>
              </form>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}
