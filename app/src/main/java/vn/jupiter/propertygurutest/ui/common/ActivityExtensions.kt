package vn.jupiter.propertygurutest.ui.common

import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import vn.jupiter.propertygurutest.PropertyGuruTestApplication
import vn.jupiter.propertygurutest.di.AppComponent

fun AppCompatActivity.getApp(): PropertyGuruTestApplication {
    return application as PropertyGuruTestApplication
}

fun AppCompatActivity.appComponent(): AppComponent {
    return getApp().appComponent
}

fun Fragment.appComponent(): AppComponent {
    return (activity.application as PropertyGuruTestApplication).appComponent
}