package com.georeminder.di.component

import com.georeminder.ui.dashboard.changepassword.ChangePasswordActivity
import com.georeminder.ui.dashboard.friends.FriendDetailActivity
import com.georeminder.ui.dashboard.friends.FriendsFragment
import com.georeminder.ui.dashboard.friends.FriendsListFragment
import com.georeminder.ui.dashboard.friends.RequestedFriendListFragment
import com.georeminder.ui.dashboard.more.BlockedListActivity
import com.georeminder.ui.dashboard.more.MoreFragment
import com.georeminder.ui.dashboard.more.NotificationListActivity
import com.georeminder.ui.dashboard.profile.ProfileFragment
import com.georeminder.ui.dashboard.requests.*
import com.georeminder.utils.GeofenceTransitionsIntentService
import dagger.Component

//Created by imobdev-rujul on 2/1/19
@Component(dependencies = [LocalDataComponent::class, NetworkComponent::class])
interface NetworkLocalComponent {
    fun injectToAddRequest(addRequestActivity: AddRequestActivity)
    fun injectRequestDetail(requestDetailsActivity: RequestDetailsActivity)
    fun injectFriendDetail(friendDetailActivity: FriendDetailActivity)
    fun injectRequestFragment(requestsFragment: RequestsFragment)
    fun injectFriendsFragment(friendsFragment: FriendsFragment)
    fun injectFriendsListFragment(friendsListFragment: FriendsListFragment)
    fun injectRequestedFriendListFragment(requestedFriendListFragment: RequestedFriendListFragment)
    fun injectMoreFragment(moreFragment: MoreFragment)
    fun injectProfile(profileFragment: ProfileFragment)
    fun injectReceivedRequestFragment(receivedRequestsFragment: ReceivedRequestsFragment)
    fun injectChangePassword(changePasswordActivity: ChangePasswordActivity)
    fun injectToAssignTo(assignToActivity: AssignToActivity)
    fun injectToGeoFenceService(geofenceTransitionsIntentService: GeofenceTransitionsIntentService)
    fun injectBlockListActivity(blockedListActivity: BlockedListActivity)
    fun injectNotificationListActivity(notificationListActivity: NotificationListActivity)
}