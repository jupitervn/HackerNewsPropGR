package vn.jupiter.propertygurutest.di

import dagger.Component
import vn.jupiter.propertygurutest.ui.home.HomeComponent
import vn.jupiter.propertygurutest.ui.comment.CommentComponent
import vn.jupiter.propertygurutest.ui.comment.CommentModule
import javax.inject.Singleton

@Component(modules = arrayOf(AppConfigurationModule::class, NetworkModule::class))
@Singleton
interface AppComponent {
    fun homeComponent(): HomeComponent
    fun commentComponent(commentModule: CommentModule): CommentComponent
}