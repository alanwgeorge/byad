package com.example.tylerwalker.buyyouadrink.model


sealed class ListItemType {
    object UserListItemType: ListItemType()
    object ListItemHeaderType: ListItemType()
}

sealed class ListItem(val type: ListItemType, val user: User? = null, val label: String? = null) {
    class UserListItem(user: User): ListItem(type = ListItemType.UserListItemType, user = user)
    class ListItemHeader(label: String): ListItem(type = ListItemType.ListItemHeaderType, label = label)
}