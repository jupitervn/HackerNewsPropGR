package vn.jupiter.propertygurutest

import android.app.Application
import vn.jupiter.propertygurutest.di.AppComponent
import vn.jupiter.propertygurutest.di.DaggerAppComponent

open class PropertyGuruTestApplication : Application() {
    open val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
                .build()
    }
}