package com.sergeybannikov.alfrescogateway.results

import com.sergeybannikov.alfrescogateway.model.AlfrescoGroupMemberModel

data class AlfrescoGroupMembersResult
(
    var list: AlfrescoListPaginationResult? = null
)

data class AlfrescoListPaginationResult (
    var pagination: AlfrescoPaginationResult,
    var entries: List<AlfrescoGroupMemberEntryResult>
)

data class AlfrescoGroupMemberEntryResult (
    var entry: AlfrescoGroupMemberModel
)


data class AlfrescoPaginationResult (
    var count: Int = 0,
    var hasMoreItems: Boolean,
    var totalItems: Int = 0,
    var skipCount: Int = 0,
    var maxItems: Int = 0
)
