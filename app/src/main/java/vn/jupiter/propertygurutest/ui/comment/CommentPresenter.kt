package vn.jupiter.propertygurutest.ui.comment

import vn.jupiter.propertygurutest.data.model.Comment
import vn.jupiter.propertygurutest.ui.common.DataLoaderUseCase
import vn.jupiter.propertygurutest.ui.common.ListPresenter

class CommentPresenter(dataLoaderUseCase: DataLoaderUseCase<Comment>) : ListPresenter<Comment, CommentScreenVM, CommentView>(dataLoaderUseCase, CommentScreenVM()) {

}