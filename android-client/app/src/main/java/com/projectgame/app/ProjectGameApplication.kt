package com.projectgame.app

import android.app.Application
import com.projectgame.app.data.supabase.SupabaseModule

class ProjectGameApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseModule.initialize(this)
    }
}
