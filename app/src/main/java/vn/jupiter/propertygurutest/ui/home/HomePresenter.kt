package vn.jupiter.propertygurutest.ui.home

import vn.jupiter.propertygurutest.data.model.Story
import vn.jupiter.propertygurutest.ui.common.DataLoaderUseCase
import vn.jupiter.propertygurutest.ui.common.ListPresenter

class HomePresenter(dataLoaderUseCase: DataLoaderUseCase<Story>) : ListPresenter<Story, HomeScreenVM, HomeView>(dataLoaderUseCase, HomeScreenVM()) {

}
