package com.suitmedia.testportal.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.suitmedia.testportal.data.remote.response.DataItem
import com.suitmedia.testportal.data.remote.retrofit.ApiService

class UserPagingSource(private val apiService: ApiService, private val page: Int) :
    PagingSource<Int, DataItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DataItem> {
        return try {
            val currentPage = params.key ?: page
            val userResponse = apiService.getUsers(currentPage, params.loadSize)
            val totalPages = userResponse.totalPages

            LoadResult.Page(
                data = userResponse.data,
                prevKey = if (currentPage == page) null else currentPage - 1,
                nextKey = if (currentPage == totalPages) null else currentPage + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, DataItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}